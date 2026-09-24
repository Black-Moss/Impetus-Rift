package com.blackmoss.impetusrift.block;

import com.blackmoss.impetusrift.ImpetusRift;
import com.blackmoss.impetusrift.energy.ImpetusEnergyStorage;
import com.blackmoss.impetusrift.energy.network.IImpetusNode;
import com.blackmoss.impetusrift.energy.network.ImpetusNodePorts;
import com.blackmoss.impetusrift.energy.network.ImpetusConnection;
import com.blackmoss.impetusrift.energy.network.ImpetusNetworkManager;
import com.blackmoss.impetusrift.registry.IRBlockEntities;
import com.leclowndu93150.thaumaturge.content.entity.EntityFluxRift;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ImpetusDrainerBlockEntity extends BlockEntity implements IImpetusNode {
    private static final int SCAN_INTERVAL = 60;
    private static final long RECENT_ENERGY_WINDOW = 20L;
    private static final double SCAN_RADIUS = 8.0;
    private static final long BUFFER_CAPACITY = 1000L;
    private static final long MAX_TRANSFER = 100L;

    private final ImpetusEnergyStorage energyStorage = new ImpetusEnergyStorage(BUFFER_CAPACITY, MAX_TRANSFER, MAX_TRANSFER);
    private boolean active = false;
    private int ticks;
    private List<BlockPos> connectedTargets = new ArrayList<>();
    private @Nullable EntityFluxRift boundRift;
    private @Nullable Vec3 lastRiftPos;
    private long lastEnergyGameTime = -1L;

    public ImpetusDrainerBlockEntity(BlockPos pos, BlockState state) {
        super(IRBlockEntities.IMPETUS_DRAINER.get(), pos, state);
        this.ticks = (int) (Math.random() * SCAN_INTERVAL);
    }

    public static void staticTick(
            Level level,
            BlockPos pos,
            BlockState state,
            ImpetusDrainerBlockEntity drainer) {
        drainer.tick();
    }

    private void tick() {
        if (level == null || level.isClientSide()) return;

        ticks++;

        if (ticks % 20 == 0) {
            updateRedstoneState();
        }

        boolean enabled = level.getBlockState(worldPosition).getValue(ImpetusDrainerBlock.ENABLED);

        if (ticks % SCAN_INTERVAL == 0 && enabled) {
            findRift();
        } else if (!enabled && boundRift != null) {
            unbindRift();
            setChanged();
        }

        if (boundRift != null && enabled) {
            extractEnergyFromRift();
        }

        if (ticks % SCAN_INTERVAL == 0) {
            updateConnectedTargets();
        }

        if (ticks % 20 == 0) {
            validateConnections();
        }

        if (level != null && energyStorage.getEnergyStored() > 0) {
            lastEnergyGameTime = level.getGameTime();
        }

        if (ticks >= Integer.MAX_VALUE - 1) {
            ticks = 0;
        }
    }

    private void updateRedstoneState() {
        if (level == null) return;
        BlockState state = level.getBlockState(worldPosition);
        boolean powered = level.hasNeighborSignal(worldPosition);
        boolean currentlyEnabled = state.getValue(ImpetusDrainerBlock.ENABLED);
        if (powered == currentlyEnabled) {
            level.setBlock(worldPosition, state.setValue(ImpetusDrainerBlock.ENABLED, !powered), 3);
        }
    }

    @Override
    public ImpetusNodePorts getPorts() {
        return ImpetusNodePorts.of(0, 2);
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
        return 0;
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
    public int getConnectedOutputCount() {
        if (level == null) return 0;
        ImpetusNetworkManager mgr = ImpetusNetworkManager.get(level);
        return mgr != null
                ? mgr.getConnectionsFrom(worldPosition).size()
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

    private void validateConnections() {
        if (level == null || level.isClientSide()) return;
        ImpetusNetworkManager mgr = ImpetusNetworkManager.get(level);
        if (mgr == null) return;

        boolean changed = false;
        for (BlockPos target : List.copyOf(connectedTargets)) {
            if (level.isLoaded(target)) {
                BlockEntity be = level.getBlockEntity(target);
                if (!(be instanceof IImpetusNode)) {
                    mgr.removeConnection(worldPosition, target);
                    connectedTargets.remove(target);
                    changed = true;
                }
            }
        }
        if (changed) {
            setChanged();
            syncToClient();
        }
    }

    private void findRift() {
        if (level == null) return;

        List<EntityFluxRift> rifts = level.getEntitiesOfClass(
                EntityFluxRift.class,
                new AABB(worldPosition).inflate(SCAN_RADIUS),
                rift -> rift.isAlive() && !rift.isRemoved() && !rift.getCollapse()
        );

        if (rifts.isEmpty()) {
            if (boundRift != null) {
                unbindRift();
                setChanged();
            }
            return;
        }

        rifts.sort(Comparator.comparingDouble(rift ->
                rift.position().distanceToSqr(Vec3.atCenterOf(worldPosition))));

        for (EntityFluxRift rift : rifts) {
            if (checkRiftLineOfSight(rift)) {
                if (boundRift != rift) {
                    boundRift = rift;
                    active = true;
                    setChanged();
                }
                return;
            }
        }

        if (boundRift != null) {
            unbindRift();
            setChanged();
        }
    }

    private boolean checkRiftLineOfSight(EntityFluxRift rift) {
        if (level == null) return false;

        Vec3 riftPos = rift.position();
        double dX = Math.clamp(riftPos.x - worldPosition.getX(), -1, 1);
        double dY = Math.clamp(riftPos.y - worldPosition.getY(), -1, 1);
        double dZ = Math.clamp(riftPos.z - worldPosition.getZ(), -1, 1);

        Vec3 rayStart = new Vec3(
                worldPosition.getX() + 0.5 + dX,
                worldPosition.getY() + 0.5 + dY,
                worldPosition.getZ() + 0.5 + dZ);

        List<Vec3> riftPoints = rift.points;
        if (riftPoints == null || riftPoints.isEmpty()) return false;

        Vec3 riftOrigin = rift.position();

        for (Vec3 point : riftPoints) {
            Vec3 worldPoint = riftOrigin.add(point);
            BlockHitResult result = level.clip(new ClipContext(
                    rayStart, worldPoint,
                    ClipContext.Block.VISUAL, ClipContext.Fluid.NONE,
                    CollisionContext.empty()));

            if (result.getType() == HitResult.Type.MISS) {
                lastRiftPos = worldPoint;
                return true;
            }
        }

        return false;
    }

    private void unbindRift() {
        boundRift = null;
        lastRiftPos = null;
        active = false;
    }

    private void extractEnergyFromRift() {
        if (level == null
                || boundRift == null
                || boundRift.isRemoved()
                || boundRift.getCollapse()) {
            unbindRift();
            setChanged();
            return;
        }

        int riftSize = boundRift.getRiftSize();
        if (riftSize <= 0) {
            unbindRift();
            setChanged();
            return;
        }

        long canStore = energyStorage.getMaxEnergyStored() - energyStorage.getEnergyStored();
        if (canStore <= 0) {
            return;
        }

        long energyGain = calcEnergyForSize(riftSize);
        long accepted = energyStorage.receiveEnergy(energyGain, false);
        if (accepted > 0) {
            boundRift.setRiftSize(riftSize - 1);
            boundRift.setRiftStability(boundRift.getRiftStability() - 1.0F);

            if (boundRift.getRiftSize() <= 0) {
                unbindRift();
            }
            setChanged();
        }
    }

    public static long calcEnergyForSize(int size) {
        return (long) Math.floor(
                0.5 * StrictMath.exp(-(size - 172.0) * (size - 172.0) / 11250.0) * size + 1.0);
    }

    public static long calcTotalRiftEnergy(int size) {
        long total = 0;
        for (int i = 1; i <= size; i++) {
            total += calcEnergyForSize(i);
        }
        return total;
    }

    protected final void syncToClient() {
        if (level == null || level.isClientSide()) return;
        BlockState current = getBlockState();
        level.sendBlockUpdated(getBlockPos(), current, current, 3);
    }

    @Override
    protected void saveAdditional(@NonNull ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean("Active", active);
        output.putInt("Ticks", ticks);
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
        active = input.getBooleanOr("Active", false);
        ticks = input.getIntOr("Ticks", 0);
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
        return active;
    }

    @Override
    public boolean hasRecentEnergy() {
        if (level == null || lastEnergyGameTime < 0) return false;
        return level.getGameTime() - lastEnergyGameTime < RECENT_ENERGY_WINDOW;
    }

    public boolean isEnabled() {
        return getBlockState().getValue(ImpetusDrainerBlock.ENABLED);
    }

    public boolean hasBoundRift() {
        return boundRift != null && !boundRift.isRemoved();
    }

    public @Nullable EntityFluxRift getBoundRift() {
        return boundRift;
    }

    public int getNearbyRiftCount() {
        if (level == null) return 0;
        return level.getEntitiesOfClass(
                EntityFluxRift.class,
                new AABB(worldPosition).inflate(SCAN_RADIUS),
                rift -> rift.isAlive() && !rift.isRemoved()).size();
    }
}
