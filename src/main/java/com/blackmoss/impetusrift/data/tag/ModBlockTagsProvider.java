package com.blackmoss.impetusrift.data.tag;

import com.blackmoss.impetusrift.ImpetusRift;
import com.blackmoss.impetusrift.registry.IRBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;

public final class ModBlockTagsProvider extends BlockTagsProvider {
    public ModBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, ImpetusRift.MODID);
    }

    @Override
    protected void addTags(HolderLookup.@NonNull Provider lookupProvider) {
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(IRBlocks.IMPETUS_DRAINER.get())
                .add(IRBlocks.IMPETUS_DIFFUSER.get())
                .add(IRBlocks.IMPETUS_GENERATOR.get())
                .add(IRBlocks.IMPETUS_MATRIX.get())
                .add(IRBlocks.IMPETUS_ALTAR_BASE.get())
                .add(IRBlocks.RIFT_FEEDER.get())
                .add(IRBlocks.RIFT_MOVER_INPUT.get())
                .add(IRBlocks.RIFT_MOVER_OUTPUT.get())
                .add(IRBlocks.RIFT_DIOPTRA.get());
    }
}