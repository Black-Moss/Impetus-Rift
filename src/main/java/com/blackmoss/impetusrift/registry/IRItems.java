package com.blackmoss.impetusrift.registry;

import com.blackmoss.impetusrift.ImpetusRift;
import com.blackmoss.impetusrift.item.ImpetusCellItem;
import com.blackmoss.impetusrift.item.ImpetusLinkerItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class IRItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ImpetusRift.MODID);

    // 方块物品
    public static final DeferredItem<BlockItem> IMPETUS_DRAINER = ITEMS.registerSimpleBlockItem(IRBlocks.IMPETUS_DRAINER);
    public static final DeferredItem<BlockItem> IMPETUS_DIFFUSER = ITEMS.registerSimpleBlockItem(IRBlocks.IMPETUS_DIFFUSER);
    public static final DeferredItem<BlockItem> IMPETUS_GENERATOR = ITEMS.registerSimpleBlockItem(IRBlocks.IMPETUS_GENERATOR);
    public static final DeferredItem<BlockItem> IMPETUS_MATRIX = ITEMS.registerSimpleBlockItem(IRBlocks.IMPETUS_MATRIX);
    public static final DeferredItem<BlockItem> IMPETUS_ALTAR_BASE = ITEMS.registerSimpleBlockItem(IRBlocks.IMPETUS_ALTAR_BASE);
    public static final DeferredItem<BlockItem> IMPETUS_MIRROR = ITEMS.registerSimpleBlockItem(IRBlocks.IMPETUS_MIRROR);
    public static final DeferredItem<BlockItem> IMPETUS_RELAY = ITEMS.registerSimpleBlockItem(IRBlocks.IMPETUS_RELAY);
    public static final DeferredItem<BlockItem> IMPETUS_GATE = ITEMS.registerSimpleBlockItem(IRBlocks.IMPETUS_GATE);

    public static final DeferredItem<BlockItem> RIFT_FEEDER = ITEMS.registerSimpleBlockItem(IRBlocks.RIFT_FEEDER);
    public static final DeferredItem<BlockItem> RIFT_MOVER_INPUT = ITEMS.registerSimpleBlockItem(IRBlocks.RIFT_MOVER_INPUT);
    public static final DeferredItem<BlockItem> RIFT_MOVER_OUTPUT = ITEMS.registerSimpleBlockItem(IRBlocks.RIFT_MOVER_OUTPUT);
    public static final DeferredItem<BlockItem> RIFT_JAR = ITEMS.registerSimpleBlockItem(IRBlocks.RIFT_JAR);
    public static final DeferredItem<BlockItem> RIFT_MONITOR = ITEMS.registerSimpleBlockItem(IRBlocks.RIFT_MONITOR);
    public static final DeferredItem<BlockItem> RIFT_BARRIER = ITEMS.registerSimpleBlockItem(IRBlocks.RIFT_BARRIER);

    public static final DeferredItem<Item> IMPETUS_GEM = ITEMS.registerItem(
            "impetus_gem", Item::new, props -> props
                    .stacksTo(64));

    public static final DeferredItem<Item> RIFT_SEED_FLUX = ITEMS.registerItem(
            "rift_seed_flux", Item::new, props -> props
                    .stacksTo(64));

    public static final DeferredItem<Item> RIFT_COMPASS = ITEMS.registerItem(
            "rift_compass", Item::new, props -> props
                    .stacksTo(1));

    public static final DeferredItem<ImpetusLinkerItem> IMPETUS_LINKER = ITEMS.registerItem(
            "impetus_linker", ImpetusLinkerItem::new, props -> props
                    .stacksTo(1));

    public static final DeferredItem<ImpetusCellItem> IMPETUS_CELL = ITEMS.registerItem(
            "impetus_cell", ImpetusCellItem::new, props -> props
                    .stacksTo(64));

    private IRItems() {
    }
}