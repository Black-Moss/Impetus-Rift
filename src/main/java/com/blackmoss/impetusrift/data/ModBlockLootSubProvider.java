package com.blackmoss.impetusrift.data;

import com.blackmoss.impetusrift.registry.IRBlocks;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.NonNull;

import java.util.Set;

public class ModBlockLootSubProvider extends BlockLootSubProvider {

    public ModBlockLootSubProvider(HolderLookup.Provider lookupProvider) {
        super(Set.of(), FeatureFlags.DEFAULT_FLAGS, lookupProvider);
    }

    @Override
    protected @NonNull Iterable<Block> getKnownBlocks() {
        return IRBlocks.BLOCKS.getEntries().stream().map(holder -> (Block) holder.value()).toList();
    }

    @Override
    protected void generate() {
        dropSelf(IRBlocks.IMPETUS_DRAINER.get());
        dropSelf(IRBlocks.IMPETUS_DIFFUSER.get());
        dropSelf(IRBlocks.IMPETUS_GENERATOR.get());
        dropSelf(IRBlocks.IMPETUS_MATRIX.get());
        dropOther(IRBlocks.IMPETUS_ALTAR_BASE.get(), TCBlocks.PEDESTAL_ELDRITCH.get());
        dropSelf(IRBlocks.IMPETUS_MIRROR.get());
        dropSelf(IRBlocks.IMPETUS_RELAY.get());
        dropSelf(IRBlocks.IMPETUS_GATE.get());
        dropSelf(IRBlocks.RIFT_FEEDER.get());
        dropSelf(IRBlocks.RIFT_MOVER_INPUT.get());
        dropSelf(IRBlocks.RIFT_MOVER_OUTPUT.get());
        dropSelf(IRBlocks.RIFT_JAR.get());
        dropSelf(IRBlocks.RIFT_MONITOR.get());
        dropSelf(IRBlocks.RIFT_BARRIER.get());
    }
}