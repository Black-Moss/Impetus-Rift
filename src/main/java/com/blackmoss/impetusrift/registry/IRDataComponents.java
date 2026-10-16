package com.blackmoss.impetusrift.registry;

import com.blackmoss.impetusrift.ImpetusRift;
import com.blackmoss.impetusrift.energy.ElytraThruster;
import com.blackmoss.impetusrift.energy.ImpetusChargeProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.util.ExtraCodecs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class IRDataComponents {
    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, ImpetusRift.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BlockPos>> LINKER_BOUND_SOURCE =
            DATA_COMPONENTS.registerComponentType("linker_bound_source",
                    builder -> builder
                            .persistent(BlockPos.CODEC)
                            .networkSynchronized(ByteBufCodecs.fromCodec(BlockPos.CODEC)));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BlockPos>> LINKER_HIGHLIGHT_NODE =
            DATA_COMPONENTS.registerComponentType("linker_highlight_node",
                    builder -> builder
                            .persistent(BlockPos.CODEC)
                            .networkSynchronized(ByteBufCodecs.fromCodec(BlockPos.CODEC)));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> IMPETUS_ENERGY =
            DATA_COMPONENTS.registerComponentType("impetus_energy",
                    builder -> builder
                            .persistent(ExtraCodecs.NON_NEGATIVE_INT)
                            .networkSynchronized(ByteBufCodecs.VAR_INT));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ImpetusChargeProfile>> IMPETUS_RECHARGEABLE =
            DATA_COMPONENTS.registerComponentType("impetus_rechargeable",
                    builder -> builder
                            .persistent(ImpetusChargeProfile.CODEC)
                            .networkSynchronized(ImpetusChargeProfile.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ElytraThruster>> ELYTRA_THRUSTER =
            DATA_COMPONENTS.registerComponentType("elytra_thruster",
                    builder -> builder
                            .persistent(ElytraThruster.CODEC)
                            .networkSynchronized(ElytraThruster.STREAM_CODEC));
}
