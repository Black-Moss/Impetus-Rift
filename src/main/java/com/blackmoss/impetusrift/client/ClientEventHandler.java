package com.blackmoss.impetusrift.client;

import com.blackmoss.impetusrift.ImpetusRift;
import com.blackmoss.impetusrift.client.model.RiftCompassAngleProperty;
import com.blackmoss.impetusrift.client.render.ImpetusDrainerRenderer;
import com.blackmoss.impetusrift.client.render.ImpetusMatrixRenderer;
import com.blackmoss.impetusrift.client.render.ImpetusNodeRenderer;
import com.blackmoss.impetusrift.client.render.RiftJarRenderer;
import com.blackmoss.impetusrift.registry.IRBlockEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterRangeSelectItemModelPropertyEvent;

@EventBusSubscriber(modid = ImpetusRift.MODID, value = Dist.CLIENT)
public class ClientEventHandler {
    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(IRBlockEntities.RIFT_JAR.get(), RiftJarRenderer::new);
        event.registerBlockEntityRenderer(IRBlockEntities.IMPETUS_MATRIX.get(), ImpetusMatrixRenderer::new);
        event.registerBlockEntityRenderer(IRBlockEntities.IMPETUS_DRAINER.get(), ImpetusDrainerRenderer::new);
        event.registerBlockEntityRenderer(IRBlockEntities.IMPETUS_GENERATOR.get(), ImpetusNodeRenderer::new);
    }

    @SubscribeEvent
    public static void onRegisterRangeSelectProperties(RegisterRangeSelectItemModelPropertyEvent event) {
        event.register(
                ImpetusRift.IRIdentifier("rift_compass_angle"),
                RiftCompassAngleProperty.MAP_CODEC
        );
    }
}
