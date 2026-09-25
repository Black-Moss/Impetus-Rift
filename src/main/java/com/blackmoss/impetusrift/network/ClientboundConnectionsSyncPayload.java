package com.blackmoss.impetusrift.network;

import com.blackmoss.impetusrift.ImpetusRift;
import com.blackmoss.impetusrift.client.network.ClientConnectionCache;
import com.blackmoss.impetusrift.energy.network.ImpetusConnection;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jspecify.annotations.NonNull;

import java.util.List;

public record ClientboundConnectionsSyncPayload(List<ImpetusConnection> connections) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ClientboundConnectionsSyncPayload> TYPE =
            new CustomPacketPayload.Type<>(ImpetusRift.IRIdentifier("connections_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundConnectionsSyncPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ImpetusConnection.STREAM_CODEC.apply(ByteBufCodecs.list()),
                    ClientboundConnectionsSyncPayload::connections,
                    ClientboundConnectionsSyncPayload::new
            );

    public static void handle(ClientboundConnectionsSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientConnectionCache.update(payload.connections()));
    }

    @Override
    public CustomPacketPayload.@NonNull Type<ClientboundConnectionsSyncPayload> type() {
        return TYPE;
    }
}