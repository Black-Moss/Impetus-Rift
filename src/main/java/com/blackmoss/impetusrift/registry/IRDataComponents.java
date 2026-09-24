package com.blackmoss.impetusrift.registry;

import com.blackmoss.impetusrift.ImpetusRift;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
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
}
