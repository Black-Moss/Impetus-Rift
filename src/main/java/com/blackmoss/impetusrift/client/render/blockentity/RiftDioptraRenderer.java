package com.blackmoss.impetusrift.client.render.blockentity;

import com.blackmoss.impetusrift.block.RiftDioptraBlockEntity;
import com.leclowndu93150.thaumaturge.api.items.GogglesAccess;
import com.leclowndu93150.thaumaturge.client.entity.FluxRiftRenderState;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class RiftDioptraRenderer implements BlockEntityRenderer<RiftDioptraBlockEntity, RiftDioptraRenderState> {
    private static final int POINTS = 12;
    private static final float CENTER_Y = 1.35F;
    private static final float RIFT_LENGTH = 0.75F;
    private static final float BASE_RADIUS = 0.035F;
    private static final float SIZE_RADIUS = 0.05F;
    private static final float PATH_SWAY = 0.12F;

    public RiftDioptraRenderer(BlockEntityRendererProvider.@NonNull Context context) {
    }

    @Override
    public @NonNull RiftDioptraRenderState createRenderState() {
        return new RiftDioptraRenderState();
    }

    @Override
    public void extractRenderState(
            @NonNull RiftDioptraBlockEntity dioptra,
            @NonNull RiftDioptraRenderState state,
            float partialTicks,
            @NonNull Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderState.extractBase(dioptra, state, breakProgress);

        FluxRiftRenderState rift = state.rift;
        rift.entityType = TTEntities.FLUX_RIFT.get();
        rift.stability = dioptra.syncedRiftStability();
        rift.animationTime = (Minecraft.getInstance().player == null ? 0 : Minecraft.getInstance().player.tickCount) + partialTicks;
        rift.goggles = GogglesAccess.wearsRevealingGear(Minecraft.getInstance().player);

        rift.points.clear();
        rift.widths.clear();
        if (!dioptra.hasSyncedTarget()) {
            return;
        }

        float size = Mth.clamp(dioptra.syncedRiftSize() / 60.0F, 0.2F, 1.0F);
        for (int a = 0; a < POINTS; a++) {
            float f = a / (float) (POINTS - 1);
            rift.points.add(new Vec3(
                    Mth.sin(f * Mth.TWO_PI * 1.5F) * PATH_SWAY * size,
                    (f - 0.5F) * RIFT_LENGTH * size,
                    Mth.cos(f * Mth.TWO_PI * 1.2F) * PATH_SWAY * size));
            rift.widths.add(BASE_RADIUS + SIZE_RADIUS * size * Mth.sin(f * Mth.PI));
        }
    }

    @Override
    public void submit(
            RiftDioptraRenderState state,
            @NonNull PoseStack poseStack,
            @NonNull SubmitNodeCollector collector,
            @NonNull CameraRenderState camera) {
        FluxRiftRenderState rift = state.rift;
        if (rift.points.size() <= 2) {
            return;
        }

        EntityRenderer<?, ? super FluxRiftRenderState> renderer =
                Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(rift);

        poseStack.pushPose();
        poseStack.translate(0.5F, CENTER_Y, 0.5F);
        renderer.submit(rift, poseStack, collector, camera);
        poseStack.popPose();
    }

    @Override
    public int getViewDistance() {
        return 64;
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public @NonNull AABB getRenderBoundingBox(RiftDioptraBlockEntity blockEntity) {
        BlockPos pos = blockEntity.getBlockPos();
        return new AABB(pos.getX() - 0.5, pos.getY(), pos.getZ() - 0.5,
                pos.getX() + 1.5, pos.getY() + 2.6, pos.getZ() + 1.5);
    }
}
