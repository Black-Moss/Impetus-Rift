package com.blackmoss.impetusrift.registry;

import com.blackmoss.impetusrift.ImpetusRift;
import com.blackmoss.impetusrift.block.*;
import com.leclowndu93150.thaumaturge.registry.TTSoundTypes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;

public final class IRBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ImpetusRift.MODID);

    public static final DeferredBlock<Block> IMPETUS_DRAINER = impetusMachine("impetus_drainer", ImpetusDrainerBlock::new);
    public static final DeferredBlock<Block> IMPETUS_DIFFUSER = impetusMachine("impetus_diffuser", ImpetusDiffuserBlock::new);
    public static final DeferredBlock<Block> IMPETUS_GENERATOR = impetusMachine("impetus_generator", ImpetusGeneratorBlock::new);
    public static final DeferredBlock<Block> IMPETUS_MATRIX = impetusMachine("impetus_matrix", ImpetusMatrixBlock::new);
    public static final DeferredBlock<Block> IMPETUS_ALTAR_BASE = impetusMachine("impetus_altar_base", ImpetusAltarBaseBlock::new);
    public static final DeferredBlock<ImpetusMirrorBlock> IMPETUS_MIRROR = impetusMachine("impetus_mirror", ImpetusMirrorBlock::new);
    public static final DeferredBlock<ImpetusRelayBlock> IMPETUS_RELAY = impetusMachine("impetus_relay", ImpetusRelayBlock::new);
    public static final DeferredBlock<ImpetusGateBlock> IMPETUS_GATE = impetusMachine("impetus_gate", ImpetusGateBlock::new);
    public static final DeferredBlock<FluxInducerBlock> FLUX_INDUCER = impetusMachine("rift_feeder", FluxInducerBlock::new);
    public static final DeferredBlock<Block> METASPATIAL_ACCUMULATOR = impetusMachine("rift_mover_input", MetaspatialAccumulatorBlock::new);
    public static final DeferredBlock<Block> METASPATIAL_EXTRUDER = impetusMachine("rift_mover_output", RiftMoverOutputBlock::new);
    public static final DeferredBlock<ExtradimensionalMonitorBlock> EXTRADIMENSIONAL_MONITOR = impetusMachine("rift_dioptra", ExtradimensionalMonitorBlock::new);

    // 裂隙罐子
    public static final DeferredBlock<Block> RIFT_JAR = BLOCKS.registerBlock(
            "rift_jar", RiftJarBlock::new, props -> props
                    .mapColor(MapColor.NONE)
                    .strength(0.3F)
                    .sound(TTSoundTypes.JAR.get())
                    .noOcclusion());


    private IRBlocks() {
    }

    private static <B extends Block> DeferredBlock<B> impetusMachine(String name, Function<BlockBehaviour.Properties, ? extends B> func) {
        return BLOCKS.registerBlock(name, func, props -> props
                .mapColor(MapColor.METAL)
                .strength(3.0F, 35.0F)
                .sound(SoundType.METAL)
                .noOcclusion());
    }
}