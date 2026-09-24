package com.blackmoss.impetusrift.client.render;

import com.blackmoss.impetusrift.ImpetusRift;
import com.blackmoss.impetusrift.client.network.ClientConnectionCache;
import com.blackmoss.impetusrift.energy.network.ImpetusConnection;
import com.blackmoss.impetusrift.item.ImpetusLinkerItem;
import com.blackmoss.impetusrift.registry.IRDataComponents;
import com.leclowndu93150.thaumaturge.client.effect.pipeline.TCFXPipelines;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

@EventBusSubscriber(modid = ImpetusRift.MODID, value = Dist.CLIENT)
public final class LinkerWorldRenderer {

    private static final Identifier PIPELINE_LOC = ImpetusRift.IRIdentifier("pipeline/linker_overlay");
    private static final Identifier FRAME_TEX = ImpetusRift.IRIdentifier("textures/misc/frame.png");
    private static final RenderPipeline PIPELINE = TCFXPipelines.additiveTexturedNoDepth(PIPELINE_LOC);
    private static final RenderType OVERLAY_TYPE = RenderType.create("ir_linker_overlay",
            RenderSetup.builder(PIPELINE)
                    .withTexture("Sampler0", FRAME_TEX)
                    .createRenderSetup());

    private LinkerWorldRenderer() {
    }

    @SubscribeEvent
    static void registerPipelines(RegisterRenderPipelinesEvent event) {
        event.registerPipeline(PIPELINE);
    }

    @SubscribeEvent
    public static void onRender(RenderLevelStageEvent.AfterTranslucentBlocks event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        Level level = mc.level;
        if (level == null || player == null || mc.options.hideGui) return;

        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();

        boolean holdingLinker = mainHand.getItem() instanceof ImpetusLinkerItem
                || offHand.getItem() instanceof ImpetusLinkerItem;

        if (!holdingLinker) return;

        ItemStack linkerStack = mainHand.getItem() instanceof ImpetusLinkerItem ? mainHand : offHand;

        BlockPos boundSource = linkerStack.get(IRDataComponents.LINKER_BOUND_SOURCE.get());
        BlockPos highlight = linkerStack.get(IRDataComponents.LINKER_HIGHLIGHT_NODE.get());

        Object2IntOpenHashMap<BlockPos> frameColors = new Object2IntOpenHashMap<>();

        if (highlight != null) {
            frameColors.put(highlight, 0);
            for (ImpetusConnection conn : ClientConnectionCache.getConnectionsAt(highlight)) {
                if (conn.sourcePos().equals(highlight)) {
                    frameColors.mergeInt(conn.targetPos(), 2, LinkerWorldRenderer::mergeColor);
                } else {
                    frameColors.mergeInt(conn.sourcePos(), 1, LinkerWorldRenderer::mergeColor);
                }
            }
        }

        if (boundSource != null) {
            frameColors.put(boundSource, 0);
        }

        if (frameColors.isEmpty()) return;

        PoseStack poseStack = event.getPoseStack();
        Vec3 cam = mc.gameRenderer.getMainCamera().position();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        VertexConsumer buffer = buffers.getBuffer(OVERLAY_TYPE);
        Matrix4f matrix = poseStack.last().pose();

        for (var entry : frameColors.object2IntEntrySet()) {
            BlockPos pos = entry.getKey();
            int color = entry.getIntValue();

            if (pos.distSqr(player.blockPosition()) > 64 * 64) continue;

            float r, g, b;
            switch (color) {
                case 0:
                    r = 0.4F;
                    g = 0.6F;
                    b = 1.0F;
                    break;
                case 1:
                    r = 1.0F;
                    g = 0.3F;
                    b = 0.3F;
                    break;
                case 2:
                    r = 0.3F;
                    g = 1.0F;
                    b = 0.3F;
                    break;
                default:
                    r = 0.8F;
                    g = 0.3F;
                    b = 1.0F;
                    break;
            }

            double x = pos.getX() - cam.x;
            double y = pos.getY() - cam.y;
            double z = pos.getZ() - cam.z;
            renderBoxFrame(buffer, matrix, x, y, z, r, g, b);
        }

        buffers.endBatch(OVERLAY_TYPE);
    }

    private static int mergeColor(int existing, int add) {
        return existing | add;
    }

    private static void renderBoxFrame(
            VertexConsumer buffer, Matrix4f matrix,
            double x, double y, double z,
            float r, float g, float b) {
        float o = 0.002F;
        float x1 = (float) (x - o), y1 = (float) (y - o), z1 = (float) (z - o);
        float x2 = (float) (x + 1 + o), y2 = (float) (y + 1 + o), z2 = (float) (z + 1 + o);

        // 北面 (-Z)
        buffer.addVertex(matrix, x1, y1, z1).setUv(0, 0).setColor(r, g, b, 1.0F);
        buffer.addVertex(matrix, x2, y1, z1).setUv(1, 0).setColor(r, g, b, 1.0F);
        buffer.addVertex(matrix, x2, y2, z1).setUv(1, 1).setColor(r, g, b, 1.0F);
        buffer.addVertex(matrix, x1, y2, z1).setUv(0, 1).setColor(r, g, b, 1.0F);

        // 南面 (+Z)
        buffer.addVertex(matrix, x2, y1, z2).setUv(0, 0).setColor(r, g, b, 1.0F);
        buffer.addVertex(matrix, x1, y1, z2).setUv(1, 0).setColor(r, g, b, 1.0F);
        buffer.addVertex(matrix, x1, y2, z2).setUv(1, 1).setColor(r, g, b, 1.0F);
        buffer.addVertex(matrix, x2, y2, z2).setUv(0, 1).setColor(r, g, b, 1.0F);

        // 西面 (-X)
        buffer.addVertex(matrix, x1, y1, z2).setUv(0, 0).setColor(r, g, b, 1.0F);
        buffer.addVertex(matrix, x1, y1, z1).setUv(1, 0).setColor(r, g, b, 1.0F);
        buffer.addVertex(matrix, x1, y2, z1).setUv(1, 1).setColor(r, g, b, 1.0F);
        buffer.addVertex(matrix, x1, y2, z2).setUv(0, 1).setColor(r, g, b, 1.0F);

        // 东面 (+X)
        buffer.addVertex(matrix, x2, y1, z1).setUv(0, 0).setColor(r, g, b, 1.0F);
        buffer.addVertex(matrix, x2, y1, z2).setUv(1, 0).setColor(r, g, b, 1.0F);
        buffer.addVertex(matrix, x2, y2, z2).setUv(1, 1).setColor(r, g, b, 1.0F);
        buffer.addVertex(matrix, x2, y2, z1).setUv(0, 1).setColor(r, g, b, 1.0F);

        // 顶面 (+Y)
        buffer.addVertex(matrix, x1, y2, z1).setUv(0, 0).setColor(r, g, b, 1.0F);
        buffer.addVertex(matrix, x2, y2, z1).setUv(1, 0).setColor(r, g, b, 1.0F);
        buffer.addVertex(matrix, x2, y2, z2).setUv(1, 1).setColor(r, g, b, 1.0F);
        buffer.addVertex(matrix, x1, y2, z2).setUv(0, 1).setColor(r, g, b, 1.0F);

        // 底面 (-Y)
        buffer.addVertex(matrix, x1, y1, z2).setUv(0, 0).setColor(r, g, b, 1.0F);
        buffer.addVertex(matrix, x2, y1, z2).setUv(1, 0).setColor(r, g, b, 1.0F);
        buffer.addVertex(matrix, x2, y1, z1).setUv(1, 1).setColor(r, g, b, 1.0F);
        buffer.addVertex(matrix, x1, y1, z1).setUv(0, 1).setColor(r, g, b, 1.0F);
    }
}
