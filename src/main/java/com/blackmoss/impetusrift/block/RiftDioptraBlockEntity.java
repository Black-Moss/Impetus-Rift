package com.blackmoss.impetusrift.block;

import com.blackmoss.impetusrift.ImpetusRift;
import com.blackmoss.impetusrift.registry.IRBlockEntities;
import com.leclowndu93150.thaumaturge.content.entity.EntityFluxRift;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class RiftDioptraBlockEntity extends BlockEntity {
    public static final int MODE_SIZE = 0;
    public static final int MODE_STABILITY = 1;
    public static final int SCAN_RADIUS = 12;
    private static final int SCAN_INTERVAL = 20;
    private static final double STABILITY_SPAN = 200.0D;
    private static final double SIZE_FULL_SCALE = 100.0D;

    private int mode = MODE_SIZE;
    private int ticks;
    private int lastResult = -1;
    private @Nullable UUID targetId;
    private @Nullable EntityFluxRift cachedTarget;

    private boolean syncedHasTarget;
    private int syncedRiftSize;
    private float syncedRiftStability;

    public RiftDioptraBlockEntity(BlockPos pos, BlockState state) {
        super(IRBlockEntities.RIFT_DIOPTRA.get(), pos, state);
    }

    public static void staticTick(Level level, BlockPos pos, BlockState state, RiftDioptraBlockEntity monitor) {
        monitor.tick();
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int newMode) {
        if (mode != newMode) {
            mode = newMode;
            setChanged();
            syncToClient();
        }
    }

    public void cycleMode() {
        mode = mode == MODE_SIZE ? MODE_STABILITY : MODE_SIZE;
        setChanged();
        syncToClient();
    }

    public @Nullable UUID getTargetId() {
        return targetId;
    }

    public void setTargetId(@Nullable UUID id) {
        targetId = id;
        setChanged();
        syncToClient();
    }

    public int getClientTargetId() {
        return -1;
    }

    public int getTicks() {
        return ticks;
    }

    public int comparatorOutputFor(int riftSize, float riftStability) {
        double ratio = mode == MODE_STABILITY
                ? (riftStability + 100.0D) / STABILITY_SPAN
                : Math.min(riftSize / SIZE_FULL_SCALE, 1.0D);
        ratio = Math.clamp(ratio, 0.0D, 1.0D);
        return (int) (Math.floor(ratio * 14.0D) + Math.signum(ratio));
    }

    public int getLastResult() {
        return lastResult;
    }

    public void setLastResult(int value) {
        lastResult = value;
    }

    public AABB scanBox() {
        return new AABB(worldPosition).inflate(SCAN_RADIUS);
    }

    public @Nullable EntityFluxRift resolveTarget() {
        if (targetId == null || level == null) {
            return null;
        }
        EntityFluxRift cached = cachedTarget;
        if (cached != null && !cached.isRemoved() && targetId.equals(cached.getUUID())) {
            return cached;
        }
        for (EntityFluxRift rift : level.getEntitiesOfClass(EntityFluxRift.class, scanBox())) {
            if (targetId.equals(rift.getUUID())) {
                cachedTarget = rift;
                return rift;
            }
        }
        return null;
    }

    public void cycleTarget() {
        if (level == null) {
            return;
        }
        List<EntityFluxRift> rifts = level.getEntitiesOfClass(EntityFluxRift.class, scanBox());
        if (rifts.isEmpty()) {
            cachedTarget = null;
            setTargetId(null);
            return;
        }
        Vec3 center = Vec3.atCenterOf(worldPosition);
        rifts.sort(Comparator.comparingDouble(r -> r.distanceToSqr(center)));
        EntityFluxRift nearest = rifts.getFirst();
        cachedTarget = nearest;
        setTargetId(nearest.getUUID());
    }

    public boolean hasSyncedTarget() {
        return syncedHasTarget;
    }

    public int syncedRiftSize() {
        return syncedRiftSize;
    }

    public float syncedRiftStability() {
        return syncedRiftStability;
    }

    private void notifyComparator() {
        if (level != null && !level.isClientSide()) {
            level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
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
        output.putInt("Mode", mode);
        output.storeNullable("Target", UUIDUtil.CODEC, targetId);
    }

    @Override
    protected void loadAdditional(@NonNull ValueInput input) {
        super.loadAdditional(input);
        mode = input.getIntOr("Mode", MODE_SIZE);
        targetId = input.read("Target", UUIDUtil.CODEC).orElse(null);
        syncedHasTarget = input.getBooleanOr("HasTarget", false);
        syncedRiftSize = input.getIntOr("RiftSize", 0);
        syncedRiftStability = input.getFloatOr("RiftStability", 0.0F);
    }

    @Override
    public @NonNull CompoundTag getUpdateTag(HolderLookup.@NonNull Provider registries) {
        CompoundTag nbt = super.getUpdateTag(registries);
        try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(this.problemPath(), ImpetusRift.LOGGER)) {
            TagValueOutput output = TagValueOutput.createWithContext(reporter, registries);
            saveAdditional(output);
            output.putBoolean("HasTarget", syncedHasTarget);
            output.putInt("RiftSize", syncedRiftSize);
            output.putFloat("RiftStability", syncedRiftStability);
            nbt.merge(output.buildResult());
        }
        return nbt;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private void tick() {
        if (level == null || level.isClientSide()) {
            return;
        }
        if (++ticks % SCAN_INTERVAL != 0) {
            return;
        }

        EntityFluxRift rift = resolveTarget();
        if (rift == null) {
            cycleTarget();
            rift = resolveTarget();
        }

        int result = rift == null ? 0 : comparatorOutputFor(rift.getRiftSize(), rift.getRiftStability());
        if (result != lastResult) {
            lastResult = result;
            setChanged();
            notifyComparator();
            syncToClient();
        }

        boolean hasTarget = rift != null;
        int size = rift == null ? 0 : rift.getRiftSize();
        float stability = rift == null ? 0.0F : rift.getRiftStability();
        if (hasTarget != syncedHasTarget || size != syncedRiftSize || stability != syncedRiftStability) {
            syncedHasTarget = hasTarget;
            syncedRiftSize = size;
            syncedRiftStability = stability;
            setChanged();
            syncToClient();
        }
    }
}
