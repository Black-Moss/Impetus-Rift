package com.blackmoss.impetusrift.block;

import com.blackmoss.impetusrift.ImpetusRift;
import com.blackmoss.impetusrift.energy.ImpetusEnergyStorage;
import com.blackmoss.impetusrift.energy.network.IImpetusNode;
import com.blackmoss.impetusrift.energy.network.ImpetusConnection;
import com.blackmoss.impetusrift.energy.network.ImpetusNetworkManager;
import com.blackmoss.impetusrift.energy.network.ImpetusNodePorts;
import com.blackmoss.impetusrift.registry.IRBlockEntities;
import com.leclowndu93150.thaumaturge.api.infusion.IInfusionStabiliser;
import com.leclowndu93150.thaumaturge.api.items.IGogglesDisplayExtended;
import com.leclowndu93150.thaumaturge.content.infusion.InfusionStabilitySurvey;
import com.leclowndu93150.thaumaturge.content.infusion.InstabilityEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.NonNull;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImpetusMatrixBlockEntity extends BlockEntity implements IImpetusNode, IGogglesDisplayExtended {
    private static final long BASE_CAPACITY = 0L;
    private static final long MAX_TRANSFER = 200L;
    private static final float MIN_STABILITY = -100.0F;
    private static final float MAX_STABILITY = 25.0F;
    private static final int STABILITY_DECAY_INTERVAL = 20;
    private static final int STABILITY_SCAN_RADIUS = 2;
    private static final double STABILISER_DIMINISHING = 0.75;
    private static final DecimalFormat STABILITY_FORMAT = new DecimalFormat("#######.##");
    private final ImpetusEnergyStorage energyStorage = new ImpetusEnergyStorage(BASE_CAPACITY, MAX_TRANSFER);
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

        if (level.isClientSide()) {
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

    public void onCapacityChanged() {
        if (level == null || level.isClientSide()) return;
        updateInstalledUnits();
    }

    private void updateStability() {
        if (level == null || !checkLocation()) return;

        stabilityGain = calculateStabilityGain();

        float loss = getStabilityLossPerCycle();
        stability -= level.getRandom().nextFloat() * loss;
        stability += stabilityGain;
        stability = Math.clamp(stability, MIN_STABILITY, MAX_STABILITY);

        if (stability < 0.0F && level.getRandom().nextInt(1500) <= Math.abs(stability)) {
            if (level.getRandom().nextInt(5) == 0) {
                ejectEnergyUnit();
            } else {
                long discharge = (long) (energyStorage.getMaxEnergyStored() * level.getRandom().nextFloat() * 0.02F);
                energyStorage.extractEnergy(discharge, false);
            }
            InstabilityEvents.trigger((ServerLevel) level, worldPosition, List.of());
            setChanged();
            syncToClient();
        }
    }

    private float calculateStabilityGain() {
        if (level == null) return 0.0F;

        float total = 0.0F;
        Map<Block, Integer> countedByType = new HashMap<>();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -STABILITY_SCAN_RADIUS; dx <= STABILITY_SCAN_RADIUS; dx++) {
            for (int dz = -STABILITY_SCAN_RADIUS; dz <= STABILITY_SCAN_RADIUS; dz++) {
                for (int dy = -STABILITY_SCAN_RADIUS; dy <= STABILITY_SCAN_RADIUS; dy++) {
                    if (dx == 0 && dy == 0 && dz == 0) continue;
                    cursor.set(worldPosition.getX() + dx, worldPosition.getY() + dy, worldPosition.getZ() + dz);
                    if (!InfusionStabilitySurvey.isStabiliser(level, cursor)) continue;

                    Block block = level.getBlockState(cursor).getBlock();
                    float amount = block instanceof IInfusionStabiliser stabiliser
                            ? stabiliser.getStabilizationAmount(level, cursor)
                            : IInfusionStabiliser.DEFAULT_STABILIZATION;
                    int counted = countedByType.merge(block, 1, Integer::sum) - 1;
                    if (counted > 0) {
                        amount *= (float) Math.pow(STABILISER_DIMINISHING, counted);
                    }
                    total += amount;
                }
            }
        }
        return total;
    }

    private float getStabilityLossPerCycle() {
        long maxEnergy = energyStorage.getMaxEnergyStored();
        if (maxEnergy <= 0) return 0.0F;
        return (float) ((double) energyStorage.getEnergyStored() / maxEnergy);
    }

    private void ejectEnergyUnit() {
        if (level == null) return;

        BlockPos belowPos = worldPosition.below();
        if (level.getBlockEntity(belowPos) instanceof ImpetusAltarBaseBlockEntity belowBase) {
            if (belowBase.ejectRandomUnit(level, belowPos)) {
                return;
            }
        }

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
    public Component[] getIGogglesText() {
        Component tier = Component.translatable(stabilityTierKey()).withStyle(ChatFormatting.BOLD);
        Component gain = Component.literal(STABILITY_FORMAT.format(stabilityGain) + " ")
                .append(Component.translatable("gui.thaumaturge.infusion.stability.gain"))
                .withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC);
        float loss = getStabilityLossPerCycle();
        if (loss <= 0.0F) {
            return new Component[]{tier, gain};
        }
        Component lossText = Component.translatable("gui.thaumaturge.infusion.stability.range")
                .append(Component.literal(STABILITY_FORMAT.format(loss) + " ")
                        .append(Component.translatable("gui.thaumaturge.infusion.stability.loss")
                                .withStyle(ChatFormatting.ITALIC)))
                .withStyle(ChatFormatting.RED);
        return new Component[]{tier, gain, lossText};
    }

    private String stabilityTierKey() {
        String tier = stability > 12.5F ? "very_stable"
                : stability >= 0.0F ? "stable"
                : stability > -25.0F ? "unstable"
                : "very_unstable";
        return "gui.thaumaturge.infusion.stability." + tier;
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