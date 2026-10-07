package com.blackmoss.impetusrift.registry;

import com.blackmoss.impetusrift.ImpetusRift;
import com.leclowndu93150.thaumaturge.registry.TCItemTags;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

import java.util.Map;

public final class IRMaterials {
    public static final ResourceKey<EquipmentAsset> ASSET_IMPETUS_TRAVELLER =
            ResourceKey.create(EquipmentAssets.ROOT_ID, ImpetusRift.identifier("impetus_traveller"));

    public static final ArmorMaterial ARMOR_IMPETUS_TRAVELLER = new ArmorMaterial(
            50,
            defense(6, 9, 12, 6),
            30,
            SoundEvents.ARMOR_EQUIP_LEATHER,
            3.0F,
            0.25F,
            TCItemTags.INGOTS_VOID_METAL,
            ASSET_IMPETUS_TRAVELLER
    );

    public IRMaterials() {
    }

    private static Map<ArmorType, Integer> defense(int boots, int leggings, int chestplate, int helmet) {
        return Map.of(
                ArmorType.BOOTS, boots,
                ArmorType.LEGGINGS, leggings,
                ArmorType.CHESTPLATE, chestplate,
                ArmorType.HELMET, helmet);
    }
}
