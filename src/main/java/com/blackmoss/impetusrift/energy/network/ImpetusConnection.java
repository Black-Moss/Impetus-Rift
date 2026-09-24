package com.blackmoss.impetusrift.energy.network;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record ImpetusConnection(BlockPos sourcePos, BlockPos targetPos) {

    public static final Codec<ImpetusConnection> CODEC = RecordCodecBuilder.create(builder -> builder
            .group(
                    BlockPos.CODEC.fieldOf("source").forGetter(ImpetusConnection::sourcePos),
                    BlockPos.CODEC.fieldOf("target").forGetter(ImpetusConnection::targetPos)
            ).apply(builder, ImpetusConnection::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ImpetusConnection> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, ImpetusConnection::sourcePos,
            BlockPos.STREAM_CODEC, ImpetusConnection::targetPos,
            ImpetusConnection::new
    );

    public static final int MAX_DISTANCE = 8;

    public static final double MATRIX_TO_MATRIX_RATE = 0.01;

    public boolean isWithinDistance() {
        return sourcePos.distManhattan(targetPos) <= MAX_DISTANCE;
    }

    public boolean involves(BlockPos pos) {
        return sourcePos.equals(pos) || targetPos.equals(pos);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ImpetusConnection(BlockPos pos, BlockPos targetPos1))) return false;
        return sourcePos.equals(pos) && targetPos.equals(targetPos1);
    }

    @Override
    public int hashCode() {
        return sourcePos.hashCode() * 31 + targetPos.hashCode();
    }
}