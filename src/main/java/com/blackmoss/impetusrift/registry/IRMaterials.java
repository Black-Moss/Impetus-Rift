package com.blackmoss.impetusrift.registry;

import com.blackmoss.impetusrift.ImpetusRift;
import com.leclowndu93150.thaumaturge.registry.TTItemTags;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

import java.util.Map;

public final class IRMaterials {
    public static final ResourceKey<EquipmentAsset> ASSET_RIFT_TRAVELLER =
            ResourceKey.create(EquipmentAssets.ROOT_ID, ImpetusRift.identifier("rift_traveller"));

    public static final ArmorMaterial ARMOR_RIFT_TRAVELLER = new ArmorMaterial(
            50,
            Map.of(
                    ArmorType.BOOTS, 6,
                    ArmorType.LEGGINGS, 9,
                    ArmorType.CHESTPLATE, 12,
                    ArmorType.HELMET, 6),
            30,
            SoundEvents.ARMOR_EQUIP_LEATHER,
            3.0F,
            0.25F,
            TTItemTags.INGOTS_VOID_METAL,
            ASSET_RIFT_TRAVELLER
    );

    public IRMaterials() {
    }
}
