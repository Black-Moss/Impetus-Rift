package com.blackmoss.impetusrift;

import com.blackmoss.impetusrift.data.*;
import com.blackmoss.impetusrift.data.lang.EnUsProvider;
import com.blackmoss.impetusrift.data.lang.ZhCnProvider;
import com.blackmoss.impetusrift.data.tag.ModBlockTagsProvider;
import com.blackmoss.impetusrift.data.tag.ModItemTagsProvider;
import com.blackmoss.impetusrift.registry.*;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.data.worldgen.aspect.AspectBootstrap;
import com.mojang.logging.LogUtils;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.slf4j.Logger;

import java.util.List;
import java.util.Set;

@EventBusSubscriber(modid = ImpetusRift.MODID)
@Mod(ImpetusRift.MODID)
public class ImpetusRift {
    public static final String MODID = "impetusrift";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ImpetusRift(IEventBus modEventBus, ModContainer modContainer) {
        IRBlocks.BLOCKS.register(modEventBus);
        IRItems.ITEMS.register(modEventBus);
        IRBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        IRDataComponents.DATA_COMPONENTS.register(modEventBus);
        IRCreativeModeTabs.CREATIVE_MODE_TABS.register(modEventBus);
    }

    public static Identifier identifier(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }

    @SubscribeEvent
    public static void onGatherData(GatherDataEvent.Client event) {
        RegistrySetBuilder registries = new RegistrySetBuilder()
                .add(IAspect.REGISTRY_KEY, AspectBootstrap::bootstrap);
        event.createDatapackRegistryObjects(registries);

        event.createProvider(EnUsProvider::new);
        event.createProvider(ZhCnProvider::new);
        event.createProvider(ModBlockTagsProvider::new);
        event.createProvider(ModItemTagsProvider::new);
        event.createProvider(ModRecipeProvider.Runner::new);
        event.createProvider(ModModelProvider::new);

        event.createProvider((output, lookupProvider) -> new LootTableProvider(
                output,
                Set.of(),
                List.of(new LootTableProvider.SubProviderEntry(
                        ModBlockLootSubProvider::new, LootContextParamSets.BLOCK)),
                lookupProvider));
    }

}
