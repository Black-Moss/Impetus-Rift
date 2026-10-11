package com.blackmoss.impetusrift.energy;

import com.blackmoss.impetusrift.registry.IRDataComponents;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class ImpetusRechargeAccess {
    private ImpetusRechargeAccess() {
    }

    public static @Nullable ImpetusChargeProfile profile(ItemStack stack) {
        return stack.isEmpty() ? null : stack.get(IRDataComponents.IMPETUS_RECHARGEABLE.get());
    }

    public static boolean isRechargeable(ItemStack stack) {
        return profile(stack) != null;
    }

    public static int getCharge(ItemStack stack) {
        return isRechargeable(stack) ? stack.getOrDefault(IRDataComponents.IMPETUS_ENERGY.get(), 0) : -1;
    }

    public static int getCapacity(ItemStack stack) {
        ImpetusChargeProfile profile = profile(stack);
        return profile == null ? -1 : profile.capacity();
    }

    public static float getChargePercentage(ItemStack stack) {
        ImpetusChargeProfile profile = profile(stack);
        return profile == null ? -1.0F : getCharge(stack) / (float) profile.capacity();
    }

    public static void setCharge(ItemStack stack, int charge) {
        ImpetusChargeProfile profile = profile(stack);
        if (profile == null) {
            return;
        }
        stack.set(IRDataComponents.IMPETUS_ENERGY.get(), Math.clamp(charge, 0, profile.capacity()));
    }

    public static int addCharge(ItemStack stack, int amount) {
        ImpetusChargeProfile profile = profile(stack);
        if (profile == null) {
            return 0;
        }
        int current = getCharge(stack);
        int added = Math.clamp(amount, 0, profile.capacity() - current);
        if (added > 0) {
            stack.set(IRDataComponents.IMPETUS_ENERGY.get(), current + added);
        }
        return added;
    }

    public static boolean consumeCharge(ItemStack stack, int amount) {
        int charge = getCharge(stack);
        if (charge < 0 || charge < amount) {
            return false;
        }
        stack.set(IRDataComponents.IMPETUS_ENERGY.get(), charge - amount);
        return true;
    }
}
