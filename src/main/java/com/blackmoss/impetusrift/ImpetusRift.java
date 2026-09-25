package com.blackmoss.impetusrift;

import com.blackmoss.impetusrift.registry.*;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

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
}
