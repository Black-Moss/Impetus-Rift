package com.blackmoss.impetusrift.data;

import com.blackmoss.impetusrift.ImpetusRift;
import com.blackmoss.impetusrift.registry.IRItems;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TCAspects;
import com.leclowndu93150.thaumaturge.api.recipe.ResearchGate;
import com.leclowndu93150.thaumaturge.data.recipe.builders.CrucibleRecipeBuilder;
import com.leclowndu93150.thaumaturge.data.recipe.builders.InfusionRecipeBuilder;
import com.leclowndu93150.thaumaturge.data.recipe.builders.workbench.ArcaneWorkbenchShapelessRecipeBuilder;
import com.leclowndu93150.thaumaturge.registry.TCDataComponents;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
import org.jspecify.annotations.NonNull;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public final class ModRecipeProvider extends RecipeProvider {
    private final HolderLookup.Provider lookupProvider;

    private ModRecipeProvider(HolderLookup.Provider provider, RecipeOutput output) {
        super(provider, output);
        this.lookupProvider = provider;
    }

    private static ResearchGate irGate(String path) {
        return new ResearchGate(ImpetusRift.IRIdentifier(path), Optional.empty(), false);
    }

    @Override
    protected void buildRecipes() {
        HolderGetter<IAspect> aspects = registries.lookupOrThrow(IAspect.REGISTRY_KEY);

        impetusCell(aspects);
        impetusGem(aspects);

        riftLocator(aspects);
        impetusLinker(aspects);

        impetusDrainer(aspects);
        impetusDiffuser(aspects);
        impetusGenerator(aspects);

        riftFeeder(aspects);
    }

    // 元动能量单元
    private void impetusCell(HolderGetter<IAspect> aspects) {
        new ArcaneWorkbenchShapelessRecipeBuilder(RecipeCategory.MISC,
                new ItemStackTemplate(IRItems.IMPETUS_CELL.get()),
                aspects,
                30,
                registries.lookupOrThrow(Registries.ITEM))
                .aspect(TCAspects.IGNIS)
                .requires(TCItems.VIS_BATTERY)
                .requires(TCItems.CRYSTAL_VITIUM)
                .requires(TCItems.SALIS_MUNDUS)
                .gate(irGate("impetus_cell"))
                .unlockedBy("has", this.has(TCItems.VIS_BATTERY.get()))
                .save(output);
    }

    // 元动宝石
    private void impetusGem(HolderGetter<IAspect> aspects) {
        new CrucibleRecipeBuilder(aspects, RecipeCategory.MISC,
                new ItemStackTemplate(IRItems.IMPETUS_GEM.get()),
                Ingredient.of(TCItems.ESSENTIA_CRYSTAL.get()))
                .aspect(TCAspects.POTENTIA, 10)
                .aspect(TCAspects.VACUOS, 5)
                .gate(irGate("impetus_gem"))
                .unlockedBy("has", this.has(TCItems.ESSENTIA_CRYSTAL.get()))
                .save(output);
    }

    // 裂隙定位器
    private void riftLocator(HolderGetter<IAspect> aspects) {
        new ArcaneWorkbenchShapelessRecipeBuilder(RecipeCategory.TOOLS,
                new ItemStackTemplate(IRItems.RIFT_COMPASS.get()),
                aspects,
                50,
                registries.lookupOrThrow(Registries.ITEM))
                .aspect(TCAspects.PERDITIO)
                .requires(TCItems.SALIS_MUNDUS)
                .requires(TCItems.CRYSTAL_VITIUM)
                .requires(IRItems.IMPETUS_GEM)
                .gate(irGate("rift_locator"))
                .unlockedBy("has", this.has(IRItems.IMPETUS_GEM.get()))
                .save(output);
    }

    // 元动链接器
    private void impetusLinker(HolderGetter<IAspect> aspects) {
        new ArcaneWorkbenchShapelessRecipeBuilder(RecipeCategory.TOOLS,
                new ItemStackTemplate(IRItems.IMPETUS_LINKER.get()),
                aspects,
                40,
                registries.lookupOrThrow(Registries.ITEM))
                .aspect(TCAspects.IGNIS)
                .aspect(TCAspects.ORDO)
                .requires(TCItems.SALIS_MUNDUS)
                .requires(TCItems.CRYSTAL_VITIUM)
                .requires(IRItems.IMPETUS_GEM)
                .gate(irGate("impetus_linker"))
                .unlockedBy("has", this.has(IRItems.IMPETUS_GEM.get()))
                .save(output);
    }

    // 元动汲取仪
    private void impetusDrainer(HolderGetter<IAspect> aspects) {
        new InfusionRecipeBuilder(aspects, RecipeCategory.MISC,
                new ItemStackTemplate(IRItems.IMPETUS_DRAINER.get()),
                Ingredient.of(TCItems.VIS_BATTERY.get()))
                .component(Ingredient.of(TCItems.CRYSTAL_VITIUM.get()))
                .component(Ingredient.of(TCItems.SALIS_MUNDUS.get()))
                .component(Ingredient.of(TCItems.CRYSTAL_VITIUM.get()))
                .component(Ingredient.of(TCItems.SALIS_MUNDUS.get()))
                .component(Ingredient.of(TCItems.CRYSTAL_VITIUM.get()))
                .component(Ingredient.of(TCItems.SALIS_MUNDUS.get()))
                .aspect(TCAspects.VITIUM, 35)
                .aspect(TCAspects.AQUA, 35)
                .instability(6)
                .gate(irGate("impetus_drainer"))
                .unlockedBy("has", this.has(IRItems.IMPETUS_DRAINER.get()))
                .save(output);
    }

    // 元动扩散仪
    private void impetusDiffuser(HolderGetter<IAspect> aspects) {
        new InfusionRecipeBuilder(aspects, RecipeCategory.MISC,
                new ItemStackTemplate(IRItems.IMPETUS_DIFFUSER.get()),
                Ingredient.of(TCItems.VIS_BATTERY.get()))
                .component(Ingredient.of(TCItems.CRYSTAL_VITIUM.get()))
                .component(Ingredient.of(TCItems.SALIS_MUNDUS.get()))
                .component(Ingredient.of(TCItems.CRYSTAL_VITIUM.get()))
                .component(Ingredient.of(TCItems.SALIS_MUNDUS.get()))
                .aspect(TCAspects.VITIUM, 25)
                .aspect(TCAspects.ALIENIS, 25)
                .instability(4)
                .gate(irGate("impetus_diffuser"))
                .unlockedBy("has", this.has(IRItems.IMPETUS_DIFFUSER.get()))
                .save(output);
    }

    // 元动发电机
    private void impetusGenerator(HolderGetter<IAspect> aspects) {
        new InfusionRecipeBuilder(aspects, RecipeCategory.MISC,
                new ItemStackTemplate(IRItems.IMPETUS_GENERATOR.get()),
                Ingredient.of(TCItems.VIS_BATTERY.get()))
                .component(Ingredient.of(TCItems.CRYSTAL_VITIUM.get()))
                .component(Ingredient.of(TCItems.SALIS_MUNDUS.get()))
                .component(Ingredient.of(TCItems.CRYSTAL_VITIUM.get()))
                .component(Ingredient.of(TCItems.SALIS_MUNDUS.get()))
                .component(Ingredient.of(TCItems.CRYSTAL_VITIUM.get()))
                .component(Ingredient.of(TCItems.SALIS_MUNDUS.get()))
                .aspect(TCAspects.VITIUM, 40)
                .aspect(TCAspects.IGNIS, 30)
                .instability(7)
                .gate(irGate("impetus_generator"))
                .unlockedBy("has", this.has(IRItems.IMPETUS_GENERATOR.get()))
                .save(output);
    }

    // 裂隙供能器
    private void riftFeeder(HolderGetter<IAspect> aspects) {
        new InfusionRecipeBuilder(aspects, RecipeCategory.MISC,
                new ItemStackTemplate(IRItems.RIFT_FEEDER.get()),
                Ingredient.of(TCItems.JAR_BRAIN.get()))
                .component(Ingredient.of(TCItems.CRYSTAL_VITIUM.get()))
                .component(Ingredient.of(TCItems.SALIS_MUNDUS.get()))
                .component(Ingredient.of(TCItems.CRYSTAL_VITIUM.get()))
                .component(Ingredient.of(TCItems.SALIS_MUNDUS.get()))
                .aspect(TCAspects.VITIUM, 30)
                .aspect(TCAspects.ALIENIS, 20)
                .instability(5)
                .gate(irGate("rift_feeder"))
                .unlockedBy("has", this.has(IRItems.RIFT_FEEDER.get()))
                .save(output);
    }

    private HolderSet<Item> tag(String space, String path) {
        return this.lookupProvider.getOrThrow(TagKey.create(
                Registries.ITEM, Identifier.fromNamespaceAndPath(
                        space, path)));
    }

    private Ingredient crystal(HolderGetter<IAspect> aspects, ResourceKey<IAspect> aspect) {
        return DataComponentIngredient.of(
                TCDataComponents.CRYSTAL_ASPECT.get(),
                new AspectInstance(aspects.getOrThrow(aspect), 1),
                TCItems.ESSENTIA_CRYSTAL.get());
    }

    protected @NonNull Criterion<InventoryChangeTrigger.TriggerInstance> has(
            MinMaxBounds.@NonNull Ints count,
            @NonNull ItemLike item) {
        return inventoryTrigger(ItemPredicate.Builder.item().of(this.items, item).withCount(count));
    }

    protected @NonNull Criterion<InventoryChangeTrigger.TriggerInstance> has(@NonNull ItemLike item) {
        return inventoryTrigger(ItemPredicate.Builder.item().of(this.items, item));
    }

    protected @NonNull Criterion<InventoryChangeTrigger.TriggerInstance> has(@NonNull TagKey<Item> tag) {
        return inventoryTrigger(ItemPredicate.Builder.item().of(this.items, tag));
    }

    public static final class Runner extends RecipeProvider.Runner {
        public Runner(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
            super(output, lookupProvider);
        }

        @Override
        protected @NonNull RecipeProvider createRecipeProvider(HolderLookup.@NonNull Provider provider, @NonNull RecipeOutput output) {
            return new ModRecipeProvider(provider, output);
        }

        @Override
        public @NonNull String getName() {
            return "Impulse Rift Recipes";
        }
    }
}