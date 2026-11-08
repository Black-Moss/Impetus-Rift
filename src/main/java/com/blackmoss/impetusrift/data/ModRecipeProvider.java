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
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
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

    private static ResearchGate irGate(String path) {
        return new ResearchGate(ImpetusRift.identifier(path), Optional.empty(), false);
    }

    private static ResearchGate tcGate(String path) {
        return new ResearchGate(TTIds.rl(path), Optional.empty(), false);
    }

    @Override
    protected void buildRecipes() {
        // 元动能量单元
        infusion(IRItems.IMPETUS_CELL.get(), TTItems.VOID_SEED.get())
                .component(tag(TTItemTags.PLATES_VOID_METAL))
                .component(crystal(TTAspects.ALIENIS))
                .component(tag(TTItemTags.PLATES_VOID_METAL))
                .component(Ingredient.of(TTItems.PRIMORDIAL_PEARL.get()))
                .component(tag(TTItemTags.PLATES_VOID_METAL))
                .component(tag(TTItemTags.GEMS_AMBER))
                .component(tag(TTItemTags.PLATES_VOID_METAL))
                .component(tag(Tags.Items.DUSTS_REDSTONE))
                .aspect(TTAspects.ALIENIS, 25)
                .aspect(TTAspects.POTENTIA, 100)
                .aspect(TTAspects.VACUOS, 50)
                .save(output);

        // 元动宝石
        crucible(IRItems.IMPETUS_GEM.get(),
                TTItems.VOID_SEED.get())
                .aspect(TTAspects.VACUOS, 10)
                .save(output);

        // 裂隙指南针
        arcaneShaped(IRItems.RIFT_COMPASS, 50)
                .pattern(" P ")
                .pattern("PSP")
                .pattern(" P ")
                .define('P', TTItemTags.PLATES_BRASS)
                .define('S', TTItems.SALIS_MUNDUS)
                .aspect(TTAspects.PERDITIO)
                .aspect(TTAspects.ORDO)
                .save(output);

        // 元动谐振器
        arcaneShaped(IRItems.IMPETUS_LINKER.get(), 40)
                .pattern("PP")
                .pattern("GP")
                .pattern("R ")
                .define('P', TTItemTags.PLATES_VOID_METAL)
                .define('G', IRItems.IMPETUS_GEM.get())
                .define('R', Tags.Items.RODS_WOODEN)
                .aspect(TTAspects.IGNIS)
                .aspect(TTAspects.ORDO)
                .save(output);

        // 元动汲取仪
        arcaneShaped(IRItems.IMPETUS_DRAINER.get(),
                50)
                .pattern("PVP")
                .pattern("PGP")
                .pattern("SBS")
                .define('P', TTItemTags.PLATES_VOID_METAL)
                .define('V', TTItemTags.STORAGE_BLOCKS_VOID_METAL)
                .define('G', IRItems.IMPETUS_GEM.get())
                .define('S', TTItems.ELDRITCH_STONE)
                .define('B', Tags.Items.BARS_IRON)
                .aspect(TTAspects.IGNIS)
                .aspect(TTAspects.ORDO)
                .aspect(TTAspects.PERDITIO)
                .save(output);

        // 元动弥散仪
        arcaneShaped(IRItems.IMPETUS_DIFFUSER.get(),
                50)
                .pattern("I I")
                .pattern("PGP")
                .pattern("SPS")
                .define('I', TTItemTags.PLATES_IRON)
                .define('P', TTItemTags.PLATES_VOID_METAL)
                .define('G', IRItems.IMPETUS_GEM.get())
                .define('S', TTItems.STONE_ANCIENT)
                .aspect(TTAspects.IGNIS)
                .aspect(TTAspects.ORDO)
                .aspect(TTAspects.PERDITIO)
                .save(output);

        // 元动发电机
        arcaneShaped(IRItems.IMPETUS_GENERATOR.get(),
                100)
                .pattern("SRS")
                .pattern("rPr")
                .pattern("SGS")
                .define('S', TTItems.ELDRITCH_STONE)
                .define('R', Tags.Items.DUSTS_REDSTONE)
                .define('r', TTItems.RARE_EARTH)
                .define('P', Items.PISTON)
                .define('G', IRItems.IMPETUS_GEM.get())
                .aspect(TTAspects.AER)
                .aspect(TTAspects.IGNIS)
                .aspect(TTAspects.ORDO)
                .aspect(TTAspects.PERDITIO)
                .save(output);

        // 裂隙旅行者之靴
        infusion(IRItems.RIFT_TRAVELLER_BOOTS.get(),
                TTItems.VOID_BOOTS.get())
                .component(Ingredient.of(TTItems.FABRIC.get()))
                .component(Ingredient.of(TTItems.TRAVELLER_BOOTS.get()))
                .component(tag(TTItemTags.PLATES_VOID_METAL))
                .component(Ingredient.of(TTItems.PRIMORDIAL_PEARL))
                .component(Ingredient.of(TTItems.FABRIC.get()))
                .component(tag(Tags.Items.FOODS_RAW_FISH))
                .component(tag(TTItemTags.PLATES_VOID_METAL))
                .component(tag(Tags.Items.FEATHERS))
                .aspect(TTAspects.VACUOS, 50)
                .aspect(TTAspects.ALIENIS, 50)
                .aspect(TTAspects.MOTUS, 50)
                .aspect(TTAspects.VOLATUS, 50)
                .instability(5)
                .save(output);

        // 奥法鞘翅
        infusion(IRItems.THAUMOSTATIC_ELYTRA,
                TTItems.THAUMOSTATIC_HARNESS)
                .component(tag(TTItemTags.PLATES_VOID_METAL))
                .component(Ingredient.of(TTItems.VIS_RESONATOR))
                .component(tag(Tags.Items.FEATHERS))
                .component(Ingredient.of(Items.ELYTRA))
                .aspect(TTAspects.VOLATUS, 50)
                .aspect(TTAspects.POTENTIA, 50)
                .aspect(TTAspects.MOTUS, 75)
                .instability(6)
                .save(output);

        // 鞘翅助推器
        infusion(IRItems.ELYTRA_THRUSTER,
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

        // 元动中继器
        arcaneShaped(IRItems.IMPETUS_RELAY, 25)
                .pattern("G")
                .pattern("P")
                .define('G', IRItems.IMPETUS_GEM.get())
                .define('P', TTItemTags.PLATES_VOID_METAL)
                .aspect(TTAspects.IGNIS)
                .aspect(TTAspects.ORDO)
                .aspect(TTAspects.PERDITIO)
                .save(output);

        // 元动阀门
        arcaneShaped(IRItems.IMPETUS_GATE, 50)
                .pattern(" P ")
                .pattern("PRP")
                .pattern(" C ")
                .define('P', TTItemTags.PLATES_VOID_METAL)
                .define('R', IRItems.IMPETUS_RELAY)
                .define('C', Items.COMPARATOR)
                .aspect(TTAspects.TERRA)
                .aspect(TTAspects.ORDO)
                .aspect(TTAspects.PERDITIO)
                .save(output);
    }

    private CrucibleRecipeBuilder crucible(ItemLike result, ItemLike catalyst) {
        return (CrucibleRecipeBuilder) new CrucibleRecipeBuilder(
                aspects,
                RecipeCategory.MISC,
                new ItemStackTemplate(result.asItem()),
                Ingredient.of(catalyst))
                .unlockedBy("has", this.has(result.asItem()));
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

    private InfusionRecipeBuilder infusion(ItemLike result, ItemLike catalyst) {
        return (InfusionRecipeBuilder) new InfusionRecipeBuilder(
                aspects,
                RecipeCategory.MISC,
                new ItemStackTemplate(result.asItem()),
                Ingredient.of(catalyst))
                .unlockedBy("has", this.has(result));
    }


    private CrucibleRecipeBuilder crucible(ItemLike result, RecipeCategory category, ItemLike catalyst) {
        return (CrucibleRecipeBuilder) new CrucibleRecipeBuilder(
                aspects,
                category,
                new ItemStackTemplate(result.asItem()),
                Ingredient.of(catalyst))
                .unlockedBy("has", this.has(result));
    }

    private ArcaneWorkbenchShapelessRecipeBuilder arcaneShapeless(ItemLike result, int vis) {
        return (ArcaneWorkbenchShapelessRecipeBuilder) new ArcaneWorkbenchShapelessRecipeBuilder(
                RecipeCategory.MISC,
                new ItemStackTemplate(result.asItem()),
                aspects,
                vis,
                registries.lookupOrThrow(Registries.ITEM))
                .unlockedBy("has", this.has(result.asItem()));

    }

    private ArcaneWorkbenchShapedRecipeBuilder arcaneShaped(ItemLike result, int vis) {
        return (ArcaneWorkbenchShapedRecipeBuilder) new ArcaneWorkbenchShapedRecipeBuilder(
                RecipeCategory.MISC,
                new ItemStackTemplate(result.asItem()),
                this.items,
                aspects,
                vis)
                .unlockedBy("has", this.has(result.asItem()));

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