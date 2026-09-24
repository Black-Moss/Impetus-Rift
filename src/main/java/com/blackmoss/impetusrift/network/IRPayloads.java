package com.blackmoss.impetusrift.network;

import com.blackmoss.impetusrift.ImpetusRift;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = ImpetusRift.MODID)
public final class IRPayloads {
    private static final String VERSION = "1";

    private IRPayloads() {}

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(VERSION);
        registrar.playToClient(
                ClientboundConnectionsSyncPayload.TYPE,
                ClientboundConnectionsSyncPayload.STREAM_CODEC,
                ClientboundConnectionsSyncPayload::handle
        );
    }
}