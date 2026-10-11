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

    // 元动汲取仪
    public static final DeferredBlock<Block> IMPETUS_DRAINER = impetusMachine("impetus_drainer", ImpetusDrainerBlock::new);

    // 元动弥散仪
    public static final DeferredBlock<Block> IMPETUS_DIFFUSER = impetusMachine("impetus_diffuser", ImpetusDiffuserBlock::new);

    // 元动发电机
    public static final DeferredBlock<Block> IMPETUS_GENERATOR = impetusMachine("impetus_generator", ImpetusGeneratorBlock::new);

    // 元动矩阵
    public static final DeferredBlock<Block> IMPETUS_MATRIX = impetusMachine("impetus_matrix", ImpetusMatrixBlock::new);

    // 元动祭坛基座
    public static final DeferredBlock<Block> IMPETUS_ALTAR_BASE = impetusMachine("impetus_altar_base", ImpetusAltarBaseBlock::new);

    // 元动之镜
    public static final DeferredBlock<ImpetusMirrorBlock> IMPETUS_MIRROR = impetusMachine("impetus_mirror", ImpetusMirrorBlock::new);

    // 元动中继器
    public static final DeferredBlock<ImpetusRelayBlock> IMPETUS_RELAY = impetusMachine("impetus_relay", ImpetusRelayBlock::new);

    // 元动阀门
    public static final DeferredBlock<ImpetusGateBlock> IMPETUS_GATE = impetusMachine("impetus_gate", ImpetusGateBlock::new);

    // 裂隙引流仪
    public static final DeferredBlock<RiftFeederBlock> RIFT_FEEDER = impetusMachine("rift_feeder", RiftFeederBlock::new);

    // 裂隙封装仪
    public static final DeferredBlock<Block> RIFT_MOVER_INPUT = impetusMachine("rift_mover_input", RiftMoverInputBlock::new);

    // 裂隙释放仪
    public static final DeferredBlock<Block> RIFT_MOVER_OUTPUT = impetusMachine("rift_mover_output", RiftMoverOutputBlock::new);

    // 裂隙罐子
    public static final DeferredBlock<Block> RIFT_JAR = BLOCKS.registerBlock(
            "rift_jar", RiftJarBlock::new, props -> props
                    .mapColor(MapColor.NONE)
                    .strength(0.3F)
                    .sound(TTSoundTypes.JAR.get())
                    .noOcclusion());

    // 裂隙监测仪
    public static final DeferredBlock<RiftMonitorBlock> RIFT_MONITOR = impetusMachine("rift_monitor", RiftMonitorBlock::new);

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