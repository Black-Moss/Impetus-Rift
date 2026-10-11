package com.blackmoss.impetusrift.energy;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;

public record ElytraThruster(int fuel, int capacity) {
    public static final Codec<ElytraThruster> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("fuel").forGetter(ElytraThruster::fuel),
            ExtraCodecs.POSITIVE_INT.fieldOf("capacity").forGetter(ElytraThruster::capacity)
    ).apply(instance, ElytraThruster::new));

    public static final StreamCodec<ByteBuf, ElytraThruster> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ElytraThruster::fuel,
            ByteBufCodecs.VAR_INT, ElytraThruster::capacity,
            ElytraThruster::new);
}
