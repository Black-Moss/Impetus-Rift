package com.blackmoss.impetusrift.data;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.data.DataMapProvider;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;

public class ItemAspectsProvider extends DataMapProvider {

    private HolderGetter<IAspect> aspects;

    public ItemAspectsProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider);
    }

    @Override
    public @NonNull String getName() {
        return "Impetus Rift Item Aspects Data Maps";
    }

    @Override
    protected void gather(HolderLookup.@NonNull Provider provider) {
    }


    @SuppressWarnings("unchecked")
    private AspectList aspects(Object... pairs) {
        AspectList result = AspectList.EMPTY;
        for (int i = 0; i < pairs.length; i += 2) {
            result = result.add(aspects.getOrThrow((ResourceKey<IAspect>) pairs[i]), (Integer) pairs[i + 1]);
        }
        return result;
    }

    private void add(DataMapProvider.Builder<AspectList, Item> b, HolderLookup.RegistryLookup<Item> items, String namespace, String itemId, AspectList value) {
        items.get(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(namespace, itemId)))
                .ifPresent(holder -> b.add(holder, value, false));
    }
}