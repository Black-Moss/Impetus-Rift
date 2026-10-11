package com.blackmoss.impetusrift.block;

import com.blackmoss.impetusrift.ImpetusRift;
import com.blackmoss.impetusrift.energy.network.IImpetusNode;
import com.blackmoss.impetusrift.energy.network.ImpetusNodePorts;
import com.blackmoss.impetusrift.registry.IRBlockEntities;
import net.minecraft.core.BlockPos;
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
import org.jspecify.annotations.Nullable;

public class ImpetusGateBlockEntity extends BlockEntity implements IImpetusNode {
    private static final long GATE_TRANSFER = 1000L;
    private static final long RECENT_ENERGY_WINDOW = 20L;
    private static final int GATE_POLL_INTERVAL = 5;

    private long buffer = 0L;
    private long lastEnergyGameTime = -1L;

    private boolean enabled = true;

    private int ticks;

    public ImpetusGateBlockEntity(BlockPos pos, BlockState state) {
        super(IRBlockEntities.IMPETUS_GATE.get(), pos, state);
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

    public boolean isBlocking() {
        return !enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean value) {
        if (enabled == value) {
            return;
        }
        enabled = value;
        if (!enabled && buffer > 0L) {
            buffer = 0L;
        }
        setChanged();
        syncToClient();
    }

    @Override
    public boolean canReceive() {
        return !isBlocking() && getEnergyStored() < getMaxEnergyStored();
    }

    @Override
    public boolean canExtract() {
        return !isBlocking() && getEnergyStored() > 0;
    }

    @Override
    public long receiveEnergy(long maxReceive, boolean simulate) {
        if (isBlocking()) {
            return 0L;
        }
        long free = GATE_TRANSFER - buffer;
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
        if (isBlocking()) {
            return 0L;
        }
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
        return GATE_TRANSFER;
    }

    @Override
    public boolean hasRecentEnergy() {
        return level != null && (level.getGameTime() - lastEnergyGameTime) <= RECENT_ENERGY_WINDOW;
    }

    private void markRecentEnergy() {
        if (level != null) {
            lastEnergyGameTime = level.getGameTime();
        }
    }

    public static void staticTick(Level level, BlockPos pos, BlockState state, ImpetusGateBlockEntity gate) {
        gate.tick();
    }

    private void tick() {
        if (level == null || level.isClientSide()) {
            return;
        }
        if (++ticks % GATE_POLL_INTERVAL == 0) {
            setEnabled(!level.hasNeighborSignal(worldPosition));
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
        output.putLong("LastEnergyTime", lastEnergyGameTime);
        output.putBoolean("Enabled", enabled);
    }

    @Override
    protected void loadAdditional(@NonNull ValueInput input) {
        super.loadAdditional(input);
        lastEnergyGameTime = input.getLongOr("LastEnergyTime", -1L);
        enabled = input.getBooleanOr("Enabled", true);
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
