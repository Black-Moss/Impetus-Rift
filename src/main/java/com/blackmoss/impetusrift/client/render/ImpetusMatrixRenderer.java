package com.blackmoss.impetusrift.client.render;

import com.blackmoss.impetusrift.block.ImpetusMatrixBlockEntity;
import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.client.effect.pipeline.TCRenderPipelines;
import com.leclowndu93150.thaumaturge.client.entity.TCModelLayers;
import com.leclowndu93150.thaumaturge.client.model.entity.MatrixCubeModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class ImpetusMatrixRenderer implements BlockEntityRenderer<ImpetusMatrixBlockEntity, ImpetusMatrixRenderState> {
    private static final Identifier TEXTURE = TCIds.rl("textures/block/infuser_eldritch.png");
    private static final RenderType GLOW_TYPE = RenderType.create(
            "ir_matrix_glow",
            RenderSetup.builder(TCRenderPipelines.ENTITY_ADDITIVE_EMISSIVE)
                    .withTexture("Sampler0", TEXTURE)
                    .createRenderSetup());

    private static final float SUB_CUBE_OFFSET = 0.25F;
    private static final float SUB_CUBE_SCALE = 0.45F;
    private static final float TILT_X = 35.0F;
    private static final float TILT_Z = 45.0F;
    private static final float JITTER_SCALE = 0.01F;

    // 符文颜色
    private static final float RUNE_R = 0.8F;
    private static final float RUNE_G = 0.1F;
    private static final float RUNE_B = 1.0F;

    private final MatrixCubeModel model;

    public ImpetusMatrixRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new MatrixCubeModel(context.bakeLayer(TCModelLayers.MATRIX_CUBE));
    }

    @Override
    public @NonNull ImpetusMatrixRenderState createRenderState() {
        return new ImpetusMatrixRenderState();
    }

    @Override
    public void extractRenderState(
            @NonNull ImpetusMatrixBlockEntity matrix,
            @NonNull ImpetusMatrixRenderState state,
            float partialTicks,
            @NonNull Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(
                matrix,
                state,
                partialTicks,
                cameraPosition,
                breakProgress);

        var viewEntity = Minecraft.getInstance().getCameraEntity();
        state.animationTime = viewEntity == null
                ? partialTicks
                : viewEntity.tickCount + partialTicks;

        long maxEnergy = matrix.getEnergyStorage().getMaxEnergyStored();
        state.energyRatio = maxEnergy > 0
                ? (float) matrix.getEnergyStorage().getEnergyStored() / maxEnergy
                : 0.0F;
        state.stability = matrix.getStability();
        state.active = matrix.checkLocation();
        state.unstable = state.stability < 0.0F;
        state.startUp = matrix.clientStartUp;
        state.beamTargets = matrix.getBeamTargetPositions();
        state.blockPos = matrix.getBlockPos();
    }

    @Override
    public void submit(
            ImpetusMatrixRenderState state,
            @NonNull PoseStack poseStack,
            @NonNull SubmitNodeCollector collector,
            @NonNull CameraRenderState camera) {
        float instability = state.unstable
                ? Math.min(6.0F, 1.0F + (-state.stability * 0.66F))
                : 1.0F;

        float runeR, runeG, runeB;
        if (state.energyRatio <= 0.0F) {
            runeR = 0.8F;
            runeG = 0.1F;
            runeB = 0.1F;
        } else if (state.energyRatio >= 1.0F) {
            runeR = 0.1F;
            runeG = 0.8F;
            runeB = 0.1F;
        } else if (state.energyRatio >= 0.5F) {
            float t = (state.energyRatio - 0.5F) * 2.0F;
            runeR = Mth.lerp(t, 0.8F, 0.1F);
            runeG = Mth.lerp(t, 0.8F, 0.8F);
            runeB = Mth.lerp(t, 0.1F, 0.1F);
        } else {
            float t = state.energyRatio * 2.0F;
            runeR = Mth.lerp(t, 0.8F, 0.8F);
            runeG = Mth.lerp(t, 0.1F, 0.8F);
            runeB = Mth.lerp(t, 0.1F, 0.1F);
        }

        if (state.unstable) {
            float t = Mth.sin(state.animationTime / 8.0F) * 0.3F + 0.5F;
            runeR = Mth.lerp(t, runeR, 0.6F);
            runeG = Mth.lerp(t, runeG, 0.2F * 0.3F);
            runeB = Mth.lerp(t, runeB, 0.1F * 0.3F);
        }

        var cubeType = RenderTypes.entityCutout(TEXTURE);

        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);

        poseStack.mulPose(Axis.YP.rotationDegrees(state.animationTime % 360.0F * state.startUp));
        poseStack.mulPose(Axis.XP.rotationDegrees(TILT_X * state.startUp));
        poseStack.mulPose(Axis.ZP.rotationDegrees(TILT_Z * state.startUp));

        for (int a = 0; a < 2; a++) {
            for (int b = 0; b < 2; b++) {
                for (int c = 0; c < 2; c++) {
                    float jx = 0.0F;
                    float jy = 0.0F;
                    float jz = 0.0F;
                    if (state.active) {
                        jx = Mth.sin((state.animationTime + a * 10) / 15.0F) * JITTER_SCALE * state.startUp * instability;
                        jy = Mth.sin((state.animationTime + b * 10) / 14.0F) * JITTER_SCALE * state.startUp * instability;
                        jz = Mth.sin((state.animationTime + c * 10) / 13.0F) * JITTER_SCALE * state.startUp * instability;
                    }
                    int aa = a == 0 ? -1 : 1;
                    int bb = b == 0 ? -1 : 1;
                    int cc = c == 0 ? -1 : 1;

                    poseStack.pushPose();
                    poseStack.translate(
                            jx + aa * SUB_CUBE_OFFSET,
                            jy + bb * SUB_CUBE_OFFSET,
                            jz + cc * SUB_CUBE_OFFSET);

                    if (a > 0) {
                        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
                    }
                    if (b > 0) {
                        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
                    }
                    if (c > 0) {
                        poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
                    }

                    poseStack.scale(SUB_CUBE_SCALE, SUB_CUBE_SCALE, SUB_CUBE_SCALE);

                    collector.submitModelPart(model.cube, poseStack, cubeType, state.lightCoords, OverlayTexture.NO_OVERLAY, null, -1, null);

                    if (state.active || state.energyRatio > 0) {
                        float glowAlpha = (Mth.sin((state.animationTime + a * 2 + b * 3 + c * 4) / 4.0F) * 0.1F + 0.2F);
                        if (state.active) {
                            glowAlpha = Math.max(glowAlpha, 0.2F);
                        }
                        int glowColor = ARGB.colorFromFloat(glowAlpha, runeR, runeG, runeB);
                        collector.submitModelPart(
                                model.glow,
                                poseStack,
                                GLOW_TYPE,
                                LightCoordsUtil.FULL_BRIGHT,
                                OverlayTexture.NO_OVERLAY,
                                null,
                                glowColor,
                                null);
                    }

                    poseStack.popPose();
                }
            }
        }

        poseStack.popPose();

        if (!state.beamTargets.isEmpty() && state.energyRatio > 0) {
            var beamSource = new ImpetusEnergyBeamRenderer.SimpleBeamSource(
                    state.blockPos,
                    state.beamTargets,
                    true);
            ImpetusEnergyBeamRenderer.renderBeams(beamSource, poseStack, collector, 0);
        }
    }

    @Override
    public int getViewDistance() {
        return 32;
    }

    @Override
    public @NonNull AABB getRenderBoundingBox(@NonNull ImpetusMatrixBlockEntity blockEntity) {
        return ImpetusEnergyBeamRenderer.beamRenderBox(
                blockEntity.getBlockPos(), blockEntity.getBeamTargetPositions());
    }
}