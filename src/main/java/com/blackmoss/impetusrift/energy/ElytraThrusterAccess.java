package com.blackmoss.impetusrift.energy;

import com.blackmoss.impetusrift.registry.IRDataComponents;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class ElytraThrusterAccess {
    public static final int THRUSTER_CAPACITY = 1500;

    private ElytraThrusterAccess() {
    }

    public static boolean hasThruster(ItemStack stack) {
        return !stack.isEmpty() && stack.has(IRDataComponents.ELYTRA_THRUSTER.get());
    }

    public static int getFuel(ItemStack stack) {
        ElytraThruster thruster = stack.get(IRDataComponents.ELYTRA_THRUSTER.get());
        return thruster == null ? -1 : thruster.fuel();
    }

    public static int getCapacity(ItemStack stack) {
        ElytraThruster thruster = stack.get(IRDataComponents.ELYTRA_THRUSTER.get());
        return thruster == null ? -1 : thruster.capacity();
    }

    public static void install(ItemStack stack) {
        if (!hasThruster(stack)) {
            stack.set(IRDataComponents.ELYTRA_THRUSTER.get(), new ElytraThruster(0, THRUSTER_CAPACITY));
        }
    }

    public static void setFuel(ItemStack stack, int fuel) {
        ElytraThruster current = stack.get(IRDataComponents.ELYTRA_THRUSTER.get());
        int capacity = current != null ? current.capacity() : THRUSTER_CAPACITY;
        stack.set(IRDataComponents.ELYTRA_THRUSTER.get(),
                new ElytraThruster(Math.clamp(fuel, 0, capacity), capacity));
    }

    public static int addFuel(ItemStack stack, int amount) {
        ElytraThruster thruster = stack.get(IRDataComponents.ELYTRA_THRUSTER.get());
        if (thruster == null) {
            return 0;
        }
        int added = Math.clamp(amount, 0, thruster.capacity() - thruster.fuel());
        if (added > 0) {
            stack.set(IRDataComponents.ELYTRA_THRUSTER.get(),
                    new ElytraThruster(thruster.fuel() + added, thruster.capacity()));
        }
        return added;
    }

    public static boolean consumeFuel(ItemStack stack, int amount) {
        ElytraThruster thruster = stack.get(IRDataComponents.ELYTRA_THRUSTER.get());
        if (thruster == null || thruster.fuel() < amount) {
            return false;
        }
        stack.set(IRDataComponents.ELYTRA_THRUSTER.get(),
                new ElytraThruster(thruster.fuel() - amount, thruster.capacity()));
        return true;
    }

    public static @Nullable ElytraThruster get(ItemStack stack) {
        return stack.get(IRDataComponents.ELYTRA_THRUSTER.get());
    }
}
