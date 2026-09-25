package com.blackmoss.impetusrift.data;

import com.blackmoss.impetusrift.ImpetusRift;
import com.blackmoss.impetusrift.registry.IRItems;
import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TCAspects;
import com.leclowndu93150.thaumaturge.api.recipe.ResearchGate;
import com.leclowndu93150.thaumaturge.data.recipe.builders.CrucibleRecipeBuilder;
import com.leclowndu93150.thaumaturge.data.recipe.builders.InfusionRecipeBuilder;
import com.leclowndu93150.thaumaturge.data.recipe.builders.workbench.ArcaneWorkbenchShapedRecipeBuilder;
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
import net.minecraft.world.item.Items;
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

    @Override
    protected void buildRecipes() {
        // 元动能量单元
        arcaneShapeless(new ItemStackTemplate(IRItems.IMPETUS_CELL.get()), 30)
                .aspect(TCAspects.IGNIS)
                .requires(TCItems.VIS_BATTERY)
                .requires(TCItems.CRYSTAL_VITIUM)
                .requires(TCItems.SALIS_MUNDUS)
                .gate(irGate("impetus_cell"))
                .unlockedBy("has", this.has(IRItems.IMPETUS_CELL.get()))
                .save(output);

        // 元动宝石
        crucible(new ItemStackTemplate(IRItems.IMPETUS_GEM.get()),
                Ingredient.of(TCItems.VOID_SEED.get()))
                .aspect(TCAspects.VACUOS, 10)
                .gate(irGate("impetus_gem"))
                .unlockedBy("has", this.has(IRItems.IMPETUS_GEM.get()))
                .save(output);

        // 裂隙指南针
        arcaneShaped(new ItemStackTemplate(IRItems.RIFT_COMPASS), 50)
                .aspect(TCAspects.PERDITIO, 10)
                .aspect(TCAspects.ORDO, 10)
                .pattern(" P ")
                .pattern("PSP")
                .pattern(" P ")
                .define('P', TCItems.PLATE_BRASS)
                .define('S', TCItems.SALIS_MUNDUS)
                .gate(irGate("rift_compass"))
                .unlockedBy("has", has(IRItems.RIFT_COMPASS))
                .save(output);

        // 元动谐振器
        arcaneShapeless(new ItemStackTemplate(IRItems.IMPETUS_LINKER.get()), 40)
                .aspect(TCAspects.IGNIS)
                .aspect(TCAspects.ORDO)
                .requires(TCItems.SALIS_MUNDUS)
                .requires(TCItems.CRYSTAL_VITIUM)
                .requires(IRItems.IMPETUS_GEM)
                .gate(irGate("impetus_linker"))
                .unlockedBy("has", this.has(IRItems.IMPETUS_GEM.get()))
                .save(output);

        // 元动汲取仪
        infusion(new ItemStackTemplate(IRItems.IMPETUS_DRAINER.get()),
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

        // 元动弥散仪
        infusion(new ItemStackTemplate(IRItems.IMPETUS_DIFFUSER.get()),
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

        // 元动发电机
        infusion(new ItemStackTemplate(IRItems.IMPETUS_GENERATOR.get()),
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

        // 裂隙旅行者之靴
        infusion(new ItemStackTemplate(IRItems.IMPETUS_TRAVELLER_BOOTS.get()),
                Ingredient.of(TCItems.VOID_BOOTS.get()))
                .component(Ingredient.of(TCItems.TRAVELLER_BOOTS.get()))
                .component(Ingredient.of(TCItems.PLATE_VOID.get()))
                .component(Ingredient.of(IRItems.IMPETUS_GEM.get()))

                .component(Ingredient.of(TCItems.PLATE_VOID.get()))
                .component(Ingredient.of(TCItems.SALIS_MUNDUS.get()))
                .component(Ingredient.of(IRItems.IMPETUS_GEM.get()))

                .component(Ingredient.of(TCItems.PLATE_VOID.get()))
                .component(Ingredient.of(TCItems.FABRIC.get()))
                .component(Ingredient.of(IRItems.IMPETUS_GEM.get()))

                .component(Ingredient.of(Items.LEATHER))
                .component(Ingredient.of(IRItems.IMPETUS_GEM.get()))
                .component(Ingredient.of(TCItems.PLATE_VOID.get()))

                .aspect(TCAspects.METALLUM, 80)
                .aspect(TCAspects.AQUA, 80)
                .aspect(TCAspects.VACUOS, 80)
                .aspect(TCAspects.FABRICO, 80)
                .aspect(TCAspects.ALIENIS, 80)
                .aspect(TCAspects.VOLATUS, 120)
                .aspect(TCAspects.MOTUS, 120)
                .instability(6)
                .gate(irGate("impetus_traveller_boots"))
                .unlockedBy("has", this.has(IRItems.IMPETUS_TRAVELLER_BOOTS.get()))
                .save(output);
    }

    private ArcaneWorkbenchShapedRecipeBuilder arcaneShaped(ItemStackTemplate result, int vis) {
        return new ArcaneWorkbenchShapedRecipeBuilder(RecipeCategory.MISC, result, this.items, this.registries.lookupOrThrow(IAspect.REGISTRY_KEY), vis);
    }

    private ArcaneWorkbenchShapelessRecipeBuilder arcaneShapeless(ItemStackTemplate result, int vis) {
        return new ArcaneWorkbenchShapelessRecipeBuilder(RecipeCategory.MISC, result, this.registries.lookupOrThrow(IAspect.REGISTRY_KEY), vis, this.items);
    }

    private CrucibleRecipeBuilder crucible(ItemStackTemplate result, Ingredient catalyst) {
        return new CrucibleRecipeBuilder(this.registries.lookupOrThrow(IAspect.REGISTRY_KEY), RecipeCategory.MISC, result, catalyst);
    }

    private InfusionRecipeBuilder infusion(ItemStackTemplate result, Ingredient catalyst) {
        return new InfusionRecipeBuilder(this.registries.lookupOrThrow(IAspect.REGISTRY_KEY), RecipeCategory.MISC, result, catalyst);
    }

    private static ResearchGate irGate(String path) {
        return new ResearchGate(ImpetusRift.identifier(path), Optional.empty(), false);
    }

    private static ResearchGate tcGate(String path) {
        return new ResearchGate(TCIds.rl(path), Optional.empty(), false);
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
            return "Impetus Rift Recipes";
        }
    }
}