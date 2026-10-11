package com.blackmoss.impetusrift.registry;

import com.blackmoss.impetusrift.ImpetusRift;
import com.blackmoss.impetusrift.recipe.GliderIngredient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class IRIngredientTypes {
    public static final DeferredRegister<IngredientType<?>> INGREDIENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.INGREDIENT_TYPES, ImpetusRift.MODID);

    public static final DeferredHolder<IngredientType<?>, IngredientType<GliderIngredient>> GLIDER =
            INGREDIENT_TYPES.register("glider", () -> new IngredientType<>(GliderIngredient.CODEC));

    private IRIngredientTypes() {
    }

    public static void register(IEventBus modBus) {
        INGREDIENT_TYPES.register(modBus);
    }
}
