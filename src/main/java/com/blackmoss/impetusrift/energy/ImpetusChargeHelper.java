package com.blackmoss.impetusrift.energy;

import com.blackmoss.impetusrift.registry.IRDataComponents;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;

public final class ImpetusChargeHelper {
    public static final Integer CAPACITY = 1000;

    private static final DataComponentType<Integer> ENERGY = IRDataComponents.IMPETUS_ENERGY.get();

    private ImpetusChargeHelper() {
    }

    public static Integer getCharge(ItemStack stack) {
        var value = stack.get(ENERGY);
        return value == null ? 0 : value;
    }

    public static void setCharge(ItemStack stack, Integer charge) {
        stack.set(ENERGY, Math.clamp(charge, 0, CAPACITY));
    }

    public static Integer addCharge(ItemStack stack, Integer amount) {
        Integer current = getCharge(stack);
        Integer added = Math.min(CAPACITY - current, amount);
        if (added > 0L) {
            setCharge(stack, current + added);
        }
        return added;
    }
}
