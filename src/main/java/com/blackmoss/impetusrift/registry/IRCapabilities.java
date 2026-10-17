package com.blackmoss.impetusrift.registry;

import com.blackmoss.impetusrift.ImpetusRift;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaCapabilities;
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

        event.registerBlockEntity(
                EssentiaCapabilities.STORAGE,
                IRBlockEntities.RIFT_FEEDER.get(),
                (feeder, side) -> feeder);
        event.registerBlockEntity(
                EssentiaCapabilities.TRANSPORT,
                IRBlockEntities.RIFT_FEEDER.get(),
                (feeder, side) -> feeder);
    }
}
