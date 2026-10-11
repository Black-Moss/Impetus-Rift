package com.blackmoss.impetusrift.block;

import com.blackmoss.impetusrift.ImpetusRift;
import com.blackmoss.impetusrift.registry.IRBlockEntities;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.casters.IInteractWithCaster;
import com.leclowndu93150.thaumaturge.content.entity.EntityFluxRift;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySpawnReason;
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

public class RiftMoverOutputBlockEntity extends BlockEntity implements IInteractWithCaster {
    private static final double EXCLUSION_RANGE = 32.0;
    private static final float VIS_PER_STEP = 0.25F;
    private static final int OPERATION_INTERVAL = 10;
    private static final double RIFT_OFFSET_Y = 6.5;

    private boolean operating = false;
    private EntityFluxRift rift;
    private int targetSize;
    private int lastSize;
    private int ticks = 0;

    public RiftMoverOutputBlockEntity(BlockPos pos, BlockState state) {
        super(IRBlockEntities.RIFT_MOVER_OUTPUT.get(), pos, state);
    }

    public static void staticTick(
            Level level,
            BlockPos pos,
            BlockState state,
            RiftMoverOutputBlockEntity mover) {
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
                || rift.getRiftSize() < lastSize
                || jar == null) {
            failOperation();
            return;
        }

        snapRiftAbove();

        if (AuraHelper.drainVis(level, worldPosition, VIS_PER_STEP, false) >= VIS_PER_STEP - 0.0001F) {
            rift.setRiftSize(rift.getRiftSize() + 1);
            lastSize = rift.getRiftSize();
            if (rift.getRiftSize() >= targetSize) {
                completeOperation();
            }
        }
    }

    public void tryStartOperation() {
        if (level == null || level.isClientSide() || operating) {
            return;
        }

        RiftJarBlockEntity jar = getJarBelow();
        if (jar == null || !jar.hasRift()) {
            return;
        }

        if (hasNearbyRift()) {
            return;
        }

        RiftJarBlockEntity.RiftData data = jar.releaseRift();
        if (data == null) {
            return;
        }

        EntityFluxRift created = TTEntities.FLUX_RIFT.get().create(level, EntitySpawnReason.EVENT);
        if (created == null) {
            AuraHelper.polluteAura(level, worldPosition, data.size(), true);
            return;
        }

        created.setRiftSeed(data.seed());
        created.setRiftSize(1);
        created.snapTo(
                worldPosition.getX() + 0.5,
                worldPosition.getY() + RIFT_OFFSET_Y,
                worldPosition.getZ() + 0.5,
                0.0F,
                0.0F);
        created.refreshDimensions();
        if (!level.addFreshEntity(created)) {
            AuraHelper.polluteAura(level, worldPosition, data.size(), true);
            return;
        }

        operating = true;
        rift = created;
        targetSize = data.size();
        lastSize = 1;
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

    private boolean hasNearbyRift() {
        if (level == null) {
            return true;
        }
        List<EntityFluxRift> rifts = level.getEntitiesOfClass(EntityFluxRift.class,
                new AABB(worldPosition).inflate(EXCLUSION_RANGE));
        return !rifts.isEmpty();
    }

    @Nullable
    private EntityFluxRift findRift() {
        if (level == null) {
            return null;
        }
        List<EntityFluxRift> rifts = level.getEntitiesOfClass(EntityFluxRift.class,
                new AABB(worldPosition).inflate(EXCLUSION_RANGE));
        if (rifts.isEmpty()) {
            return null;
        }
        rifts.sort(Comparator.comparingDouble(r ->
                r.distanceToSqr(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5)));
        return rifts.getFirst();
    }

    private void failOperation() {
        if (level == null) {
            operating = false;
            return;
        }
        if (rift != null) {
            if (rift.getCollapse()) {
                rift.setCollapse(false);
            }
            rift.setRiftSize(0);
        }
        AuraHelper.polluteAura(level, worldPosition, targetSize, true);
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
        output.putInt("TargetSize", targetSize);
        output.putInt("LastSize", lastSize);
    }

    @Override
    protected void loadAdditional(@NonNull ValueInput input) {
        super.loadAdditional(input);
        operating = input.getBooleanOr("Operating", false);
        targetSize = input.getIntOr("TargetSize", 0);
        lastSize = input.getIntOr("LastSize", 0);
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
