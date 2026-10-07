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

public class ModRecipeProvider extends RecipeProvider {
    private final HolderLookup.Provider lookupProvider;
    private final HolderGetter<IAspect> aspects;

    private ModRecipeProvider(HolderLookup.Provider provider, RecipeOutput output) {
        super(provider, output);
        this.lookupProvider = provider;
        this.aspects = registries.lookupOrThrow(IAspect.REGISTRY_KEY);
    }

    @Override
    protected void buildRecipes() {
        // 元动能量单元
        arcaneShapeless(IRItems.IMPETUS_CELL.get(), 30)
                .aspect(TCAspects.IGNIS)
                .requires(TCItems.VIS_BATTERY)
                .requires(TCItems.CRYSTAL_VITIUM)
                .requires(TCItems.SALIS_MUNDUS)
                .gate(irGate("impetus_cell"))
                .save(output);

        // 元动宝石
        crucible(IRItems.IMPETUS_GEM.get(),
                TCItems.VOID_SEED.get())
                .aspect(TCAspects.VACUOS, 10)
                .gate(irGate("impetus_gem"))
                .save(output);

        // 裂隙指南针
        arcaneShaped(IRItems.RIFT_COMPASS, 50)
                .aspect(TCAspects.PERDITIO, 10)
                .aspect(TCAspects.ORDO, 10)
                .pattern(" P ")
                .pattern("PSP")
                .pattern(" P ")
                .define('P', TCItems.PLATE_BRASS)
                .define('S', TCItems.SALIS_MUNDUS)
                .gate(irGate("rift_compass"))
                .save(output);

        // 元动谐振器
        arcaneShapeless(IRItems.IMPETUS_LINKER.get(), 40)
                .aspect(TCAspects.IGNIS)
                .aspect(TCAspects.ORDO)
                .requires(TCItems.SALIS_MUNDUS)
                .requires(TCItems.CRYSTAL_VITIUM)
                .requires(IRItems.IMPETUS_GEM)
                .gate(irGate("impetus_linker"))
                .save(output);

        // 元动汲取仪
        infusion(IRItems.IMPETUS_DRAINER.get(),
                TCItems.VIS_BATTERY.get())
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
                .save(output);

        // 元动弥散仪
        infusion(IRItems.IMPETUS_DIFFUSER.get(),
                TCItems.VIS_BATTERY.get())
                .component(Ingredient.of(TCItems.CRYSTAL_VITIUM.get()))
                .component(Ingredient.of(TCItems.SALIS_MUNDUS.get()))
                .component(Ingredient.of(TCItems.CRYSTAL_VITIUM.get()))
                .component(Ingredient.of(TCItems.SALIS_MUNDUS.get()))
                .aspect(TCAspects.VITIUM, 25)
                .aspect(TCAspects.ALIENIS, 25)
                .instability(4)
                .gate(irGate("impetus_diffuser"))
                .save(output);

        // 元动发电机
        infusion(IRItems.IMPETUS_GENERATOR.get(), TCItems.VIS_BATTERY.get())
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
                .save(output);

        // 裂隙旅行者之靴
        infusion(IRItems.IMPETUS_TRAVELLER_BOOTS.get(),
                TCItems.VOID_BOOTS.get())
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
                .save(output);
    }

    private CrucibleRecipeBuilder crucible(ItemStackTemplate result, ItemLike catalyst) {
        return new CrucibleRecipeBuilder(this.registries.lookupOrThrow(IAspect.REGISTRY_KEY), RecipeCategory.MISC, result, Ingredient.of(catalyst));
    }

    private CrucibleRecipeBuilder crucible(ItemLike result, ItemLike catalyst) {
        return (CrucibleRecipeBuilder) crucible(new ItemStackTemplate(result.asItem()), catalyst)
                .unlockedBy("has", this.has(result.asItem()));
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

    private Ingredient crystal(ResourceKey<IAspect> aspect) {
        return DataComponentIngredient.of(
                TCDataComponents.CRYSTAL_ASPECT.get(),
                new AspectInstance(aspects.getOrThrow(aspect), 1),
                TCItems.ESSENTIA_CRYSTAL.get());
    }

    private Ingredient commonTag(String path) {
        return Ingredient.of(tag("c", path));
    }

    private InfusionRecipeBuilder infusion(ItemLike result, RecipeCategory category, ItemLike catalyst) {
        return (InfusionRecipeBuilder) new InfusionRecipeBuilder(
                this.registries.lookupOrThrow(IAspect.REGISTRY_KEY),
                category,
                new ItemStackTemplate(result.asItem()),
                Ingredient.of(catalyst))
                .unlockedBy("has", this.has(result));
    }

    private InfusionRecipeBuilder infusion(ItemLike result, ItemLike catalyst) {
        return infusion(result, RecipeCategory.MISC, catalyst);
    }


    private CrucibleRecipeBuilder crucible(ItemLike result, RecipeCategory category, ItemLike catalyst) {
        return (CrucibleRecipeBuilder) new CrucibleRecipeBuilder(
                aspects,
                category,
                new ItemStackTemplate(result.asItem()),
                Ingredient.of(catalyst))
                .unlockedBy("has", this.has(result));
    }

    private ArcaneWorkbenchShapelessRecipeBuilder arcaneShapeless(RecipeCategory recipeCategory, ItemStackTemplate result, int vis) {
        return new ArcaneWorkbenchShapelessRecipeBuilder(recipeCategory,
                result,
                aspects,
                vis,
                registries.lookupOrThrow(Registries.ITEM));
    }

    private ArcaneWorkbenchShapelessRecipeBuilder arcaneShapeless(RecipeCategory recipeCategory, ItemLike result, int vis) {
        return (ArcaneWorkbenchShapelessRecipeBuilder) arcaneShapeless(recipeCategory, new ItemStackTemplate(result.asItem()), vis)
                .unlockedBy("has", this.has(result.asItem()));
    }

    private ArcaneWorkbenchShapelessRecipeBuilder arcaneShapeless(ItemLike result, int vis) {
        return arcaneShapeless(RecipeCategory.MISC, result, vis);
    }

    private ArcaneWorkbenchShapedRecipeBuilder arcaneShaped(RecipeCategory recipeCategory, ItemStackTemplate result, int vis) {
        return new ArcaneWorkbenchShapedRecipeBuilder(recipeCategory,
                result,
                this.items,
                aspects,
                vis);
    }

    private ArcaneWorkbenchShapedRecipeBuilder arcaneShaped(RecipeCategory recipeCategory, ItemLike result, int vis) {
        return (ArcaneWorkbenchShapedRecipeBuilder) arcaneShaped(recipeCategory, new ItemStackTemplate(result.asItem()), vis)
                .unlockedBy("has", this.has(result.asItem()));
    }

    private ArcaneWorkbenchShapedRecipeBuilder arcaneShaped(ItemLike result, int vis) {
        return arcaneShaped(RecipeCategory.MISC, result, vis);
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