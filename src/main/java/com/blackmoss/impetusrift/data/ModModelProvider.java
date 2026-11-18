package com.blackmoss.impetusrift.data;

import com.blackmoss.impetusrift.ImpetusRift;
import com.blackmoss.impetusrift.block.ImpetusAltarBaseBlock;
import com.blackmoss.impetusrift.client.model.RiftCompassAngleProperty;
import com.blackmoss.impetusrift.registry.IRBlocks;
import com.blackmoss.impetusrift.registry.IRItems;
import com.blackmoss.lazycat.api.LazyIdentifier;
import com.blackmoss.lazycat.api.model.ModelContext;
import com.blackmoss.lazycat.model.LazyCatModelProvider;
import com.leclowndu93150.thaumaturge.TTIds;
import net.minecraft.client.color.item.Dye;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.ConditionBuilder;
import net.minecraft.client.data.models.blockstates.MultiPartGenerator;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.renderer.block.dispatch.VariantMutator;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.RangeSelectItemModel;
import net.minecraft.client.renderer.item.properties.conditional.Broken;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ModModelProvider extends LazyCatModelProvider {
    private ModelContext context;

    public ModModelProvider(PackOutput output) {
        super(output, ImpetusRift.MODID);
    }

    private void riftFeeder(@NonNull Block block) {
        blockItemModel(block);
        Variant base = new Variant(getBlockModelId(block));
        VariantMutator x90 = BlockModelGenerators.X_ROT_90;
        VariantMutator x270 = BlockModelGenerators.X_ROT_270;
        VariantMutator y90 = BlockModelGenerators.Y_ROT_90;
        VariantMutator y180 = BlockModelGenerators.Y_ROT_180;
        VariantMutator y270 = BlockModelGenerators.Y_ROT_270;

        context.blockModels().blockStateOutput.accept(
                MultiVariantGenerator.dispatch(block)
                        .with(PropertyDispatch.initial(BlockStateProperties.FACING)
                                .generate((facing) -> {
                                    Variant v = base;
                                    switch (facing) {
                                        case NORTH -> {
                                        }
                                        case SOUTH -> v = y180.apply(v);
                                        case WEST -> v = y270.apply(v);
                                        case EAST -> v = y90.apply(v);
                                        case UP -> v = x270.apply(v);
                                        case DOWN -> v = x90.apply(v);
                                    }
                                    return new MultiVariant(WeightedList.of(v));
                                })));
    }

    private void directionalBlockState(@NonNull Block block) {
        blockItemModel(block);

        Variant base = new Variant(getBlockModelId(block));
        VariantMutator x90 = BlockModelGenerators.X_ROT_90;
        VariantMutator x180 = BlockModelGenerators.X_ROT_180;
        VariantMutator y90 = BlockModelGenerators.Y_ROT_90;
        VariantMutator y180 = BlockModelGenerators.Y_ROT_180;
        VariantMutator y270 = BlockModelGenerators.Y_ROT_270;

        context.blockModels().blockStateOutput.accept(
                MultiVariantGenerator.dispatch(block)
                        .with(PropertyDispatch.initial(BlockStateProperties.FACING)
                                .generate((facing) -> {
                                    Variant v = base;
                                    switch (facing) {
                                        case NORTH -> v = x90.apply(v);
                                        case EAST -> {
                                            v = x90.apply(v);
                                            v = y90.apply(v);
                                        }
                                        case SOUTH -> {
                                            v = x90.apply(v);
                                            v = y180.apply(v);
                                        }
                                        case WEST -> {
                                            v = x90.apply(v);
                                            v = y270.apply(v);
                                        }
                                        case UP -> {
                                        }
                                        case DOWN -> v = x180.apply(v);
                                    }
                                    return new MultiVariant(WeightedList.of(v));
                                })));
    }

    private void riftCompass(@NonNull Item item) {
        var itemName = LazyIdentifier.getItemPath(item);
        List<RangeSelectItemModel.Entry> overrides = new ArrayList<>();

        Identifier baseModelId = ImpetusRift.identifier("item/" + LazyIdentifier.getItemPath(item) + "_16");
        Material baseMaterial = new Material(ImpetusRift.identifier("item/" + itemName + "_16"));
        ModelTemplates.FLAT_ITEM.create(baseModelId, TextureMapping.layer0(baseMaterial), context.itemModels().modelOutput);
        ItemModel.Unbaked base = ItemModelUtils.plainModel(baseModelId);
        overrides.add(ItemModelUtils.override(base, 0.0F));

        for (int i = 1; i < 32; ++i) {
            int textureIndex = Mth.positiveModulo(i - 16, 32);
            String suffix = String.format(Locale.ROOT, "_%02d", textureIndex);
            Identifier modelId = ImpetusRift.identifier("item/" + itemName + suffix);
            Material material = new Material(ImpetusRift.identifier("item/" + itemName + suffix));
            ModelTemplates.FLAT_ITEM.create(modelId, TextureMapping.layer0(material), context.itemModels().modelOutput);
            ItemModel.Unbaked overrideModel = ItemModelUtils.plainModel(modelId);
            overrides.add(ItemModelUtils.override(overrideModel, (float) i - 0.5F));
        }

        overrides.add(ItemModelUtils.override(base, 31.5F));

        ItemModel.Unbaked rangeSelect = ItemModelUtils.rangeSelect(
                new RiftCompassAngleProperty(), 32.0F, overrides);

        context.itemModels().itemModelOutput.accept(item, rangeSelect);
    }

    private void riftTravellerBoots(@NonNull Item item) {
        Identifier identifier = getItemModelId(item);
        Material clothTex = new Material(ImpetusRift.identifier("item/rift_traveller_boots_overlay"));
        Material metalTex = new Material(identifier);
        ModelTemplates.TWO_LAYERED_ITEM.create(identifier, TextureMapping.layered(clothTex, metalTex), context.itemModels().modelOutput);
        context.itemModels().itemModelOutput.accept(item, ItemModelUtils.tintedModel(identifier, new Dye(-9815936)));
    }

    private void thaumostaticElytra(@NonNull Item item) {
        Identifier normalModel = getItemModelId(item);

        Material wingTex = new Material(Identifier.withDefaultNamespace("item/elytra"));
        Material baseTex = new Material(TTIds.rl("item/thaumostatic_harness"));
        ModelTemplates.TWO_LAYERED_ITEM.create(normalModel, TextureMapping.layered(wingTex, baseTex), context.itemModels().modelOutput);

        Identifier brokenModel = ImpetusRift.identifier("item/" + LazyIdentifier.getItemPath(item) + "_broken");
        Material brokenTex = new Material(Identifier.withDefaultNamespace("item/elytra_broken"));
        ModelTemplates.FLAT_ITEM.create(brokenModel, TextureMapping.layer0(brokenTex), context.itemModels().modelOutput);

        context.itemModels().itemModelOutput.accept(item, ItemModelUtils.conditional(new Broken(),
                ItemModelUtils.plainModel(brokenModel),
                ItemModelUtils.plainModel(normalModel)));
    }

    private void impetusAltarBase(@NonNull Block block) {
        Identifier baseModel = getBlockModelId(block);
        Identifier cellModel = ImpetusRift.identifier("block/" + LazyIdentifier.getBlockPath(block) + "_cell");

        blockItemModel(block);
        MultiPartGenerator multipart = MultiPartGenerator.multiPart(block);

        multipart.with(
                new ConditionBuilder().term(ImpetusAltarBaseBlock.FACING, Direction.UP).build(),
                buildVariant(baseModel));
        multipart.with(
                new ConditionBuilder().term(ImpetusAltarBaseBlock.FACING, Direction.DOWN).build(),
                buildVariant(baseModel, BlockModelGenerators.X_ROT_180));

        addCellPart(multipart, cellModel, ImpetusAltarBaseBlock.CELL_NORTH, BlockModelGenerators.NOP, BlockModelGenerators.Y_ROT_180);
        addCellPart(multipart, cellModel, ImpetusAltarBaseBlock.CELL_SOUTH, BlockModelGenerators.Y_ROT_180, BlockModelGenerators.NOP);
        addCellPart(multipart, cellModel, ImpetusAltarBaseBlock.CELL_WEST, BlockModelGenerators.Y_ROT_270, BlockModelGenerators.Y_ROT_90);
        addCellPart(multipart, cellModel, ImpetusAltarBaseBlock.CELL_EAST, BlockModelGenerators.Y_ROT_90, BlockModelGenerators.Y_ROT_270);

        context.blockModels().blockStateOutput.accept(multipart);
    }

    private static void addCellPart(@NonNull MultiPartGenerator multipart, @NonNull Identifier cellModel, @NonNull BooleanProperty cell, @NonNull VariantMutator upYRot, @NonNull VariantMutator downYRot) {
        multipart.with(
                new ConditionBuilder().term(cell, true).term(ImpetusAltarBaseBlock.FACING, Direction.UP).build(),
                buildVariant(cellModel, upYRot));
        multipart.with(
                new ConditionBuilder().term(cell, true).term(ImpetusAltarBaseBlock.FACING, Direction.DOWN).build(),
                buildVariant(cellModel, downYRot, BlockModelGenerators.X_ROT_180));
    }

    private static MultiVariant buildVariant(@NonNull Identifier model, @NonNull VariantMutator... mutators) {
        Variant variant = new Variant(model);
        for (VariantMutator mutator : mutators) {
            variant = mutator.apply(variant);
        }
        return new MultiVariant(WeightedList.of(variant));
    }

    @Override
    protected void addModels(ModelContext context) {
        this.context = context;
        blockCustom(IRBlocks.IMPETUS_MATRIX.get());
        blockCustom(IRBlocks.IMPETUS_MIRROR.get());
        blockCustom(IRBlocks.IMPETUS_DRAINER.get());
        blockCustom(IRBlocks.IMPETUS_DIFFUSER.get());
        directionalBlockState(IRBlocks.IMPETUS_GATE.get());
        riftFeeder(IRBlocks.FLUX_INDUCER.get());
        blockCustom(IRBlocks.RIFT_JAR.get());

        directionalBlockState(IRBlocks.IMPETUS_GENERATOR.get());
        directionalBlockState(IRBlocks.IMPETUS_RELAY.get());

        blockObj(IRBlocks.EXTRADIMENSIONAL_MONITOR.get());
        blockObj(IRBlocks.METASPATIAL_ACCUMULATOR.get());
        blockObj(IRBlocks.METASPATIAL_EXTRUDER.get());

        impetusAltarBase(IRBlocks.IMPETUS_ALTAR_BASE.get());

        itemFlat(IRItems.IMPETUS_CELL.get());
        itemFlat(IRItems.IMPETUS_GEM.get());
        itemFlat(IRItems.RIFT_SEED_FLUX.get());
        itemFlat(IRItems.IMPETUS_LINKER.get());

        riftCompass(IRItems.RIFT_COMPASS.get());
        riftTravellerBoots(IRItems.RIFT_TRAVELLER_BOOTS.get());
        thaumostaticElytra(IRItems.THAUMOSTATIC_ELYTRA.get());
        itemFlat(IRItems.ELYTRA_THRUSTER.get());
    }
}
