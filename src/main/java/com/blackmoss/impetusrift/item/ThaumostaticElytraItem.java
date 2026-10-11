package com.blackmoss.impetusrift.item;

import com.blackmoss.impetusrift.energy.ImpetusRechargeAccess;
import com.leclowndu93150.thaumaturge.content.equipment.VoidGearItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class ThaumostaticElytraItem extends VoidGearItem {
    private static final int GLIDE_COST_INTERVAL = 20;

    public ThaumostaticElytraItem(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(
            @NonNull ItemStack stack,
            @NonNull ServerLevel level,
            @NonNull Entity entity,
            @Nullable EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);
        if (slot != EquipmentSlot.FEET || !(entity instanceof ServerPlayer player)) {
            return;
        }

        if (player.tickCount % GLIDE_COST_INTERVAL == 0) {
            ImpetusRechargeAccess.consumeCharge(stack, 1);
        }
        if (ImpetusRechargeAccess.getCharge(stack) <= 0) {
            player.stopFallFlying();
        }
    }
}
