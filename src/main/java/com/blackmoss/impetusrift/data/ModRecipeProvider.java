package com.blackmoss.impetusrift.data;

import com.blackmoss.impetusrift.ImpetusRift;
import com.blackmoss.impetusrift.registry.IRItems;
import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.recipe.ResearchGate;
import com.leclowndu93150.thaumaturge.data.recipe.builders.CrucibleRecipeBuilder;
import com.leclowndu93150.thaumaturge.data.recipe.builders.InfusionRecipeBuilder;
import com.leclowndu93150.thaumaturge.data.recipe.builders.workbench.ArcaneWorkbenchShapedRecipeBuilder;
import com.leclowndu93150.thaumaturge.data.recipe.builders.workbench.ArcaneWorkbenchShapelessRecipeBuilder;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTItemTags;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
import org.jspecify.annotations.NonNull;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public final class ModRecipeProvider extends RecipeProvider {
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
                .aspect(TTAspects.IGNIS)
                .requires(TTItems.VIS_BATTERY)
                .requires(TTItems.CRYSTAL_VITIUM)
                .requires(TTItems.SALIS_MUNDUS)
                .save(output);

        // 元动宝石
        crucible(IRItems.IMPETUS_GEM.get(),
                TTItems.VOID_SEED.get())
                .aspect(TTAspects.VACUOS, 10)
                .save(output);

        // 裂隙指南针
        arcaneShaped(IRItems.RIFT_COMPASS, 50)
                .aspect(TTAspects.PERDITIO, 10)
                .aspect(TTAspects.ORDO, 10)
                .pattern(" P ")
                .pattern("PSP")
                .pattern(" P ")
                .define('P', TTItems.PLATE_BRASS)
                .define('S', TTItems.SALIS_MUNDUS)
                .save(output);

        // 元动谐振器
        arcaneShapeless(IRItems.IMPETUS_LINKER.get(), 40)
                .aspect(TTAspects.IGNIS)
                .aspect(TTAspects.ORDO)
                .requires(TTItems.SALIS_MUNDUS)
                .requires(TTItems.CRYSTAL_VITIUM)
                .requires(IRItems.IMPETUS_GEM)
                .save(output);

        // 元动汲取仪
        infusion(IRItems.IMPETUS_DRAINER.get(),
                TTItems.VIS_BATTERY.get())
                .component(Ingredient.of(TTItems.CRYSTAL_VITIUM.get()))
                .component(Ingredient.of(TTItems.SALIS_MUNDUS.get()))
                .component(Ingredient.of(TTItems.CRYSTAL_VITIUM.get()))
                .component(Ingredient.of(TTItems.SALIS_MUNDUS.get()))
                .component(Ingredient.of(TTItems.CRYSTAL_VITIUM.get()))
                .component(Ingredient.of(TTItems.SALIS_MUNDUS.get()))
                .aspect(TTAspects.VITIUM, 35)
                .aspect(TTAspects.AQUA, 35)
                .instability(6)
                .save(output);

        // 元动弥散仪
        infusion(IRItems.IMPETUS_DIFFUSER.get(),
                TTItems.VIS_BATTERY.get())
                .component(Ingredient.of(TTItems.CRYSTAL_VITIUM.get()))
                .component(Ingredient.of(TTItems.SALIS_MUNDUS.get()))
                .component(Ingredient.of(TTItems.CRYSTAL_VITIUM.get()))
                .component(Ingredient.of(TTItems.SALIS_MUNDUS.get()))
                .aspect(TTAspects.VITIUM, 25)
                .aspect(TTAspects.ALIENIS, 25)
                .instability(4)
                .save(output);

        // 元动发电机
        infusion(IRItems.IMPETUS_GENERATOR.get(), TTItems.VIS_BATTERY.get())
                .component(Ingredient.of(TTItems.CRYSTAL_VITIUM.get()))
                .component(Ingredient.of(TTItems.SALIS_MUNDUS.get()))
                .component(Ingredient.of(TTItems.CRYSTAL_VITIUM.get()))
                .component(Ingredient.of(TTItems.SALIS_MUNDUS.get()))
                .component(Ingredient.of(TTItems.CRYSTAL_VITIUM.get()))
                .component(Ingredient.of(TTItems.SALIS_MUNDUS.get()))
                .aspect(TTAspects.VITIUM, 40)
                .aspect(TTAspects.IGNIS, 30)
                .instability(7)
                .save(output);

        // 裂隙旅行者之靴
        infusion(IRItems.RIFT_TRAVELLER_BOOTS.get(),
                TTItems.VOID_BOOTS.get())
                .component(Ingredient.of(TTItems.TRAVELLER_BOOTS.get()))
                .component(Ingredient.of(TTItems.PLATE_VOID.get()))
                .component(Ingredient.of(IRItems.IMPETUS_GEM.get()))

                .component(Ingredient.of(TTItems.PLATE_VOID.get()))
                .component(Ingredient.of(TTItems.SALIS_MUNDUS.get()))
                .component(Ingredient.of(IRItems.IMPETUS_GEM.get()))

                .component(Ingredient.of(TTItems.PLATE_VOID.get()))
                .component(Ingredient.of(TTItems.FABRIC.get()))
                .component(Ingredient.of(IRItems.IMPETUS_GEM.get()))

                .component(Ingredient.of(Items.LEATHER))
                .component(Ingredient.of(IRItems.IMPETUS_GEM.get()))
                .component(Ingredient.of(TTItems.PLATE_VOID.get()))

                .aspect(TTAspects.METALLUM, 80)
                .aspect(TTAspects.AQUA, 80)
                .aspect(TTAspects.VACUOS, 80)
                .aspect(TTAspects.FABRICO, 80)
                .aspect(TTAspects.ALIENIS, 80)
                .aspect(TTAspects.VOLATUS, 120)
                .aspect(TTAspects.MOTUS, 120)
                .instability(6)
                .save(output);

        // 奥法鞘翅
        infusion(IRItems.THAUMOSTATIC_ELYTRA,
                RecipeCategory.COMBAT,
                Items.ELYTRA)
                .component(Ingredient.of(TTItems.CRYSTAL_AER.get()))
                .component(Ingredient.of(TTItems.CRYSTAL_AER.get()))
                .component(tag(TTItemTags.PLANKS_GREATWOOD))
                .component(tag(TTItemTags.PLANKS_GREATWOOD))
                .component(Ingredient.of(Items.COMPARATOR))
                .component(Ingredient.of(IRItems.IMPETUS_GEM.get()))
                .component(Ingredient.of(IRItems.IMPETUS_GEM.get()))
                .component(tag(TTItemTags.PLATES_VOID_METAL))
                .component(tag(TTItemTags.PLATES_VOID_METAL))
                .aspect(TTAspects.VOLATUS, 64)
                .aspect(TTAspects.POTENTIA, 64)
                .aspect(TTAspects.MACHINA, 64)
                .aspect(TTAspects.MOTUS, 32)
                .aspect(TTAspects.VACUOS, 16)
                .instability(7)
                .save(output);

        // 鞘翅助推器
        infusion(IRItems.ELYTRA_THRUSTER,
                RecipeCategory.COMBAT,
                TTItems.GIRDLE_MUNDANE)
                .component(Ingredient.of(IRItems.IMPETUS_CELL.get()))
                .component(crystal(TTAspects.ALIENIS))
                .component(tag(TTItemTags.PLATES_VOID_METAL))
                .component(Ingredient.of(IRItems.IMPETUS_GEM.get()))
                .component(crystal(TTAspects.VACUOS))
                .component(tag(TTItemTags.PLATES_VOID_METAL))
                .aspect(TTAspects.ALIENIS, 25)
                .aspect(TTAspects.MOTUS, 75)
                .aspect(TTAspects.POTENTIA, 50)
                .aspect(TTAspects.VOLATUS, 45)
                .instability(7)
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
        return new ResearchGate(TTIds.rl(path), Optional.empty(), false);
    }

    private HolderSet<Item> tag(String space, String path) {
        return this.lookupProvider.getOrThrow(TagKey.create(
                Registries.ITEM, Identifier.fromNamespaceAndPath(
                        space, path)));
    }

    private Ingredient crystal(ResourceKey<IAspect> aspect) {
        return DataComponentIngredient.of(
                TTDataComponents.CRYSTAL_ASPECT.get(),
                new AspectInstance(aspects.getOrThrow(aspect), 1),
                TTItems.ESSENTIA_CRYSTAL.get());
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