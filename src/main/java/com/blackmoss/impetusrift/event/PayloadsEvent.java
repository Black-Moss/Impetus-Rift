package com.blackmoss.impetusrift.event;

import com.blackmoss.impetusrift.ImpetusRift;
import com.blackmoss.impetusrift.network.ClientboundConnectionsSyncPayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = ImpetusRift.MODID)
public final class PayloadsEvent {
    private static final String VERSION = "1";

    private PayloadsEvent() {
    }

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