package com.blackmoss.impetusrift.client;

import com.blackmoss.impetusrift.ImpetusRift;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = ImpetusRift.MODID, value = Dist.CLIENT)
public final class IRHudEvents {
    private IRHudEvents() {
    }

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.EXPERIENCE_LEVEL,
                ImpetusRift.identifier("impetus_hud"), new ImpetusHudOverlay());
    }
}
