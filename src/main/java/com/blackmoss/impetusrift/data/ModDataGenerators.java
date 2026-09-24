package com.blackmoss.impetusrift.data;

import com.blackmoss.impetusrift.ImpetusRift;
import com.blackmoss.impetusrift.data.lang.EnUsProvider;
import com.blackmoss.impetusrift.data.lang.ZhCnProvider;
import com.blackmoss.impetusrift.data.tag.ModBlockTagsProvider;
import com.blackmoss.impetusrift.data.tag.ModItemTagsProvider;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.List;
import java.util.Set;

@EventBusSubscriber(modid = ImpetusRift.MODID)
public final class ModDataGenerators {
    private ModDataGenerators() {
    }

    @SubscribeEvent
    public static void onGatherData(GatherDataEvent.Client event) {
        RegistrySetBuilder registries = new RegistrySetBuilder()
                .add(IAspect.REGISTRY_KEY, ModAspectBootstrap::bootstrap);
        event.createDatapackRegistryObjects(registries);

        event.createProvider(EnUsProvider::new);
        event.createProvider(ZhCnProvider::new);
        event.createProvider(ModBlockTagsProvider::new);
        event.createProvider(ModItemTagsProvider::new);
        event.createProvider(ModRecipeProvider.Runner::new);
        event.createProvider(ModModelProvider::new);
        event.createProvider(ItemAspectsProvider::new);

        event.createProvider((output, lookupProvider) -> new LootTableProvider(
                output,
                Set.of(),
                List.of(new LootTableProvider.SubProviderEntry(
                        ModBlockLootSubProvider::new, LootContextParamSets.BLOCK)),
                lookupProvider));
    }
}