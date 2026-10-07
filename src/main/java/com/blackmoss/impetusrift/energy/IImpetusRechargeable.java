package com.blackmoss.impetusrift.energy;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public interface IImpetusRechargeable {
    long getMaxCharge(@NonNull ItemStack stack, @Nullable LivingEntity entity);

    IImpetusRechargeable.ChargeDisplay showInHud(ItemStack var1, LivingEntity var2);

    enum ChargeDisplay {
        NEVER,
        NORMAL,
        PERIODIC;

        ChargeDisplay() {
        }
    }
}
