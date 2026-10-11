package com.blackmoss.impetusrift.registry;

import com.blackmoss.impetusrift.ImpetusRift;
import com.blackmoss.impetusrift.recipe.ThrusterUpgradeRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class IRRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, ImpetusRift.MODID);

    public static final Supplier<RecipeSerializer<ThrusterUpgradeRecipe>> THRUSTER_UPGRADE =
            SERIALIZERS.register("thruster_upgrade", () -> ThrusterUpgradeRecipe.SERIALIZER);

    private IRRecipeSerializers() {
    }
}
