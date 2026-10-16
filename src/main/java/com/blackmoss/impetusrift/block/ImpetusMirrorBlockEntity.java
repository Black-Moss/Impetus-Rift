package com.blackmoss.impetusrift.block;

import com.blackmoss.impetusrift.ImpetusRift;
import com.blackmoss.impetusrift.energy.network.IImpetusNode;
import com.blackmoss.impetusrift.energy.network.ImpetusConnection;
import com.blackmoss.impetusrift.energy.network.ImpetusNetworkManager;
import com.blackmoss.impetusrift.energy.network.ImpetusNodePorts;
import com.blackmoss.impetusrift.registry.IRBlockEntities;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ImpetusMirrorBlockEntity extends BlockEntity implements IImpetusNode {
    public static final long MAX_TRANSFER_PER_TRANSACTION = 45L;
    public static final long FLUX_PER_POLLUTION = 1000L;
    public static final long MIRROR_CAPACITY = 1000L;
    private static final int TRANSFER_INTERVAL = 20;
    private static final long RECENT_ENERGY_WINDOW = 20L;
    private @Nullable GlobalPos linked;
    private boolean open;
    private long fluxProgress;
    private long buffer;
    private long lastEnergyGameTime = -1L;
    private int ticks;
    private List<BlockPos> connectedTargets = new ArrayList<>();

    public ImpetusMirrorBlockEntity(BlockPos pos, BlockState state) {
        super(IRBlockEntities.IMPETUS_MIRROR.get(), pos, state);
    }

    public static void staticTick(Level level, BlockPos pos, BlockState state, ImpetusMirrorBlockEntity mirror) {
        mirror.tick();
    }

    @Override
    public @NonNull BlockPos getNodePos() {
        return worldPosition;
    }

    @Override
    public @Nullable Level getNodeLevel() {
        return level;
    }

    @Override
    public @NonNull ImpetusNodePorts getPorts() {
        return ImpetusNodePorts.of(1, 1);
    }

    @Override
    public boolean canOutput() {
        return !isBound();
    }

    @Override
    public long receiveEnergy(long maxReceive, boolean simulate) {
        long free = MIRROR_CAPACITY - buffer;
        if (free <= 0L) {
            return 0L;
        }
        long accepted = Math.min(maxReceive, free);
        if (!simulate) {
            buffer += accepted;
            markRecentEnergy();
        }
        return accepted;
    }

    @Override
    public long extractEnergy(long maxExtract, boolean simulate) {
        long extracted = Math.min(maxExtract, buffer);
        if (!simulate && extracted > 0L) {
            buffer -= extracted;
            markRecentEnergy();
        }
        return extracted;
    }

    @Override
    public long getEnergyStored() {
        return buffer;
    }

    @Override
    public long getMaxEnergyStored() {
        return MIRROR_CAPACITY;
    }

    @Override
    public boolean hasRecentEnergy() {
        return level != null && (level.getGameTime() - lastEnergyGameTime) <= RECENT_ENERGY_WINDOW;
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

    @Override
    public int getConnectedInputCount() {
        if (level == null) return 0;
        ImpetusNetworkManager mgr = ImpetusNetworkManager.get(level);
        return mgr == null ? 0 : mgr.getConnectionsTo(worldPosition).size();
    }

    @Override
    public int getConnectedOutputCount() {
        if (level == null) return 0;
        ImpetusNetworkManager mgr = ImpetusNetworkManager.get(level);
        return mgr == null ? 0 : mgr.getConnectionsFrom(worldPosition).size();
    }

    private void markRecentEnergy() {
        if (level != null) {
            lastEnergyGameTime = level.getGameTime();
        }
    }

    public @Nullable GlobalPos getLink() {
        return linked;
    }

    public boolean isBound() {
        return linked != null;
    }

    public boolean shouldShowOpenMirror() {
        return open;
    }

    public void bindTo(GlobalPos otherPos) {
        if (level == null || level.isClientSide()) {
            return;
        }
        GlobalPos self = GlobalPos.of(level.dimension(), worldPosition);
        linked = otherPos;
        setChanged();
        syncToClient();

        ImpetusMirrorBlockEntity other = findMirror(otherPos);
        if (other != null) {
            other.linked = self;
            other.setChanged();
            other.syncToClient();
        }
    }

    public void unbind() {
        if (level == null || level.isClientSide()) {
            return;
        }
        ImpetusMirrorBlockEntity other = findMirror(linked);
        linked = null;
        open = false;
        setChanged();
        syncToClient();

        if (other != null && other.getLink() != null
                && other.getLink().dimension().equals(level.dimension())
                && other.getLink().pos().equals(worldPosition)) {
            other.linked = null;
            other.open = false;
            other.setChanged();
            other.syncToClient();
        }
    }

    public @Nullable ImpetusMirrorBlockEntity findMirror(@Nullable GlobalPos pos) {
        if (pos == null || !(level instanceof ServerLevel serverLevel)) {
            return null;
        }
        ServerLevel target = serverLevel.getServer().getLevel(pos.dimension());
        if (target == null) {
            return null;
        }
        return target.getBlockEntity(pos.pos()) instanceof ImpetusMirrorBlockEntity mirror ? mirror : null;
    }

    private void tick() {
        if (level == null || level.isClientSide()) {
            return;
        }
        ticks++;

        if (ticks % 10 == 0) {
            updateConnectedTargets();
        }

        if (ticks % TRANSFER_INTERVAL == 0) {
            transferToLinkedMirror();
            settleFlux();
        }
    }

    private void transferToLinkedMirror() {
        if (buffer <= 0L || linked == null) {
            return;
        }
        ImpetusMirrorBlockEntity other = findMirror(linked);
        if (other == null) {
            open = false;
            return;
        }
        if (!other.open) {
            other.open = true;
        }
        if (!open) {
            open = true;
            setChanged();
            syncToClient();
        }

        long amount = Math.min(Math.min(buffer, MAX_TRANSFER_PER_TRANSACTION), other.getFreeCapacity());
        if (amount <= 0L) {
            return;
        }
        long accepted = other.receiveEnergy(amount, false);
        if (accepted > 0L) {
            buffer -= accepted;
            markRecentEnergy();
            fluxProgress += accepted;
            setChanged();
            syncToClient();
            other.markRecentEnergy();
            other.setChanged();
            other.syncToClient();
        }
    }

    private long getFreeCapacity() {
        return Math.max(0L, MIRROR_CAPACITY - buffer);
    }

    private void settleFlux() {
        if (fluxProgress < FLUX_PER_POLLUTION || level == null) {
            return;
        }
        int pollution = (int) Math.min(Integer.MAX_VALUE, fluxProgress / FLUX_PER_POLLUTION);
        fluxProgress %= FLUX_PER_POLLUTION;
        AuraHelper.polluteAura(level, worldPosition, pollution, true);
        setChanged();
    }

    private void updateConnectedTargets() {
        if (level == null || level.isClientSide()) {
            return;
        }
        ImpetusNetworkManager mgr = ImpetusNetworkManager.get(level);
        if (mgr == null) {
            return;
        }
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
        if (level == null || level.isClientSide()) {
            return;
        }
        BlockState current = getBlockState();
        level.sendBlockUpdated(getBlockPos(), current, current, 3);
    }

    @Override
    protected void saveAdditional(@NonNull ValueOutput output) {
        super.saveAdditional(output);
        output.storeNullable("Link", GlobalPos.CODEC, linked);
        output.putBoolean("Open", open);
        output.putLong("Flux", fluxProgress);
        output.putLong("LastEnergyTime", lastEnergyGameTime);
    }

    @Override
    protected void loadAdditional(@NonNull ValueInput input) {
        super.loadAdditional(input);
        linked = input.read("Link", GlobalPos.CODEC).orElse(null);
        open = input.getBooleanOr("Open", false);
        fluxProgress = input.getLongOr("Flux", 0L);
        lastEnergyGameTime = input.getLongOr("LastEnergyTime", -1L);
        buffer = 0L;
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
}
