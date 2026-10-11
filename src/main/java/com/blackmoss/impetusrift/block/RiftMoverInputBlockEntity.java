package com.blackmoss.impetusrift.block;

import com.blackmoss.impetusrift.ImpetusRift;
import com.blackmoss.impetusrift.registry.IRBlockEntities;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.casters.IInteractWithCaster;
import com.leclowndu93150.thaumaturge.content.entity.EntityFluxRift;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;
import java.util.List;

public class RiftMoverInputBlockEntity extends BlockEntity implements IInteractWithCaster {
    private static final float VIS_PER_STEP = 0.25F;
    private static final int OPERATION_INTERVAL = 10;
    private static final float STABILITY_PENALTY_PER_STEP = 0.5F;
    private static final double RIFT_OFFSET_Y = 3.5;
    private static final double ALIGN_TOLERANCE = 0.5;

    private boolean operating = false;
    private EntityFluxRift rift;
    private int oldSeed;
    private int oldSize;
    private float oldStability;
    private int ticks = 0;

    public RiftMoverInputBlockEntity(BlockPos pos, BlockState state) {
        super(IRBlockEntities.RIFT_MOVER_INPUT.get(), pos, state);
    }

    public static void staticTick(
            Level level,
            BlockPos pos,
            BlockState state,
            RiftMoverInputBlockEntity mover) {
        mover.tick();
    }

    private void tick() {
        if (level == null || level.isClientSide() || !operating) {
            return;
        }

        ticks++;
        if (ticks % OPERATION_INTERVAL != 0) {
            return;
        }

        RiftJarBlockEntity jar = getJarBelow();
        if (rift == null
                || rift.isRemoved()
                || rift.getRiftSize() < 1
                || rift.getCollapse()
                || jar == null
                || jar.hasRift()) {
            failOperation();
            return;
        }

        snapRiftAbove();

        if (AuraHelper.drainVis(level, worldPosition, VIS_PER_STEP, false) >= VIS_PER_STEP - 0.0001F) {
            rift.setRiftSize(rift.getRiftSize() - 1);
            rift.setRiftStability(rift.getRiftStability() - STABILITY_PENALTY_PER_STEP);
            if (rift.getRiftSize() <= 0) {
                jar.sealRift(oldSeed, oldSize);
                completeOperation();
            }
        }
    }

    public void tryStartOperation() {
        if (level == null || level.isClientSide() || operating) {
            return;
        }

        RiftJarBlockEntity jar = getJarBelow();
        if (jar == null || jar.hasRift()) {
            return;
        }

        EntityFluxRift found = findRift();
        if (found == null || found.isRemoved() || found.getRiftSize() < 1 || found.getCollapse()) {
            return;
        }

        operating = true;
        rift = found;
        oldSeed = found.getRiftSeed();
        oldSize = found.getRiftSize();
        oldStability = found.getRiftStability();
        ticks = 0;
        level.playSound(
                null,
                worldPosition,
                TTSounds.CRAFTSTART.get(),
                SoundSource.BLOCKS,
                0.5F,
                1.0F);
        setChanged();
        syncToClient();
    }

    private void snapRiftAbove() {
        if (rift == null) {
            return;
        }
        rift.snapTo(
                worldPosition.getX() + 0.5,
                worldPosition.getY() + RIFT_OFFSET_Y,
                worldPosition.getZ() + 0.5,
                0.0F,
                0.0F);
        rift.refreshDimensions();
    }

    @Nullable
    private RiftJarBlockEntity getJarBelow() {
        if (level == null) {
            return null;
        }
        BlockEntity below = level.getBlockEntity(worldPosition.below());
        return below instanceof RiftJarBlockEntity jar
                ? jar
                : null;
    }

    @Nullable
    private EntityFluxRift findRift() {
        if (level == null) {
            return null;
        }
        double cx = worldPosition.getX() + 0.5;
        double cz = worldPosition.getZ() + 0.5;
        AABB area = new AABB(
                worldPosition.getX() - 1,
                worldPosition.getY() + 1,
                worldPosition.getZ() - 1,
                worldPosition.getX() + 2,
                worldPosition.getY() + 7,
                worldPosition.getZ() + 2);
        List<EntityFluxRift> rifts = level.getEntitiesOfClass(EntityFluxRift.class, area);
        if (rifts.isEmpty()) {
            return null;
        }
        rifts.sort(Comparator.comparingDouble(r -> r.distanceToSqr(cx, r.getY(), cz)));
        for (EntityFluxRift r : rifts) {
            if (Math.abs(r.getX() - cx) <= ALIGN_TOLERANCE && Math.abs(r.getZ() - cz) <= ALIGN_TOLERANCE) {
                return r;
            }
        }
        return null;
    }

    private void failOperation() {
        if (level == null) {
            operating = false;
            return;
        }
        if (rift == null || rift.isRemoved()) {
            AuraHelper.polluteAura(level, worldPosition, oldSize, true);
        } else {
            if (rift.getCollapse()) {
                rift.setCollapse(false);
            }
            rift.setRiftStability(-150.0F);
            rift.playSound(SoundEvents.GENERIC_EXPLODE.value(), 1.0F, 0.75F);
            rift = null;
        }
        operating = false;
        level.playSound(
                null,
                worldPosition,
                TTSounds.CRAFTFAIL.get(),
                SoundSource.BLOCKS,
                0.5F,
                1.0F);
        setChanged();
        syncToClient();
    }

    private void completeOperation() {
        rift = null;
        operating = false;
        if (level != null) {
            level.playSound(
                    null,
                    worldPosition,
                    TTSounds.WAND.get(),
                    SoundSource.BLOCKS,
                    0.5F,
                    1.0F);
        }
        setChanged();
        syncToClient();
    }

    @Override
    public boolean onCasterRightClick(
            Level level,
            ItemStack casterStack,
            Player player,
            BlockPos pos,
            Direction side,
            InteractionHand hand) {
        if (level instanceof ServerLevel) {
            tryStartOperation();
        }
        return true;
    }

    public boolean isOperating() {
        return operating;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide() && operating && rift == null) {
            rift = findRift();
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
        output.putBoolean("Operating", operating);
        output.putInt("OldSeed", oldSeed);
        output.putInt("OldSize", oldSize);
        output.putFloat("OldStability", oldStability);
    }

    @Override
    protected void loadAdditional(@NonNull ValueInput input) {
        super.loadAdditional(input);
        operating = input.getBooleanOr("Operating", false);
        oldSeed = input.getIntOr("OldSeed", 0);
        oldSize = input.getIntOr("OldSize", 0);
        oldStability = input.getFloatOr("OldStability", 0.0F);
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
