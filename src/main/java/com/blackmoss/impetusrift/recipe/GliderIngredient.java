package com.blackmoss.impetusrift.recipe;

import com.blackmoss.impetusrift.registry.IRIngredientTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import org.jspecify.annotations.NonNull;

import java.util.stream.Stream;

public record GliderIngredient(boolean any) implements ICustomIngredient {
    public static final MapCodec<GliderIngredient> CODEC = RecordCodecBuilder.mapCodec(
            i -> i.group(
                    Codec.BOOL.optionalFieldOf("any", true).forGetter(GliderIngredient::any)
            ).apply(i, GliderIngredient::new)
    );

    public static final GliderIngredient INSTANCE = new GliderIngredient(true);

    @Override
    public boolean test(ItemStack stack) {
        return stack.has(DataComponents.GLIDER);
    }

    @Override
    public @NonNull Stream<Holder<Item>> items() {
        return BuiltInRegistries.ITEM.listElements()
                .filter(holder -> holder.value().components().has(DataComponents.GLIDER))
                .map(holder -> holder);
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public @NonNull IngredientType<?> getType() {
        return IRIngredientTypes.GLIDER.get();
    }
}
