package com.blackmoss.impetusrift.registry;

import com.blackmoss.impetusrift.ImpetusRift;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@EventBusSubscriber(modid = ImpetusRift.MODID)
public final class IRCapabilities {
    private IRCapabilities() {
    }

    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.Energy.BLOCK,
                IRBlockEntities.IMPETUS_GENERATOR.get(),
                (generator, side) -> side == generator.outputFace()
                        ? generator
                        : null);
    }
}
