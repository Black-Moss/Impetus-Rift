package com.blackmoss.impetusrift.client.render;

import com.blackmoss.impetusrift.block.ImpetusDrainerBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ImpetusDrainerRenderer implements BlockEntityRenderer<ImpetusDrainerBlockEntity, ImpetusDrainerRenderer.DrainerRenderState> {
    public ImpetusDrainerRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public @NonNull DrainerRenderState createRenderState() {
        return new DrainerRenderState();
    }

    @Override
    public void extractRenderState(
            @NonNull ImpetusDrainerBlockEntity blockEntity,
            @NonNull DrainerRenderState state,
            float partialTicks,
            @NonNull Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        state.beamTargets = blockEntity.getBeamTargetPositions();
        state.hasEnergy = blockEntity.getEnergyStored() > 0 || blockEntity.hasRecentEnergy();
        boolean active = state.hasEnergy
                && ImpetusEnergyBeamRenderer.hasReceivableTarget(
                blockEntity.getNodeLevel(), state.beamTargets);
        state.fade = ImpetusEnergyBeamRenderer.updateFade(blockEntity.getNodePos(), active);
        state.blockPos = blockEntity.getNodePos();
    }

    @Override
    public void submit(
            DrainerRenderState state,
            @NonNull PoseStack poseStack,
            @NonNull SubmitNodeCollector collector,
            @NonNull CameraRenderState camera) {
        if (state.fade > 0.0F && !state.beamTargets.isEmpty() && state.hasEnergy) {
            var beamSource = new ImpetusEnergyBeamRenderer.SimpleBeamSource(
                    state.blockPos, state.beamTargets, state.hasEnergy);
            ImpetusEnergyBeamRenderer.renderBeams(beamSource, poseStack, collector, 0, state.fade);
        }
    }

    @Override
    public int getViewDistance() {
        return 32;
    }

    @Override
    public @NonNull AABB getRenderBoundingBox(@NonNull ImpetusDrainerBlockEntity blockEntity) {
        return ImpetusEnergyBeamRenderer.beamRenderBox(
                blockEntity.getNodePos(), blockEntity.getBeamTargetPositions());
    }

    public static class DrainerRenderState extends BlockEntityRenderState {
        public List<BlockPos> beamTargets = new ArrayList<>();
        public boolean hasEnergy;
        public float fade;
        public BlockPos blockPos;
    }
}
