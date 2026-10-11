package com.blackmoss.impetusrift.data.tag;

import com.blackmoss.impetusrift.ImpetusRift;
import com.blackmoss.impetusrift.registry.IRItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.data.ItemTagsProvider;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;

public final class ModItemTagsProvider extends ItemTagsProvider {
    public static final TagKey<Item> GLIDERS = TagKey.create(Registries.ITEM, ImpetusRift.identifier("gliders"));

    public ModItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, ImpetusRift.MODID);
    }

    @Override
    protected void addTags(HolderLookup.@NonNull Provider lookupProvider) {
        tag(GLIDERS)
                .add(Items.ELYTRA)
                .add(IRItems.THAUMOSTATIC_ELYTRA.get());
    }
}
