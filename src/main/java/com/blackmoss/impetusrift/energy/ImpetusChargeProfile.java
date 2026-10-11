package com.blackmoss.impetusrift.energy;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;

public record ImpetusChargeProfile(int capacity, ImpetusChargeDisplay display) {
    public static final Codec<ImpetusChargeProfile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ExtraCodecs.POSITIVE_INT.fieldOf("capacity")
                    .forGetter(ImpetusChargeProfile::capacity),
            ImpetusChargeDisplay.CODEC.optionalFieldOf("display", ImpetusChargeDisplay.ALWAYS)
                    .forGetter(ImpetusChargeProfile::display)
    ).apply(instance, ImpetusChargeProfile::new));

    public static final StreamCodec<ByteBuf, ImpetusChargeProfile> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ImpetusChargeProfile::capacity,
            ByteBufCodecs.idMapper(index -> ImpetusChargeDisplay.values()[index], ImpetusChargeDisplay::ordinal),
            ImpetusChargeProfile::display,
            ImpetusChargeProfile::new);
}
