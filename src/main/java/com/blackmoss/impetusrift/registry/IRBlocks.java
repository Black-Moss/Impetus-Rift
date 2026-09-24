package com.blackmoss.impetusrift.registry;

import com.blackmoss.impetusrift.ImpetusRift;
import com.blackmoss.impetusrift.block.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class IRBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ImpetusRift.MODID);

    // 元动汲取仪
    public static final DeferredBlock<Block> IMPETUS_DRAINER = BLOCKS.registerBlock(
            "impetus_drainer", ImpetusDrainerBlock::new, props -> props
                    .mapColor(MapColor.METAL)
                    .strength(3.0F, 35.0F)
                    .sound(SoundType.METAL)
                    .noOcclusion());

    // 元动弥散仪
    public static final DeferredBlock<Block> IMPETUS_DIFFUSER = BLOCKS.registerBlock(
            "impetus_diffuser", ImpetusDiffuserBlock::new, props -> props
                    .mapColor(MapColor.METAL)
                    .strength(3.0F, 35.0F)
                    .sound(SoundType.METAL)
                    .noOcclusion());

    // 元动发电机
    public static final DeferredBlock<Block> IMPETUS_GENERATOR = BLOCKS.registerBlock(
            "impetus_generator", ImpetusGeneratorBlock::new, props -> props
                    .mapColor(MapColor.METAL)
                    .strength(3.0F, 35.0F)
                    .sound(SoundType.METAL)
                    .noOcclusion());

    // 元动矩阵
    public static final DeferredBlock<Block> IMPETUS_MATRIX = BLOCKS.registerBlock(
            "impetus_matrix", ImpetusMatrixBlock::new, props -> props
                    .mapColor(MapColor.METAL)
                    .strength(7.5F, 500.0F)
                    .sound(SoundType.METAL)
                    .noOcclusion());

    // 元动祭坛基座
    public static final DeferredBlock<Block> IMPETUS_ALTAR_BASE = BLOCKS.registerBlock(
            "impetus_altar_base", ImpetusAltarBaseBlock::new, props -> props
                    .mapColor(MapColor.METAL)
                    .strength(3.0F, 35.0F)
                    .sound(SoundType.METAL)
                    .noOcclusion());

    // 元动之镜
    public static final DeferredBlock<Block> IMPETUS_MIRROR = BLOCKS.registerBlock(
            "impetus_mirror", Block::new, props -> props
                    .mapColor(MapColor.METAL)
                    .strength(3.0F, 35.0F)
                    .sound(SoundType.METAL)
                    .noOcclusion());

    // 元动中继器
    public static final DeferredBlock<Block> IMPETUS_RELAY = BLOCKS.registerBlock(
            "impetus_relay", Block::new, props -> props
                    .mapColor(MapColor.METAL)
                    .strength(3.0F, 35.0F)
                    .sound(SoundType.METAL)
                    .noOcclusion());

    // 元动阀门
    public static final DeferredBlock<Block> IMPETUS_GATE = BLOCKS.registerBlock(
            "impetus_gate", Block::new, props -> props
                    .mapColor(MapColor.METAL)
                    .strength(3.0F, 35.0F)
                    .sound(SoundType.METAL)
                    .noOcclusion());

    // 咒波引流仪
    public static final DeferredBlock<Block> RIFT_FEEDER = BLOCKS.registerBlock(
            "rift_feeder", Block::new, props -> props
                    .mapColor(MapColor.METAL)
                    .strength(3.0F, 35.0F)
                    .sound(SoundType.METAL)
                    .noOcclusion());

    // 裂隙封装仪
    public static final DeferredBlock<Block> RIFT_MOVER_INPUT = BLOCKS.registerBlock(
            "rift_mover_input", RiftMoverInputBlock::new, props -> props
                    .mapColor(MapColor.METAL)
                    .strength(3.0F, 35.0F)
                    .sound(SoundType.METAL)
                    .noOcclusion());

    // 裂隙释放仪
    public static final DeferredBlock<Block> RIFT_MOVER_OUTPUT = BLOCKS.registerBlock(
            "rift_mover_output", RiftMoverOutputBlock::new, props -> props
                    .mapColor(MapColor.METAL)
                    .strength(3.0F, 35.0F)
                    .sound(SoundType.METAL)
                    .noOcclusion());

    // 裂隙罐子
    public static final DeferredBlock<Block> RIFT_JAR = BLOCKS.registerBlock(
            "rift_jar", RiftJarBlock::new, props -> props
                    .mapColor(MapColor.NONE)
                    .strength(0.3F)
                    .sound(SoundType.GLASS)
                    .noOcclusion());

    // 裂隙监测仪
    public static final DeferredBlock<Block> RIFT_MONITOR = BLOCKS.registerBlock(
            "rift_monitor", Block::new, props -> props
                    .mapColor(MapColor.METAL)
                    .strength(3.0F, 35.0F)
                    .sound(SoundType.METAL)
                    .noOcclusion());

    // 裂隙屏障
    public static final DeferredBlock<Block> RIFT_BARRIER = BLOCKS.registerBlock(
            "rift_barrier", Block::new);

    private IRBlocks() {
    }
}