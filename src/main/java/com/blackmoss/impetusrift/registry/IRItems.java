package com.blackmoss.impetusrift.registry;

import com.blackmoss.impetusrift.ImpetusRift;
import com.blackmoss.impetusrift.energy.ImpetusChargeDisplay;
import com.blackmoss.impetusrift.energy.ImpetusChargeProfile;
import com.blackmoss.impetusrift.item.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.equipment.Equippable;
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

    public static final DeferredItem<RiftTravellerBootsItem> RIFT_TRAVELLER_BOOTS = ITEMS.registerItem(
            "rift_traveller_boots", RiftTravellerBootsItem::new, props -> props
                    .humanoidArmor(IRMaterials.ARMOR_RIFT_TRAVELLER, ArmorType.BOOTS)
                    .durability(700)
                    .rarity(Rarity.EPIC)
                    .attributes(IRMaterials.ARMOR_RIFT_TRAVELLER.createAttributes(ArmorType.BOOTS))
                    .component(IRDataComponents.IMPETUS_RECHARGEABLE.get(),
                            new ImpetusChargeProfile(1000, ImpetusChargeDisplay.ON_CHANGE)));

    public static final DeferredItem<ThaumostaticElytraItem> THAUMOSTATIC_ELYTRA = ITEMS.registerItem(
            "thaumostatic_elytra", ThaumostaticElytraItem::new, props -> props
                    .component(DataComponents.GLIDER, Unit.INSTANCE)
                    .durability(500)
                    .stacksTo(1)
                    .rarity(Rarity.EPIC)
                    .component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.CHEST)
                            .setAsset(EquipmentAssets.ELYTRA)
                            .setEquipOnInteract(true)
                            .setDamageOnHurt(false)
                            .build())
                    .component(IRDataComponents.IMPETUS_RECHARGEABLE.get(),
                            new ImpetusChargeProfile(1000, ImpetusChargeDisplay.ON_CHANGE)));

    public static final DeferredItem<ElytraThrusterItem> ELYTRA_THRUSTER = ITEMS.registerItem(
            "elytra_thruster", ElytraThrusterItem::new, props -> props
                    .component(IRDataComponents.IMPETUS_RECHARGEABLE.get(),
                            new ImpetusChargeProfile(1000, ImpetusChargeDisplay.ON_CHANGE)));


    private IRItems() {
    }
}