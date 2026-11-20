package com.blackmoss.impetusrift.data;

import com.blackmoss.impetusrift.registry.IRBlocks;
import com.blackmoss.lazycat.recipe.thaumaturge.DustTriggerMultiblockBuilder;
import com.blackmoss.thaumaturgeadditions.registry.TAItems;
import com.blackmoss.impetusrift.ImpetusRift;
import com.blackmoss.impetusrift.registry.IRItems;
import com.blackmoss.lazycat.api.recipe.thaumaturge.ThaumaturgeUtils;
import com.blackmoss.lazycat.recipe.LazyCatRecipeProvider;
import com.blackmoss.lazycat.recipe.thaumaturge.ArcaneShapedBuilder;
import com.blackmoss.lazycat.recipe.thaumaturge.CrucibleBuilder;
import com.blackmoss.lazycat.recipe.thaumaturge.InfusionBuilder;
import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.recipe.ResearchGate;
import com.leclowndu93150.thaumaturge.registry.TTItemTags;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.Tags;
import org.jspecify.annotations.NonNull;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public final class ModRecipeProvider extends LazyCatRecipeProvider {
    private ModRecipeProvider(HolderLookup.Provider provider, RecipeOutput output, String modid) {
        super(provider, output, modid);
    }

    private static ResearchGate irGate(String path) {
        return new ResearchGate(ImpetusRift.identifier(path), Optional.empty(), false);
    }

    private static ResearchGate ttGate(String path) {
        return new ResearchGate(TTIds.rl(path), Optional.empty(), false);
    }

    @Override
    protected void addRecipes() {
        // 元动能量单元
        new InfusionBuilder(context,
                IRItems.IMPETUS_CELL, TTItems.VOID_SEED)
                .component(TTItemTags.PLATES_VOID_METAL)
                .component(crystal(TTAspects.ALIENIS))
                .component(TTItemTags.PLATES_VOID_METAL)
                .component(TTItems.PRIMORDIAL_PEARL)
                .component(TTItemTags.PLATES_VOID_METAL)
                .component(TTItemTags.GEMS_AMBER)
                .component(TTItemTags.PLATES_VOID_METAL)
                .component(Tags.Items.DUSTS_REDSTONE)
                .aspect(TTAspects.ALIENIS, 25)
                .aspect(TTAspects.POTENTIA, 100)
                .aspect(TTAspects.VACUOS, 50)
                .save();

        // 元动宝石
        new CrucibleBuilder(context,
                IRItems.IMPETUS_GEM,
                TTItems.VOID_SEED)
                .aspect(TTAspects.VACUOS, 10)
                .save();

        // 裂隙指南针
        new ArcaneShapedBuilder(context,
                IRItems.RIFT_COMPASS, 50)
                .pattern(" P ")
                .pattern("PSP")
                .pattern(" P ")
                .define('P', TTItemTags.PLATES_BRASS)
                .define('S', TTItems.SALIS_MUNDUS)
                .aspect(TTAspects.PERDITIO)
                .aspect(TTAspects.ORDO)
                .save();

        // 元动谐振器
        new ArcaneShapedBuilder(context,
                IRItems.IMPETUS_LINKER, 40)
                .pattern("PP")
                .pattern("GP")
                .pattern("R ")
                .define('P', TTItemTags.PLATES_VOID_METAL)
                .define('G', IRItems.IMPETUS_GEM)
                .define('R', Tags.Items.RODS_WOODEN)
                .aspect(TTAspects.IGNIS)
                .aspect(TTAspects.ORDO)
                .save();

        // 元动汲取仪
        new ArcaneShapedBuilder(context,
                IRItems.IMPETUS_DRAINER,
                50)
                .pattern("PVP")
                .pattern("PGP")
                .pattern("SBS")
                .define('P', TTItemTags.PLATES_VOID_METAL)
                .define('V', TTItemTags.STORAGE_BLOCKS_VOID_METAL)
                .define('G', IRItems.IMPETUS_GEM)
                .define('S', TTItems.ELDRITCH_STONE)
                .define('B', Tags.Items.BARS_IRON)
                .aspect(TTAspects.IGNIS)
                .aspect(TTAspects.ORDO)
                .aspect(TTAspects.PERDITIO)
                .save();

        // 元动弥散仪
        new ArcaneShapedBuilder(context,
                IRItems.IMPETUS_DIFFUSER,
                50)
                .pattern("I I")
                .pattern("PGP")
                .pattern("SPS")
                .define('I', TTItemTags.PLATES_IRON)
                .define('P', TTItemTags.PLATES_VOID_METAL)
                .define('G', IRItems.IMPETUS_GEM)
                .define('S', TTItems.STONE_ANCIENT)
                .aspect(TTAspects.IGNIS)
                .aspect(TTAspects.ORDO)
                .aspect(TTAspects.PERDITIO)
                .save();

        // 元动发电机
        new ArcaneShapedBuilder(context,
                IRItems.IMPETUS_GENERATOR,
                100)
                .pattern("SRS")
                .pattern("rPr")
                .pattern("SGS")
                .define('S', TTItems.ELDRITCH_STONE)
                .define('R', Tags.Items.DUSTS_REDSTONE)
                .define('r', TTItems.RARE_EARTH)
                .define('P', Items.PISTON)
                .define('G', IRItems.IMPETUS_GEM)
                .aspect(TTAspects.AER)
                .aspect(TTAspects.IGNIS)
                .aspect(TTAspects.ORDO)
                .aspect(TTAspects.PERDITIO)
                .save();

        // 裂隙旅行者之靴
        new InfusionBuilder(context,
                IRItems.RIFT_TRAVELLER_BOOTS,
                TTItems.VOID_BOOTS)
                .component(TTItems.FABRIC)
                .component(TTItems.TRAVELLER_BOOTS)
                .component(TTItemTags.PLATES_VOID_METAL)
                .component(TTItems.PRIMORDIAL_PEARL)
                .component(TTItems.FABRIC)
                .component(Tags.Items.FOODS_RAW_FISH)
                .component(TTItemTags.PLATES_VOID_METAL)
                .component(Tags.Items.FEATHERS)
                .aspect(TTAspects.VACUOS, 50)
                .aspect(TTAspects.ALIENIS, 50)
                .aspect(TTAspects.MOTUS, 50)
                .aspect(TTAspects.VOLATUS, 50)
                .instability(5)
                .save();

        // 裂隙旅行者之靴升级
        new InfusionBuilder(context,
                IRItems.RIFT_TRAVELLER_BOOTS,
                TAItems.VOID_TRAVELLER_BOOTS)
                .ifModLoaded("thaumaturgeadditions")
                .component(TTItems.SALIS_MUNDUS)
                .component(TTItems.FABRIC)
                .component(Tags.Items.LEATHERS)
                .component(TTItems.FABRIC)
                .aspect(TTAspects.VACUOS, 25)
                .aspect(TTAspects.ALIENIS, 25)
                .aspect(TTAspects.MOTUS, 25)
                .aspect(TTAspects.VOLATUS, 25)
                .instability(4)
                .save("rift_form_void_traveller_boots");

        // 奥法鞘翅
        new InfusionBuilder(context,
                IRItems.THAUMOSTATIC_ELYTRA,
                TTItems.THAUMOSTATIC_HARNESS)
                .component(TTItemTags.PLATES_VOID_METAL)
                .component(TTItems.VIS_RESONATOR)
                .component(Tags.Items.FEATHERS)
                .component(Items.ELYTRA)
                .aspect(TTAspects.VOLATUS, 50)
                .aspect(TTAspects.POTENTIA, 50)
                .aspect(TTAspects.MOTUS, 75)
                .instability(6)
                .save();

        // 鞘翅助推器
        new InfusionBuilder(context,
                IRItems.ELYTRA_THRUSTER,
                TTItems.GIRDLE_MUNDANE)
                .component(IRItems.IMPETUS_CELL)
                .component(crystal(TTAspects.ALIENIS))
                .component(TTItemTags.PLATES_VOID_METAL)
                .component(IRItems.IMPETUS_GEM)
                .component(crystal(TTAspects.VACUOS))
                .component(TTItemTags.PLATES_VOID_METAL)
                .aspect(TTAspects.ALIENIS, 25)
                .aspect(TTAspects.MOTUS, 75)
                .aspect(TTAspects.POTENTIA, 50)
                .aspect(TTAspects.VOLATUS, 45)
                .instability(7)
                .save();

        // 元动中继器
        new ArcaneShapedBuilder(context,
                IRItems.IMPETUS_RELAY,
                25)
                .pattern("G")
                .pattern("P")
                .define('G', IRItems.IMPETUS_GEM)
                .define('P', TTItemTags.PLATES_VOID_METAL)
                .aspect(TTAspects.IGNIS)
                .aspect(TTAspects.ORDO)
                .aspect(TTAspects.PERDITIO)
                .save();

        // 元动阀门
        new ArcaneShapedBuilder(context,
                IRItems.IMPETUS_GATE, 50)
                .pattern(" P ")
                .pattern("PRP")
                .pattern(" C ")
                .define('P', TTItemTags.PLATES_VOID_METAL)
                .define('R', IRItems.IMPETUS_RELAY)
                .define('C', Items.COMPARATOR)
                .aspect(TTAspects.TERRA)
                .aspect(TTAspects.ORDO)
                .aspect(TTAspects.PERDITIO)
                .save();

        // 裂隙封装仪
        new InfusionBuilder(context,
                IRItems.METASPATIAL_ACCUMULATOR,
                TTItems.AMBER_BLOCK)
                .component(TTItemTags.PLATES_IRON)
                .component(TTItems.STONE_ANCIENT)
                .component(TTItemTags.PLATES_VOID_METAL)
                .component(TTItems.MECHANISM_COMPLEX)
                .component(TTItemTags.PLATES_BRASS)
                .component(TTItems.STONE_ANCIENT)
                .component(TTItemTags.PLATES_VOID_METAL)
                .component(TTItems.VOID_SEED)
                .aspect(TTAspects.ALIENIS, 20)
                .aspect(TTAspects.MACHINA, 30)
                .aspect(TTAspects.PERMUTATIO, 10)
                .aspect(TTAspects.VOLATUS, 10)
                .aspect(TTAspects.VINCULUM, 50)
                .aspect(TTAspects.VITIUM, 25)
                .instability(4)
                .save();

        // 裂隙释放仪
        new InfusionBuilder(context,
                IRItems.METASPATIAL_EXTRUDER,
                TTItems.ALUMENTUM)
                .component(TTItems.MECHANISM_SIMPLE)
                .component(TTItemTags.PLATES_VOID_METAL)
                .component(TTItems.STONE_ANCIENT)
                .component(TTItemTags.PLATES_BRASS)
                .component(TTItemTags.PLATES_VOID_METAL)
                .component(TTItems.VOID_SEED)
                .aspect(TTAspects.ALIENIS, 20)
                .aspect(TTAspects.MACHINA, 30)
                .aspect(TTAspects.PERDITIO, 50)
                .aspect(TTAspects.PERMUTATIO, 10)
                .aspect(TTAspects.VITIUM, 25)
                .aspect(TTAspects.VOLATUS, 10)
                .instability(4)
                .save();

        // 裂隙罐子
        new InfusionBuilder(context,
                IRItems.RIFT_JAR,
                TTItems.JAR_VOID)
                .component(TTItemTags.PLATES_VOID_METAL)
                .component(IRItems.IMPETUS_GEM)
                .component(TTItemTags.PLATES_VOID_METAL)
                .component(IRItems.IMPETUS_GEM)
                .aspect(TTAspects.ALIENIS, 10)
                .aspect(TTAspects.VOLATUS, 5)
                .aspect(TTAspects.VITIUM, 25)
                .instability(4)
                .save();

        // 咒波引流仪
        new InfusionBuilder(context,
                IRItems.FLUX_INDUCER,
                TTItems.ESSENTIA_INPUT)
                .component(TTItems.MECHANISM_SIMPLE)
                .component(TTItemTags.PLATES_BRASS)
                .component(TTItems.TUBE)
                .component(TTItems.MORPHIC_RESONATOR)
                .component(TTItemTags.PLANKS_GREATWOOD)
                .component(TTItems.TUBE)
                .aspect(TTAspects.ALIENIS, 10)
                .aspect(TTAspects.MACHINA, 50)
                .aspect(TTAspects.PERMUTATIO, 50)
                .aspect(TTAspects.VOLATUS, 5)
                .aspect(TTAspects.VITIUM, 25)
                .instability(4)
                .save();

        // 裂隙检测仪
        new ArcaneShapedBuilder(context,
                IRItems.EXTRADIMENSIONAL_MONITOR,
                50)
                .pattern(" S ")
                .pattern("STS")
                .pattern("sss")
                .define('S', TTItems.SLAB_ELDRITCH)
                .define('T', TTItems.THAUMOMETER)
                .define('s', TTItems.ELDRITCH_STONE)
                .aspect(TTAspects.AER)
                .aspect(TTAspects.TERRA)
                .aspect(TTAspects.IGNIS)
                .aspect(TTAspects.AQUA)
                .aspect(TTAspects.ORDO)
                .aspect(TTAspects.PERDITIO)
                .save();

        new DustTriggerMultiblockBuilder(context,
                IRBlocks.IMPETUS_MATRIX.get(),
                "impetus_altar")
                .gate(irGate("impetus_altar"))
                .save();
    }

    private Ingredient crystal(ResourceKey<IAspect> aspect) {
        return ThaumaturgeUtils.crystal(registries, aspect);
    }

    public static final class Runner extends RecipeProvider.Runner {
        public Runner(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
            super(output, lookupProvider);
        }

        @Override
        protected @NonNull RecipeProvider createRecipeProvider(HolderLookup.@NonNull Provider provider, @NonNull RecipeOutput output) {
            return new ModRecipeProvider(provider, output, ImpetusRift.MODID);
        }

        @Override
        public @NonNull String getName() {
            return "Impetus Rift Recipes";
        }
    }
}