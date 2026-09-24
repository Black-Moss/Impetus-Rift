package com.blackmoss.impetusrift.client.render;

import com.blackmoss.impetusrift.block.RiftJarBlockEntity;
import com.leclowndu93150.thaumaturge.client.effect.pipeline.TCRenderPipelines;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.AbstractEndPortalRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class RiftJarRenderer implements BlockEntityRenderer<RiftJarBlockEntity, RiftJarRenderState> {

    private static final RenderType RIFT_GLOW_TYPE = RenderType.create("ir_rift_jar_glow",
            RenderSetup.builder(TCRenderPipelines.RIFT_GLOW)
                    .withTexture("Sampler0", AbstractEndPortalRenderer.END_PORTAL_LOCATION)
                    .withTexture("Sampler1", AbstractEndPortalRenderer.END_PORTAL_LOCATION)
                    .createRenderSetup());

    private static final RenderType RIFT_SOLID_TYPE = RenderType.create("ir_rift_jar_solid",
            RenderSetup.builder(TCRenderPipelines.RIFT_SOLID)
                    .withTexture("Sampler0", AbstractEndPortalRenderer.END_PORTAL_LOCATION)
                    .withTexture("Sampler1", AbstractEndPortalRenderer.END_PORTAL_LOCATION)
                    .createRenderSetup());

    private static final int TUBE_SIDES = 6;
    private static final int GLOW_PASSES = 3;
    private static final float GLOW_RADIUS_BASE = 1.25F;
    private static final float GLOW_RADIUS_STEP = 0.5F;
    private static final float WOBBLE_AMPLITUDE = 0.05F;
    private static final float MAX_STAB_FACTOR = 1.5F;
    private static final float STAB_DIVISOR = 50.0F;
    private static final float TIME_OFFSET_PER_POINT = 10.0F;
    private static final float WOBBLE_X_PERIOD = 50.0F;
    private static final float WOBBLE_Y_PERIOD = 60.0F;
    private static final float WOBBLE_Z_PERIOD = 70.0F;
    private static final float WIDTH_PULSE_PERIOD = 8.0F;

    // Rift generation constants (same as EntityFluxRift)
    private static final double STEP_LENGTH = 0.2;
    private static final double TIP_LENGTH = 0.1;
    private static final float GIRTH_PER_SIZE = 1.0F / 300.0F;
    private static final float SIZE_PER_STEP = 3.0F;
    private static final float WANDER_ANGLE = 0.33F;

    public RiftJarRenderer(BlockEntityRendererProvider.Context context) {
    }

    private static void submitTube(
            SubmitNodeCollector collector,
            PoseStack poseStack,
            RenderType type,
            Vec3[] centers,
            float[] radii,
            float radiusScale) {
        collector.submitCustomGeometry(poseStack, type, (pose, buffer) -> {
            Vector3f[] previousRing = null;
            for (int a = 0; a < centers.length; a++) {
                Vec3 direction = segmentDirection(centers, a);
                Vector3f[] ring = buildRing(centers[a], direction, radii[a] * radiusScale);
                if (previousRing != null) {
                    for (int side = 0; side < TUBE_SIDES; side++) {
                        int next = (side + 1) % TUBE_SIDES;
                        addVertex(buffer, pose.pose(), previousRing[side]);
                        addVertex(buffer, pose.pose(), previousRing[next]);
                        addVertex(buffer, pose.pose(), ring[next]);
                        addVertex(buffer, pose.pose(), ring[side]);
                    }
                }
                previousRing = ring;
            }
        });
    }

    private static void addVertex(
            VertexConsumer buffer,
            Matrix4fc pose,
            Vector3f vertex) {
        buffer.addVertex(pose, vertex.x, vertex.y, vertex.z);
    }

    private static Vec3 segmentDirection(Vec3[] centers, int index) {
        Vec3 direction;
        if (index == 0) {
            direction = centers[1].subtract(centers[0]);
        } else if (index == centers.length - 1) {
            direction = centers[index].subtract(centers[index - 1]);
        } else {
            direction = centers[index + 1].subtract(centers[index - 1]);
        }
        return direction.lengthSqr() < 1.0E-6 ? new Vec3(0.0, 1.0, 0.0) : direction.normalize();
    }

    private static Vector3f[] buildRing(Vec3 center, Vec3 direction, float radius) {
        Vec3 reference = Math.abs(direction.y) < 0.99 ? new Vec3(0.0, 1.0, 0.0) : new Vec3(1.0, 0.0, 0.0);
        Vec3 u = direction.cross(reference).normalize();
        Vec3 v = direction.cross(u).normalize();
        Vector3f[] ring = new Vector3f[TUBE_SIDES];
        for (int side = 0; side < TUBE_SIDES; side++) {
            double angle = (Math.PI * 2.0 * side) / TUBE_SIDES;
            double cos = Math.cos(angle) * radius;
            double sin = Math.sin(angle) * radius;
            ring[side] = new Vector3f(
                    (float) (center.x + u.x * cos + v.x * sin),
                    (float) (center.y + u.y * cos + v.y * sin),
                    (float) (center.z + u.z * cos + v.z * sin)
            );
        }
        return ring;
    }

    private static void calcSteps(List<Vec3> outPoints, List<Float> outWidths, int size, int seed) {
        outPoints.clear();
        outWidths.clear();
        java.util.Random rand = new java.util.Random(seed);
        Vec3 heading = new Vec3(rand.nextGaussian(), rand.nextGaussian(), rand.nextGaussian()).normalize();
        Arm right = new Arm(heading);
        Arm left = new Arm(heading.scale(-1.0));
        int steps = Mth.ceil(size / SIZE_PER_STEP);
        float girth = size * GIRTH_PER_SIZE;
        float taper = girth / steps;
        for (int a = 0; a < steps; a++) {
            girth -= taper;
            right.wander(rand);
            outPoints.add(right.advance(STEP_LENGTH));
            outWidths.add(girth);
            left.wander(rand);
            outPoints.addFirst(left.advance(STEP_LENGTH));
            outWidths.addFirst(girth);
        }
        outPoints.add(right.advance(TIP_LENGTH));
        outWidths.add(0.0F);
        outPoints.addFirst(left.advance(TIP_LENGTH));
        outWidths.addFirst(0.0F);
    }

    @Override
    public @NonNull RiftJarRenderState createRenderState() {
        return new RiftJarRenderState();
    }

    @Override
    public void extractRenderState(
            @NonNull RiftJarBlockEntity blockEntity,
            @NonNull RiftJarRenderState state,
            float partialTicks,
            @NonNull Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        state.hasRift = blockEntity.hasRift();
        state.riftSeed = blockEntity.getRiftSeed();
        state.riftSize = blockEntity.getRiftSize();
        var viewEntity = Minecraft.getInstance().getCameraEntity();
        state.animationTime = viewEntity == null ? partialTicks : viewEntity.tickCount + partialTicks;
    }

    @Override
    public void submit(
            RiftJarRenderState state,
            @NonNull PoseStack poseStack,
            @NonNull SubmitNodeCollector collector,
            @NonNull CameraRenderState camera) {
        if (!state.hasRift || state.riftSize <= 0) {
            return;
        }

        List<Vec3> points = new ArrayList<>();
        List<Float> widths = new ArrayList<>();
        calcSteps(points, widths, state.riftSize, state.riftSeed);

        int count = points.size();
        if (count <= 2) {
            return;
        }
        Vec3[] centers = new Vec3[count];
        float[] radii = new float[count];
        for (int a = 0; a < count; a++) {
            float time = state.animationTime;
            if (a > count / 2) {
                time -= a * TIME_OFFSET_PER_POINT;
            } else if (a < count / 2) {
                time += a * TIME_OFFSET_PER_POINT;
            }
            Vec3 p = points.get(a);
            centers[a] = new Vec3(
                    p.x + Math.sin(time / WOBBLE_X_PERIOD) * WOBBLE_AMPLITUDE,
                    p.y + Math.sin(time / WOBBLE_Y_PERIOD) * WOBBLE_AMPLITUDE,
                    p.z + Math.sin(time / WOBBLE_Z_PERIOD) * WOBBLE_AMPLITUDE
            );
            double pulse = 1.0 - Math.sin(time / WIDTH_PULSE_PERIOD) * 0.1F;
            radii[a] = (float) (widths.get(a) * pulse);
        }

        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, minZ = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE, maxZ = -Double.MAX_VALUE;
        for (Vec3 p : points) {
            minX = Math.min(minX, p.x);
            minY = Math.min(minY, p.y);
            minZ = Math.min(minZ, p.z);
            maxX = Math.max(maxX, p.x);
            maxY = Math.max(maxY, p.y);
            maxZ = Math.max(maxZ, p.z);
        }

        double halfWidthX = Math.max(Math.abs(minX), Math.abs(maxX)) + 0.075;
        double halfWidthY = Math.max(Math.abs(minY), Math.abs(maxY)) + 0.075;
        double halfWidthZ = Math.max(Math.abs(minZ), Math.abs(maxZ)) + 0.075;
        float scaleX = (float) (0.3125 / halfWidthX);
        float scaleY = (float) (0.375 / halfWidthY);
        float scaleZ = (float) (0.3125 / halfWidthZ);

        poseStack.pushPose();
        poseStack.translate(0.5, 0.375, 0.5);
        poseStack.scale(scaleX, scaleY, scaleZ);

        for (int pass = 0; pass <= GLOW_PASSES; pass++) {
            RenderType type;
            float radiusScale;
            if (pass < GLOW_PASSES) {
                type = RIFT_GLOW_TYPE;
                radiusScale = GLOW_RADIUS_BASE + GLOW_RADIUS_STEP * pass;
            } else {
                type = RIFT_SOLID_TYPE;
                radiusScale = 1.0F;
            }
            submitTube(collector, poseStack, type, centers, radii, radiusScale);
        }

        poseStack.popPose();
    }

    @Override
    public int getViewDistance() {
        return 64;
    }

    private static final class Arm {
        private Vec3 heading;
        private Vec3 tip = Vec3.ZERO;

        Arm(Vec3 heading) {
            this.heading = heading;
        }

        void wander(java.util.Random rand) {
            heading = heading.xRot((float) (rand.nextGaussian() * WANDER_ANGLE))
                    .yRot((float) (rand.nextGaussian() * WANDER_ANGLE));
        }

        Vec3 advance(double distance) {
            tip = tip.add(heading.scale(distance));
            return tip;
        }
    }
}