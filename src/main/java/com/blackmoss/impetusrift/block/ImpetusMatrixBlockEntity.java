package com.blackmoss.impetusrift.block;

import com.blackmoss.impetusrift.ImpetusRift;
import com.blackmoss.impetusrift.energy.ImpetusEnergyStorage;
import com.blackmoss.impetusrift.energy.network.IImpetusNode;
import com.blackmoss.impetusrift.energy.network.ImpetusNodePorts;
import com.blackmoss.impetusrift.energy.network.ImpetusConnection;
import com.blackmoss.impetusrift.energy.network.ImpetusNetworkManager;
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
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public class ImpetusMatrixBlockEntity extends BlockEntity implements IImpetusNode {
    private static final long BASE_CAPACITY = 0L;
    private static final long MAX_TRANSFER = 200L;
    private static final float MIN_STABILITY = -100.0F;
    private static final float MAX_STABILITY = 25.0F;
    private static final int STABILITY_DECAY_INTERVAL = 20;

    private final ImpetusEnergyStorage energyStorage = new ImpetusEnergyStorage(BASE_CAPACITY, MAX_TRANSFER, MAX_TRANSFER);
    public float clientStartUp = 0.0F;
    private int installedUnits = 0;
    private float stability = 0.0F;
    private float stabilityGain = 0.0F;
    private int ticks;

    private List<BlockPos> connectedTargets = new ArrayList<>();

    public ImpetusMatrixBlockEntity(BlockPos pos, BlockState state) {
        super(IRBlockEntities.IMPETUS_MATRIX.get(), pos, state);
        this.ticks = (int) (Math.random() * STABILITY_DECAY_INTERVAL);
    }

    public static void staticTick(
            Level level,
            BlockPos pos,
            BlockState state,
            ImpetusMatrixBlockEntity matrix) {
        matrix.tick();
    }

    public boolean checkLocation() {
        if (level == null) return false;
        BlockPos belowPos = worldPosition.below();
        BlockState belowState = level.getBlockState(belowPos);
        if (!isPedestalWithFacing(belowState, Direction.UP)) return false;
        BlockPos abovePos = worldPosition.above();
        BlockState aboveState = level.getBlockState(abovePos);
        return isPedestalWithFacing(aboveState, Direction.DOWN);
    }

    private boolean isPedestalWithFacing(BlockState state, Direction facing) {
        if (state.getBlock() instanceof ImpetusAltarBaseBlock) {
            return state.getValue(ImpetusAltarBaseBlock.FACING) == facing;
        }
        return false;
    }

    private void tick() {
        if (level == null) return;

        // 客户端：平滑过渡 clientStartUp
        if (level.isClientSide()) {
            // 服务端通过 NBT 同步 active 状态，客户端直接用 checkLocation
            boolean shouldBeActive = checkLocation();
            if (shouldBeActive) {
                clientStartUp = Math.min(1.0F, clientStartUp + 0.02F);
            } else {
                clientStartUp = Math.max(0.0F, clientStartUp - 0.02F);
            }
            return;
        }

        // 服务端逻辑
        ticks++;
        if (ticks % STABILITY_DECAY_INTERVAL == 0) {
            updateInstalledUnits();
            updateStability();
            updateConnectedTargets();
        }
    }

    /**
     * 从上下基座读取已安装的能量单元数，更新容量
     */
    private void updateInstalledUnits() {
        if (level == null) return;
        int total = 0;

        BlockPos belowPos = worldPosition.below();
        if (level.getBlockEntity(belowPos) instanceof ImpetusAltarBaseBlockEntity belowBase) {
            total += belowBase.getInstalledUnitCount();
        }

        BlockPos abovePos = worldPosition.above();
        if (level.getBlockEntity(abovePos) instanceof ImpetusAltarBaseBlockEntity aboveBase) {
            total += aboveBase.getInstalledUnitCount();
        }

        if (total != installedUnits) {
            installedUnits = total;
            long newCapacity = installedUnits * ImpetusAltarBaseBlockEntity.ENERGY_PER_UNIT;
            energyStorage.setCapacity(newCapacity);
            setChanged();
            syncToClient();
        }
    }

    /**
     * 当基座的能量单元变化时调用
     */
    public void onCapacityChanged() {
        if (level == null || level.isClientSide()) return;
        updateInstalledUnits();
    }

    private void updateStability() {
        if (level == null || !checkLocation()) return;

        // 计算稳定性增益（来自5×5×5范围内的稳定器方块）
        stabilityGain = calculateStabilityGain();

        // 计算稳定性损失（与能量比例成正比）
        // 规范：0.01稳定度=1%能量上限，所以每周期损失 = 能量比例 * 1.0
        float loss = getStabilityLossPerCycle();
        stability -= level.getRandom().nextFloat() * loss;
        stability += stabilityGain;
        stability = Math.clamp(stability, MIN_STABILITY, MAX_STABILITY);

        // 不稳定效果
        if (stability < 0.0F && level.getRandom().nextInt(1500) <= Math.abs(stability)) {
            if (level.getRandom().nextInt(5) == 0) {
                // 20%概率：弹出能量单元
                ejectEnergyUnit();
            } else {
                // 80%概率：释放能量（紫色闪电电弧效果）
                long discharge = (long) (energyStorage.getMaxEnergyStored() * level.getRandom().nextFloat() * 0.02F);
                energyStorage.extractEnergy(discharge, false);
                // TODO: 生成紫色闪电电弧粒子效果
            }
            setChanged();
            syncToClient();
        }
    }

    /**
     * 计算稳定性增益（扫描5×5×5范围内的稳定器方块）
     */
    private float calculateStabilityGain() {
        if (level == null) return 0.0F;

        float gain = 0.0F;
        // 扫描5×5×5范围
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -2; dz <= 2; dz++) {
                    BlockPos checkPos = worldPosition.offset(dx, dy, dz);
                    BlockState checkState = level.getBlockState(checkPos);
                    // 检查是否是稳定器方块（如 candles、水晶等）
                    // 暂时用简单的方块检测，后续可以扩展
                    if (isStabilizerBlock(checkState)) {
                        gain += 0.02F; // 每个稳定器提供0.02增益
                    }
                }
            }
        }
        return Math.min(gain, 0.5F); // 最大增益0.5
    }

    private boolean isStabilizerBlock(BlockState state) {
        // 检查是否是蜡烛、水晶等稳定器方块
        // 暂时返回false，后续可以添加具体方块检测
        return false;
    }

    private float getStabilityLossPerCycle() {
        long maxEnergy = energyStorage.getMaxEnergyStored();
        if (maxEnergy <= 0) return 0.0F;
        return (float) ((double) energyStorage.getEnergyStored() / maxEnergy);
    }

    private void ejectEnergyUnit() {
        if (level == null) return;

        // 尝试从下方基座弹出
        BlockPos belowPos = worldPosition.below();
        if (level.getBlockEntity(belowPos) instanceof ImpetusAltarBaseBlockEntity belowBase) {
            if (belowBase.ejectRandomUnit(level, belowPos)) {
                return;
            }
        }

        // 尝试从上方基座弹出
        BlockPos abovePos = worldPosition.above();
        if (level.getBlockEntity(abovePos) instanceof ImpetusAltarBaseBlockEntity aboveBase) {
            aboveBase.ejectRandomUnit(level, abovePos);
        }
    }

    @Override
    public boolean isBulkStorage() {
        return true;
    }

    @Override
    public ImpetusNodePorts getPorts() {
        return ImpetusNodePorts.of(3, 3);
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
        return mgr != null ? mgr.getConnectionsTo(worldPosition).size() : 0;
    }

    @Override
    public int getConnectedOutputCount() {
        if (level == null) return 0;
        ImpetusNetworkManager mgr = ImpetusNetworkManager.get(level);
        return mgr != null ? mgr.getConnectionsFrom(worldPosition).size() : 0;
    }

    @Override
    public void onEnergyChanged() {
        setChanged();
        syncToClient();
    }

    @Override
    public float getStability() {
        return stability;
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

    public int getComparatorOutput() {
        long maxEnergy = energyStorage.getMaxEnergyStored();
        if (maxEnergy <= 0) return 0;
        return (int) ((float) energyStorage.getEnergyStored() / maxEnergy * 15.0F);
    }

    public float getStabilityGain() {
        return stabilityGain;
    }

    protected final void syncToClient() {
        if (level == null || level.isClientSide()) return;
        BlockState current = getBlockState();
        level.sendBlockUpdated(getBlockPos(), current, current, 3);
    }

    @Override
    protected void saveAdditional(@NonNull ValueOutput output) {
        super.saveAdditional(output);
        output.putFloat("Stability", stability);
        output.putFloat("StabilityGain", stabilityGain);
        output.putInt("Ticks", ticks);
        output.putInt("InstalledUnits", installedUnits);
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
        stability = input.getFloatOr("Stability", 0.0F);
        stabilityGain = input.getFloatOr("StabilityGain", 0.0F);
        ticks = input.getIntOr("Ticks", 0);
        installedUnits = input.getIntOr("InstalledUnits", 0);
        input.child("EnergyStorage").ifPresent(energyStorage::readFromNBT);
        // 恢复容量
        energyStorage.setCapacity(installedUnits * ImpetusAltarBaseBlockEntity.ENERGY_PER_UNIT);
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

    public int getInstalledUnits() {
        return installedUnits;
    }
}