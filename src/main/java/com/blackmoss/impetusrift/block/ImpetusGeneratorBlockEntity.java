package com.blackmoss.impetusrift.block;

import com.blackmoss.impetusrift.ImpetusRift;
import com.blackmoss.impetusrift.energy.ImpetusEnergyStorage;
import com.blackmoss.impetusrift.energy.network.IImpetusNode;
import com.blackmoss.impetusrift.energy.network.ImpetusConnection;
import com.blackmoss.impetusrift.energy.network.ImpetusNetworkManager;
import com.blackmoss.impetusrift.energy.network.ImpetusNodePorts;
import com.blackmoss.impetusrift.registry.IRBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public class ImpetusGeneratorBlockEntity extends BlockEntity implements IImpetusNode, EnergyHandler {

    private static final long RECENT_ENERGY_WINDOW = 20L;
    private static final int FE_CAPACITY = 2000;
    private static final int MAX_PUSH = 40;
    private static final int FE_PER_IMPETUS = 20;
    private static final long MAX_CONVERT = 5;

    private final ImpetusEnergyStorage energyStorage = new ImpetusEnergyStorage(1000, 100, 100);
    private int feBuffer;
    private boolean isActive = false;
    private int ticks;
    private List<BlockPos> connectedTargets = new ArrayList<>();
    private long lastEnergyGameTime = -1L;

    public ImpetusGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(IRBlockEntities.IMPETUS_GENERATOR.get(), pos, state);
        this.ticks = (int) (Math.random() * 10);
    }

    public static void staticTick(
            Level level,
            BlockPos pos,
            BlockState state,
            ImpetusGeneratorBlockEntity generator) {
        generator.tick();
    }

    private void tick() {
        if (level == null || level.isClientSide()) return;

        ticks++;
        if (ticks % 10 == 0) {
            convertImpetusToFe();
            pushFeToNeighbors();
            updateConnectedTargets();
        }

        if (energyStorage.getEnergyStored() > 0) {
            lastEnergyGameTime = level.getGameTime();
        }
    }

    private void convertImpetusToFe() {
        if (level == null) return;
        int feSpace = FE_CAPACITY - feBuffer;
        if (feSpace <= 0) {
            setActive(false);
            return;
        }
        int impetusWanted = (int) Math.min(MAX_CONVERT, feSpace / FE_PER_IMPETUS);
        if (impetusWanted <= 0) {
            setActive(false);
            return;
        }
        long consumed = energyStorage.extractEnergy(impetusWanted, false);
        if (consumed > 0) {
            feBuffer += (int) (consumed * FE_PER_IMPETUS);
            setActive(true);
            setChanged();
        } else {
            setActive(false);
        }
    }

    public Direction outputFace() {
        return getBlockState().getValue(ImpetusGeneratorBlock.FACING);
    }

    private void pushFeToNeighbors() {
        if (level == null || feBuffer <= 0) return;
        Direction dir = outputFace();
        EnergyHandler target = level.getCapability(
                Capabilities.Energy.BLOCK, worldPosition.relative(dir), dir.getOpposite());
        if (target == null) return;

        int toPush = Math.min(feBuffer, MAX_PUSH);
        int pushed;
        try (Transaction ctx = Transaction.openRoot()) {
            pushed = target.insert(toPush, ctx);
            if (pushed > 0) ctx.commit();
        }
        if (pushed > 0) {
            feBuffer -= pushed;
            setChanged();
        }
    }

    @Override
    public ImpetusNodePorts getPorts() {
        return ImpetusNodePorts.of(2, 0);
    }

    @Override
    public BlockPos getNodePos() {
        return worldPosition;
    }

    @Override
    public Level getNodeLevel() {
        return level;
    }

    @Override
    public long receiveEnergy(long maxReceive, boolean simulate) {
        long received = energyStorage.receiveEnergy(maxReceive, simulate);
        if (received > 0 && !simulate) setChanged();
        return received;
    }

    @Override
    public long extractEnergy(long maxExtract, boolean simulate) {
        return energyStorage.extractEnergy(maxExtract, simulate);
    }

    @Override
    public long getEnergyStored() {
        return energyStorage.getEnergyStored();
    }

    @Override
    public long getMaxEnergyStored() {
        return energyStorage.getMaxEnergyStored();
    }

    @Override
    public int getConnectedInputCount() {
        if (level == null) return 0;
        ImpetusNetworkManager mgr = ImpetusNetworkManager.get(level);
        return mgr != null
                ? mgr.getConnectionsTo(worldPosition).size()
                : 0;
    }

    @Override
    public void onEnergyChanged() {
        setChanged();
        syncToClient();
    }

    @Override
    public List<BlockPos> getBeamTargetPositions() {
        return connectedTargets;
    }

    private void updateConnectedTargets() {
        if (level == null || level.isClientSide()) return;
        ImpetusNetworkManager mgr = ImpetusNetworkManager.get(level);
        if (mgr == null) return;
        List<BlockPos> newTargets = mgr.getConnectionsFrom(worldPosition).stream()
                .map(ImpetusConnection::targetPos)
                .toList();
        if (!connectedTargets.equals(newTargets)) {
            connectedTargets = new ArrayList<>(newTargets);
            setChanged();
            syncToClient();
        }
    }

    protected final void syncToClient() {
        if (level == null || level.isClientSide()) return;
        BlockState current = getBlockState();
        level.sendBlockUpdated(getBlockPos(), current, current, 3);
    }

    @Override
    public long getAmountAsLong() {
        return feBuffer;
    }

    @Override
    public long getCapacityAsLong() {
        return FE_CAPACITY;
    }

    @Override
    public int insert(int amount, @NonNull TransactionContext transaction) {
        return 0;
    }

    @Override
    public int extract(int amount, @NonNull TransactionContext transaction) {
        int extracted = Math.min(amount, feBuffer);
        if (extracted > 0) {
            feBuffer -= extracted;
            setChanged();
        }
        return extracted;
    }

    public int getFeStored() {
        return feBuffer;
    }

    public int getFeCapacity() {
        return FE_CAPACITY;
    }

    @Override
    protected void saveAdditional(@NonNull ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("IsActive", isActive ? 1 : 0);
        output.putInt("Ticks", ticks);
        output.putInt("FeBuffer", feBuffer);
        output.putLong("LastEnergyTime", lastEnergyGameTime);
        energyStorage.writeToNBT(output.child("EnergyStorage"));
        output.putInt("BeamTargets", connectedTargets.size());
        for (int i = 0; i < connectedTargets.size(); i++) {
            BlockPos t = connectedTargets.get(i);
            output.putInt("BT" + i + "X", t.getX());
            output.putInt("BT" + i + "Y", t.getY());
            output.putInt("BT" + i + "Z", t.getZ());
        }
    }

    @Override
    protected void loadAdditional(@NonNull ValueInput input) {
        super.loadAdditional(input);
        isActive = input.getIntOr("IsActive", 0) == 1;
        ticks = input.getIntOr("Ticks", 0);
        feBuffer = input.getIntOr("FeBuffer", 0);
        lastEnergyGameTime = input.getLongOr("LastEnergyTime", -1L);
        input.child("EnergyStorage").ifPresent(energyStorage::readFromNBT);
        int count = input.getIntOr("BeamTargets", 0);
        connectedTargets = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            int x = input.getIntOr("BT" + i + "X", 0);
            int y = input.getIntOr("BT" + i + "Y", 0);
            int z = input.getIntOr("BT" + i + "Z", 0);
            connectedTargets.add(new BlockPos(x, y, z));
        }
    }

    @Override
    public @NonNull CompoundTag getUpdateTag(HolderLookup.@NonNull Provider registries) {
        CompoundTag nbt = super.getUpdateTag(registries);
        try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(this.problemPath(), ImpetusRift.LOGGER)) {
            TagValueOutput output = TagValueOutput.createWithContext(reporter, registries);
            saveAdditional(output);
            nbt.merge(output.buildResult());
        }
        return nbt;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public ImpetusEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    public boolean isActive() {
        return isActive;
    }

    private void setActive(boolean active) {
        if (this.isActive != active) {
            this.isActive = active;
            setChanged();
            syncToClient();
        }
    }

    @Override
    public boolean hasRecentEnergy() {
        if (level == null || lastEnergyGameTime < 0) return false;
        return level.getGameTime() - lastEnergyGameTime < RECENT_ENERGY_WINDOW;
    }
}
