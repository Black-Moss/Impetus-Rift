package com.blackmoss.impetusrift.item;

import com.blackmoss.impetusrift.energy.ImpetusRechargeAccess;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class ThaumostaticElytraItem extends Item {
    private static final int GLIDE_COST_INTERVAL = 20;
    private static final int BOOST_COST_INTERVAL = 4;

    private static final double BOOST_DIRECT = 0.1;
    private static final double BOOST_TARGET = 1.5;
    private static final double BOOST_LERP = 0.5;

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
        if (slot != EquipmentSlot.CHEST || !(entity instanceof ServerPlayer player) || !player.isFallFlying()) {
            return;
        }

        if (player.tickCount % GLIDE_COST_INTERVAL == 0) {
            ImpetusRechargeAccess.consumeCharge(stack, 1);
        }

        if (ImpetusRechargeAccess.getCharge(stack) <= 0) {
            player.stopFallFlying();
            return;
        }

        if (player.getLastClientInput().jump()
                && player.tickCount % BOOST_COST_INTERVAL == 0
                && ImpetusRechargeAccess.consumeCharge(stack, 1)) {
            Vec3 look = player.getLookAngle();
            Vec3 velocity = player.getDeltaMovement();
            player.push(
                    look.x * BOOST_DIRECT + (look.x * BOOST_TARGET - velocity.x) * BOOST_LERP,
                    look.y * BOOST_DIRECT + (look.y * BOOST_TARGET - velocity.y) * BOOST_LERP,
                    look.z * BOOST_DIRECT + (look.z * BOOST_TARGET - velocity.z) * BOOST_LERP);
        }
    }
}
