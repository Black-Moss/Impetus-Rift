package com.blackmoss.impetusrift.client.render;

import com.blackmoss.impetusrift.ImpetusRift;
import com.blackmoss.impetusrift.energy.network.IImpetusNode;
import com.blackmoss.impetusrift.energy.network.ImpetusNodePorts;
import com.leclowndu93150.thaumaturge.client.effect.pipeline.TCFXPipelines;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import org.joml.Matrix4fc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = ImpetusRift.MODID, value = Dist.CLIENT)
public class ImpetusEnergyBeamRenderer {
    public static final float BEAM_R = 0.3F;
    public static final float BEAM_G = 0.1F;
    public static final float BEAM_B = 0.8F;
    public static final float BEAM_A = 1.0F;
    public static final float BEAM_WIDTH = 0.3F;
    private static final float BREATHE_AMPLITUDE = 0.15F;
    private static final float BREATHE_SPEED = 0.15F;
    private static final float FADE_IN_STEP = 0.03F;
    private static final float FADE_OUT_STEP = 0.05F;
    private static final Map<BlockPos, BeamFadeState> FADE_STATES = new HashMap<>();

    private static final Identifier IMPETUS_BEAM_TEX = ImpetusRift.identifier("textures/misc/impetus_energy_beam.png");
    private static final Identifier PIPELINE_LOC = ImpetusRift.identifier("pipeline/impetus_energy_beam");
    private static final RenderPipeline PIPELINE = TCFXPipelines.translucentTextured(PIPELINE_LOC);
    public static final RenderType BEAM_TYPE = RenderType.create(
            "ir_impetus_energy_beam",
            RenderSetup.builder(PIPELINE)
                    .withTexture("Sampler0", IMPETUS_BEAM_TEX)
                    .createRenderSetup());

    @SubscribeEvent
    static void registerPipelines(RegisterRenderPipelinesEvent event) {
        event.registerPipeline(PIPELINE);
    }

    public static void renderBeams(
            IImpetusNode sourceNode,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            float partialTick) {
        renderBeams(sourceNode, poseStack, collector, partialTick, 1.0F);
    }

    public static void renderBeams(
            IImpetusNode sourceNode,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            float partialTick,
            float fade) {

        if (fade <= 0.0F) return;

        List<BlockPos> targets = sourceNode.getBeamTargetPositions();
        if (targets == null || targets.isEmpty()) return;

        BlockPos sourcePos = sourceNode.getNodePos();
        Vec3 origin = new Vec3(0.5, 0.5, 0.5);
        Vec3 sourceMin = Vec3.atLowerCornerOf(sourcePos);
        Vec3 cameraRelative = currentCameraPosition().subtract(sourceMin);
        Level level = Minecraft.getInstance().level;

        for (BlockPos targetPos : targets) {
            if (fade >= 1.0F && level != null
                    && level.getBlockEntity(targetPos) instanceof IImpetusNode target
                    && !target.canReceive()) {
                continue;
            }
            Vec3 targetRelative = Vec3.atCenterOf(targetPos).subtract(sourceMin);
            renderBeam(poseStack, collector, origin, targetRelative, cameraRelative, partialTick, fade);
        }
    }

    public static AABB beamRenderBox(BlockPos sourcePos, List<BlockPos> targets) {
        AABB box = new AABB(sourcePos);
        if (targets != null) {
            for (BlockPos target : targets) {
                box = box.minmax(new AABB(target));
            }
        }
        return box.inflate(1.0);
    }

    private static Vec3 currentCameraPosition() {
        var camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        return camera.position();
    }

    private static void renderBeam(
            PoseStack poseStack,
            SubmitNodeCollector collector,
            Vec3 from,
            Vec3 to,
            Vec3 cameraRelative,
            float partialTick,
            float fade) {

        collector.submitCustomGeometry(poseStack, BEAM_TYPE, (pose, buffer) ->
                renderBeamQuads(buffer, pose.pose(), from, to, cameraRelative, partialTick, fade));
    }

    public static void renderBeamImmediate(
            VertexConsumer buffer,
            Matrix4fc matrix,
            Vec3 from,
            Vec3 to,
            float partialTick) {

        renderBeamQuads(buffer, matrix, from, to, currentCameraPosition(), partialTick, 1.0F);
    }

    public static float updateFade(BlockPos pos, boolean active) {
        BeamFadeState state = FADE_STATES.computeIfAbsent(pos, k -> new BeamFadeState());
        long now = System.currentTimeMillis();
        if (state.lastTime < 0) {
            state.lastTime = now;
            return state.fade;
        }
        long deltaMs = Math.min(200L, now - state.lastTime);
        state.lastTime = now;
        float deltaTicks = deltaMs / 50.0F;
        if (active) {
            state.fade = Math.min(1.0F, state.fade + FADE_IN_STEP * deltaTicks);
        } else {
            state.fade = Math.max(0.0F, state.fade - FADE_OUT_STEP * deltaTicks);
            if (state.fade <= 0.0F) {
                FADE_STATES.remove(pos);
            }
        }
        return state.fade;
    }

    public static boolean hasReceivableTarget(Level level, List<BlockPos> targets) {
        if (level == null || targets == null) return false;
        for (BlockPos target : targets) {
            if (level.getBlockEntity(target) instanceof IImpetusNode node && node.canReceive()) {
                return true;
            }
        }
        return false;
    }

    private static Vec3 perpendicular(Vec3 dir) {
        Vec3 up = new Vec3(0, 1, 0);
        if (Math.abs(dir.dot(up)) > 0.99) {
            up = new Vec3(1, 0, 0);
        }
        return dir.cross(up).normalize();
    }

    private static void renderBeamQuads(
            VertexConsumer buffer,
            Matrix4fc matrix,
            Vec3 from,
            Vec3 to,
            Vec3 cameraRelative,
            float partialTick,
            float fade) {

        Vec3 direction = to.subtract(from);
        float length = (float) direction.length();
        if (length < 0.01F) return;

        Vec3 beamDir = direction.scale(1.0 / length);

        Vec3 toCamera = cameraRelative.subtract(from);
        Vec3 right;
        if (toCamera.lengthSqr() < 1.0E-6) {
            right = perpendicular(beamDir);
        } else {
            Vec3 cross = beamDir.cross(toCamera.normalize());
            right = cross.lengthSqr() < 1.0E-6 ? perpendicular(beamDir) : cross.normalize();
        }

        float tick = Minecraft.getInstance().player != null
                ? Minecraft.getInstance().player.tickCount : 0;
        float time = tick + partialTick;
        float scroll = -time * 0.2F;

        float sx = (float) from.x;
        float sy = (float) from.y;
        float sz = (float) from.z;
        float ex = (float) to.x;
        float ey = (float) to.y;
        float ez = (float) to.z;
        float rx = (float) right.x;
        float ry = (float) right.y;
        float rz = (float) right.z;

        float breathe = 1.0F + BREATHE_AMPLITUDE * (float) Math.sin(time * BREATHE_SPEED);
        float w = BEAM_WIDTH * fade * breathe;
        float alpha = BEAM_A * fade;
        float v0 = -1.0F + scroll;
        float v1 = length + v0;

        // 正面
        buffer.addVertex(matrix, sx - rx * w, sy - ry * w, sz - rz * w)
                .setUv(v0, 1.0F).setColor(BEAM_R, BEAM_G, BEAM_B, alpha);
        buffer.addVertex(matrix, ex - rx * w, ey - ry * w, ez - rz * w)
                .setUv(v1, 1.0F).setColor(BEAM_R, BEAM_G, BEAM_B, alpha);
        buffer.addVertex(matrix, ex + rx * w, ey + ry * w, ez + rz * w)
                .setUv(v1, 0.0F).setColor(BEAM_R, BEAM_G, BEAM_B, alpha);
        buffer.addVertex(matrix, sx + rx * w, sy + ry * w, sz + rz * w)
                .setUv(v0, 0.0F).setColor(BEAM_R, BEAM_G, BEAM_B, alpha);

        // 背面
        buffer.addVertex(matrix, sx + rx * w, sy + ry * w, sz + rz * w)
                .setUv(v0, 0.0F).setColor(BEAM_R, BEAM_G, BEAM_B, alpha);
        buffer.addVertex(matrix, ex + rx * w, ey + ry * w, ez + rz * w)
                .setUv(v1, 0.0F).setColor(BEAM_R, BEAM_G, BEAM_B, alpha);
        buffer.addVertex(matrix, ex - rx * w, ey - ry * w, ez - rz * w)
                .setUv(v1, 1.0F).setColor(BEAM_R, BEAM_G, BEAM_B, alpha);
        buffer.addVertex(matrix, sx - rx * w, sy - ry * w, sz - rz * w)
                .setUv(v0, 1.0F).setColor(BEAM_R, BEAM_G, BEAM_B, alpha);
    }

    private static final class BeamFadeState {
        float fade = 0.0F;
        long lastTime = -1L;
    }

    public record SimpleBeamSource(
            BlockPos nodePos,
            List<BlockPos> beamTargets,
            boolean canExtract) implements IImpetusNode {

        @Override
        public ImpetusNodePorts getPorts() {
            return ImpetusNodePorts.NONE; // 仅用于渲染，不参与连接
        }

        @Override
        public BlockPos getNodePos() {
            return nodePos;
        }

        @Override
        public Level getNodeLevel() {
            return null;
        }

        @Override
        public long receiveEnergy(long m, boolean s) {
            return 0;
        }

        @Override
        public long extractEnergy(long m, boolean s) {
            return 0;
        }

        @Override
        public long getEnergyStored() {
            return 0;
        }

        @Override
        public long getMaxEnergyStored() {
            return 0;
        }

        @Override
        public boolean canExtract() {
            return canExtract;
        }

        @Override
        public List<BlockPos> getBeamTargetPositions() {
            return beamTargets;
        }
    }
}
