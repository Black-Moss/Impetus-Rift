package com.blackmoss.impetusrift.registry;

import com.blackmoss.impetusrift.ImpetusRift;
import com.blackmoss.impetusrift.block.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Set;

public final class IRBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ImpetusRift.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ImpetusGeneratorBlockEntity>> IMPETUS_GENERATOR =
            BLOCK_ENTITIES.register("impetus_generator",
                    () -> new BlockEntityType<>(ImpetusGeneratorBlockEntity::new, Set.of(IRBlocks.IMPETUS_GENERATOR.get())));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ImpetusDrainerBlockEntity>> IMPETUS_DRAINER =
            BLOCK_ENTITIES.register("impetus_drainer",
                    () -> new BlockEntityType<>(ImpetusDrainerBlockEntity::new, Set.of(IRBlocks.IMPETUS_DRAINER.get())));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ImpetusDiffuserBlockEntity>> IMPETUS_DIFFUSER =
            BLOCK_ENTITIES.register("impetus_diffuser",
                    () -> new BlockEntityType<>(ImpetusDiffuserBlockEntity::new, Set.of(IRBlocks.IMPETUS_DIFFUSER.get())));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ImpetusMatrixBlockEntity>> IMPETUS_MATRIX =
            BLOCK_ENTITIES.register("impetus_matrix",
                    () -> new BlockEntityType<>(ImpetusMatrixBlockEntity::new, Set.of(IRBlocks.IMPETUS_MATRIX.get())));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ImpetusRelayBlockEntity>> IMPETUS_RELAY =
            BLOCK_ENTITIES.register("impetus_relay",
                    () -> new BlockEntityType<>(ImpetusRelayBlockEntity::new, Set.of(IRBlocks.IMPETUS_RELAY.get())));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ImpetusMirrorBlockEntity>> IMPETUS_MIRROR =
            BLOCK_ENTITIES.register("impetus_mirror",
                    () -> new BlockEntityType<>(ImpetusMirrorBlockEntity::new, Set.of(IRBlocks.IMPETUS_MIRROR.get())));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ImpetusGateBlockEntity>> IMPETUS_GATE =
            BLOCK_ENTITIES.register("impetus_gate",
                    () -> new BlockEntityType<>(ImpetusGateBlockEntity::new, Set.of(IRBlocks.IMPETUS_GATE.get())));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RiftFeederBlockEntity>> RIFT_FEEDER =
            BLOCK_ENTITIES.register("rift_feeder",
                    () -> new BlockEntityType<>(RiftFeederBlockEntity::new, Set.of(IRBlocks.RIFT_FEEDER.get())));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RiftDioptraBlockEntity>> RIFT_DIOPTRA =
            BLOCK_ENTITIES.register("rift_dioptra",
                    () -> new BlockEntityType<>(RiftDioptraBlockEntity::new, Set.of(IRBlocks.RIFT_DIOPTRA.get())));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ImpetusAltarBaseBlockEntity>> IMPETUS_ALTAR_BASE =
            BLOCK_ENTITIES.register("impetus_altar_base",
                    () -> new BlockEntityType<>(ImpetusAltarBaseBlockEntity::new, Set.of(IRBlocks.IMPETUS_ALTAR_BASE.get())));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RiftJarBlockEntity>> RIFT_JAR =
            BLOCK_ENTITIES.register("rift_jar",
                    () -> new BlockEntityType<>(RiftJarBlockEntity::new, Set.of(IRBlocks.RIFT_JAR.get())));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RiftMoverInputBlockEntity>> RIFT_MOVER_INPUT =
            BLOCK_ENTITIES.register("rift_mover_input",
                    () -> new BlockEntityType<>(RiftMoverInputBlockEntity::new, Set.of(IRBlocks.RIFT_MOVER_INPUT.get())));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RiftMoverOutputBlockEntity>> RIFT_MOVER_OUTPUT =
            BLOCK_ENTITIES.register("rift_mover_output",
                    () -> new BlockEntityType<>(RiftMoverOutputBlockEntity::new, Set.of(IRBlocks.RIFT_MOVER_OUTPUT.get())));

    private IRBlockEntities() {
    }
}