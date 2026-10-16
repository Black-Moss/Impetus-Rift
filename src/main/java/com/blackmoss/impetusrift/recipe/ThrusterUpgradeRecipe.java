package com.blackmoss.impetusrift.recipe;

import com.blackmoss.impetusrift.energy.ElytraThruster;
import com.blackmoss.impetusrift.energy.ElytraThrusterAccess;
import com.blackmoss.impetusrift.energy.ImpetusRechargeAccess;
import com.blackmoss.impetusrift.registry.IRDataComponents;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Optional;

public class ThrusterUpgradeRecipe extends SimpleSmithingRecipe {
    public static final MapCodec<ThrusterUpgradeRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(
            i -> i.group(
                    Recipe.CommonInfo.MAP_CODEC.forGetter(o -> o.commonInfo),
                    Ingredient.CODEC.fieldOf("base").forGetter(o -> o.base),
                    Ingredient.CODEC.fieldOf("addition").forGetter(o -> o.addition)
            ).apply(i, ThrusterUpgradeRecipe::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, ThrusterUpgradeRecipe> STREAM_CODEC = StreamCodec.composite(
            Recipe.CommonInfo.STREAM_CODEC, o -> o.commonInfo,
            Ingredient.CONTENTS_STREAM_CODEC, o -> o.base,
            Ingredient.CONTENTS_STREAM_CODEC, o -> o.addition,
            ThrusterUpgradeRecipe::new
    );

    public static final RecipeSerializer<ThrusterUpgradeRecipe> SERIALIZER =
            new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    private final Ingredient base;
    private final Ingredient addition;

    public ThrusterUpgradeRecipe(Recipe.CommonInfo commonInfo, Ingredient base, Ingredient addition) {
        super(commonInfo);
        this.base = base;
        this.addition = addition;
    }

    @Override
    public boolean matches(SmithingRecipeInput input, @NonNull Level level) {
        ItemStack baseStack = input.base();
        return input.template().isEmpty()
                && baseStack.has(DataComponents.GLIDER)
                && !baseStack.has(IRDataComponents.ELYTRA_THRUSTER.get())
                && this.addition.test(input.addition());
    }

    @Override
    public @NonNull ItemStack assemble(SmithingRecipeInput input) {
        ItemStack result = input.base().copyWithCount(1);
        int fuel = Math.max(0, ImpetusRechargeAccess.getCharge(input.addition()));
        result.set(IRDataComponents.ELYTRA_THRUSTER.get(),
                new ElytraThruster(fuel, ElytraThrusterAccess.THRUSTER_CAPACITY));
        return result;
    }

    @Override
    public @NonNull Optional<Ingredient> templateIngredient() {
        return Optional.empty();
    }

    @Override
    public @NonNull Ingredient baseIngredient() {
        return this.base;
    }

    @Override
    public @NonNull Optional<Ingredient> additionIngredient() {
        return Optional.of(this.addition);
    }

    @Override
    public @NonNull RecipeSerializer<ThrusterUpgradeRecipe> getSerializer() {
        return SERIALIZER;
    }

    @Override
    protected @NonNull PlacementInfo createPlacementInfo() {
        return PlacementInfo.create(List.of(this.base, this.addition));
    }

    @Override
    public @NonNull List<RecipeDisplay> display() {
        return List.of();
    }
}
