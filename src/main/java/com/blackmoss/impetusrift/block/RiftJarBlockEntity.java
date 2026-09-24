package com.blackmoss.impetusrift.block;

import com.blackmoss.impetusrift.ImpetusRift;
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

public class RiftJarBlockEntity extends BlockEntity {
    private static final int STABILITY_RECOVERY_RATE = 1;
    private static final int STABILITY_RECOVERY_DELAY = 100;
    private boolean hasRift = false;
    private int riftSeed = 0;
    private int riftStability = 0;
    private int riftSize = 0;
    private long lastStabilityUpdate = 0;

    public RiftJarBlockEntity(BlockPos pos, BlockState state) {
        super(IRBlockEntities.RIFT_JAR.get(), pos, state);
    }

    public static void staticTick(
            Level level,
            BlockPos pos,
            BlockState state,
            RiftJarBlockEntity riftJar) {
        riftJar.tick();
    }

    private void tick() {
        if (level == null || level.isClientSide()) {
            return;
        }

        if (hasRift) {
            long currentTime = level.getGameTime();
            if (currentTime - lastStabilityUpdate > STABILITY_RECOVERY_DELAY) {
                if (riftStability < 0) {
                    riftStability = Math.min(0, riftStability + STABILITY_RECOVERY_RATE);
                    setChanged();
                } else if (riftStability > 0) {
                    riftStability = Math.max(0, riftStability - STABILITY_RECOVERY_RATE);
                    setChanged();
                }
            }

            if (riftStability < -100) {
                removeRift();
            }
        }
    }

    public void sealRift(int seed, int size) {
        if (hasRift) {
            return;
        }

        this.hasRift = true;
        this.riftSeed = seed;
        this.riftSize = size;
        this.riftStability = 0;
        this.lastStabilityUpdate = level != null ? level.getGameTime() : 0;

        setChanged();
        syncToClient();
    }

    @Nullable
    public RiftData releaseRift() {
        if (!hasRift) {
            return null;
        }

        RiftData data = new RiftData(riftSeed, riftSize);
        this.hasRift = false;
        this.riftSeed = 0;
        this.riftSize = 0;
        this.riftStability = 0;
        this.lastStabilityUpdate = 0;

        setChanged();
        syncToClient();
        return data;
    }

    public void removeRift() {
        this.hasRift = false;
        this.riftSeed = 0;
        this.riftSize = 0;
        this.riftStability = 0;
        this.lastStabilityUpdate = 0;

        setChanged();
        syncToClient();
    }

    public void decreaseStability(int amount) {
        if (!hasRift) {
            return;
        }

        this.riftStability -= amount;
        this.lastStabilityUpdate = level != null ? level.getGameTime() : 0;

        setChanged();
        syncToClient();
    }

    public int stabilizeRift(int amount) {
        if (!hasRift) {
            return 0;
        }

        int oldStability = riftStability;
        riftStability = Math.min(100, riftStability + amount);
        this.lastStabilityUpdate = level != null ? level.getGameTime() : 0;

        setChanged();
        syncToClient();
        return riftStability - oldStability;
    }

    public boolean hasRift() {
        return hasRift;
    }

    public int getRiftSeed() {
        return riftSeed;
    }

    public int getRiftStability() {
        return riftStability;
    }

    public int getRiftSize() {
        return riftSize;
    }

    public boolean isStable() {
        return riftStability >= 0;
    }

    public boolean isUnstable() {
        return riftStability < 0;
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
        output.putInt("HasRift", hasRift ? 1 : 0);
        output.putInt("RiftSeed", riftSeed);
        output.putInt("RiftStability", riftStability);
        output.putInt("RiftSize", riftSize);
        output.putLong("LastStabilityUpdate", lastStabilityUpdate);
    }

    @Override
    protected void loadAdditional(@NonNull ValueInput input) {
        super.loadAdditional(input);
        hasRift = input.getIntOr("HasRift", 0) == 1;
        riftSeed = input.getIntOr("RiftSeed", 0);
        riftStability = input.getIntOr("RiftStability", 0);
        riftSize = input.getIntOr("RiftSize", 0);
        lastStabilityUpdate = input.getLongOr("LastStabilityUpdate", 0);
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

    public record RiftData(int seed, int size) {
    }
}