package com.blackmoss.impetusrift.data;

import com.blackmoss.impetusrift.ImpetusRift;
import com.blackmoss.impetusrift.block.ImpetusAltarBaseBlock;
import com.blackmoss.impetusrift.client.model.RiftCompassAngleProperty;
import com.blackmoss.impetusrift.registry.IRBlocks;
import com.blackmoss.impetusrift.registry.IRItems;
import com.leclowndu93150.thaumaturge.TTIds;
import net.minecraft.client.color.item.Dye;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
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
import net.minecraft.core.registries.BuiltInRegistries;
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

public class ModModelProvider extends ModelProvider {
    public ModModelProvider(PackOutput output) {
        super(output, ImpetusRift.MODID);
    }

    private static void generateCubeAllBlock(@NonNull BlockModelGenerators blockModels, @NonNull ItemModelGenerators itemModels, @NonNull Block block) {
        Identifier blockId = BuiltInRegistries.BLOCK.getKey(block);
        Identifier modelId = ImpetusRift.identifier("block/" + blockId.getPath());
        ModelTemplates.CUBE_ALL.create(modelId, TextureMapping.cube(block), blockModels.modelOutput);
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, BlockModelGenerators.plainVariant(modelId)));
        itemModels.itemModelOutput.accept(block.asItem(), ItemModelUtils.plainModel(modelId));
    }

    private static void customBlock(@NonNull BlockModelGenerators blockModels, @NonNull ItemModelGenerators itemModels, @NonNull Block block) {
        Identifier blockId = BuiltInRegistries.BLOCK.getKey(block);
        Identifier modelId = ImpetusRift.identifier("block/" + blockId.getPath());
        MultiVariant variant = new MultiVariant(WeightedList.of(new Variant(modelId)));
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, variant));
        itemModels.itemModelOutput.accept(block.asItem(), ItemModelUtils.plainModel(modelId));
    }

    private static void generateBlockStateOnly(@NonNull BlockModelGenerators blockModels, @NonNull Block block) {
        Identifier blockId = BuiltInRegistries.BLOCK.getKey(block);
        Identifier modelId = ImpetusRift.identifier("block/" + blockId.getPath());
        blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(block, BlockModelGenerators.plainVariant(modelId)));
    }

    private static void generateItemModelOnly(@NonNull ItemModelGenerators itemModels, @NonNull Block block) {
        Identifier blockId = BuiltInRegistries.BLOCK.getKey(block);
        Identifier modelId = ImpetusRift.identifier("block/" + blockId.getPath());
        itemModels.itemModelOutput.accept(block.asItem(), ItemModelUtils.plainModel(modelId));
    }

    private static void objBlock(@NonNull BlockModelGenerators blockModels, @NonNull ItemModelGenerators itemModels, @NonNull Block block) {
        Identifier blockId = BuiltInRegistries.BLOCK.getKey(block);
        Identifier modelId = ImpetusRift.identifier("block/" + blockId.getPath());
        blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(block, BlockModelGenerators.plainVariant(modelId)));
        itemModels.itemModelOutput.accept(block.asItem(), ItemModelUtils.plainModel(modelId));
    }

    private static void generateFacingBlock(@NonNull BlockModelGenerators blockModels, @NonNull ItemModelGenerators itemModels, @NonNull Block block) {
        Identifier blockId = BuiltInRegistries.BLOCK.getKey(block);
        Identifier modelId = ImpetusRift.identifier("block/" + blockId.getPath());
        MultiVariant variant = new MultiVariant(WeightedList.of(new Variant(modelId)));

        blockModels.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(block, variant)
                        .with(BlockModelGenerators.ROTATION_FACING));
        itemModels.itemModelOutput.accept(block.asItem(), ItemModelUtils.plainModel(modelId));
    }

    private static void riftFeederBlockState(@NonNull BlockModelGenerators blockModels, @NonNull ItemModelGenerators itemModels, @NonNull Block block) {
        Identifier blockId = BuiltInRegistries.BLOCK.getKey(block);
        Identifier modelId = ImpetusRift.identifier("block/" + blockId.getPath());
        itemModels.itemModelOutput.accept(block.asItem(), ItemModelUtils.plainModel(modelId));

        Variant base = new Variant(modelId);
        VariantMutator x90 = BlockModelGenerators.X_ROT_90;
        VariantMutator x270 = BlockModelGenerators.X_ROT_270;
        VariantMutator y90 = BlockModelGenerators.Y_ROT_90;
        VariantMutator y180 = BlockModelGenerators.Y_ROT_180;
        VariantMutator y270 = BlockModelGenerators.Y_ROT_270;

        blockModels.blockStateOutput.accept(
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

    private static void directionalBlockState(@NonNull BlockModelGenerators blockModels, @NonNull ItemModelGenerators itemModels, @NonNull Block block) {
        Identifier blockId = BuiltInRegistries.BLOCK.getKey(block);
        Identifier modelId = ImpetusRift.identifier("block/" + blockId.getPath());
        itemModels.itemModelOutput.accept(block.asItem(), ItemModelUtils.plainModel(modelId));

        Variant base = new Variant(modelId);
        VariantMutator x90 = BlockModelGenerators.X_ROT_90;
        VariantMutator x180 = BlockModelGenerators.X_ROT_180;
        VariantMutator y90 = BlockModelGenerators.Y_ROT_90;
        VariantMutator y180 = BlockModelGenerators.Y_ROT_180;
        VariantMutator y270 = BlockModelGenerators.Y_ROT_270;

        blockModels.blockStateOutput.accept(
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

    private static void flatItem(@NonNull ItemModelGenerators itemModels, @NonNull Item item) {
        Identifier itemId = BuiltInRegistries.ITEM.getKey(item);
        Identifier modelId = ImpetusRift.identifier("item/" + itemId.getPath());
        ModelTemplates.FLAT_ITEM.create(modelId, TextureMapping.layer0(item), itemModels.modelOutput);
        itemModels.itemModelOutput.accept(item, ItemModelUtils.plainModel(modelId));
    }

    private static void riftCompassItem(@NonNull ItemModelGenerators itemModels, @NonNull Item item) {
        Identifier itemId = BuiltInRegistries.ITEM.getKey(item);
        String itemName = itemId.getPath();

        List<RangeSelectItemModel.Entry> overrides = new ArrayList<>();

        Identifier baseModelId = ImpetusRift.identifier("item/" + itemName + "_16");
        Material baseMaterial = new Material(ImpetusRift.identifier("item/" + itemName + "_16"));
        ModelTemplates.FLAT_ITEM.create(baseModelId, TextureMapping.layer0(baseMaterial), itemModels.modelOutput);
        ItemModel.Unbaked base = ItemModelUtils.plainModel(baseModelId);
        overrides.add(ItemModelUtils.override(base, 0.0F));

        for (int i = 1; i < 32; ++i) {
            int textureIndex = Mth.positiveModulo(i - 16, 32);
            String suffix = String.format(Locale.ROOT, "_%02d", textureIndex);
            Identifier modelId = ImpetusRift.identifier("item/" + itemName + suffix);
            Material material = new Material(ImpetusRift.identifier("item/" + itemName + suffix));
            ModelTemplates.FLAT_ITEM.create(modelId, TextureMapping.layer0(material), itemModels.modelOutput);
            ItemModel.Unbaked overrideModel = ItemModelUtils.plainModel(modelId);
            overrides.add(ItemModelUtils.override(overrideModel, (float) i - 0.5F));
        }

        overrides.add(ItemModelUtils.override(base, 31.5F));

        ItemModel.Unbaked rangeSelect = ItemModelUtils.rangeSelect(
                new RiftCompassAngleProperty(), 32.0F, overrides);

        itemModels.itemModelOutput.accept(item, rangeSelect);
    }

    private static void riftTravellerBootsItem(@NonNull ItemModelGenerators itemModels, @NonNull Item item) {
        Identifier identifier = ImpetusRift.identifier("item/rift_traveller_boots");
        Material clothTex = new Material(ImpetusRift.identifier("item/rift_traveller_boots_overlay"));
        Material metalTex = new Material(identifier);
        ModelTemplates.TWO_LAYERED_ITEM.create(identifier, TextureMapping.layered(clothTex, metalTex), itemModels.modelOutput);
        itemModels.itemModelOutput.accept(item, ItemModelUtils.tintedModel(identifier, new Dye(-9815936)));
    }

    private static void thaumostaticElytra(@NonNull ItemModelGenerators itemModels, @NonNull Item item) {
        String path = BuiltInRegistries.ITEM.getKey(item).getPath();

        Identifier normalModel = ImpetusRift.identifier("item/" + path);
        Material wingTex = new Material(Identifier.withDefaultNamespace("item/elytra"));
        Material baseTex = new Material(TTIds.rl("item/thaumostatic_harness"));
        ModelTemplates.TWO_LAYERED_ITEM.create(normalModel, TextureMapping.layered(wingTex, baseTex), itemModels.modelOutput);

        Identifier brokenModel = ImpetusRift.identifier("item/" + path + "_broken");
        Material brokenTex = new Material(Identifier.withDefaultNamespace("item/elytra_broken"));
        ModelTemplates.FLAT_ITEM.create(brokenModel, TextureMapping.layer0(brokenTex), itemModels.modelOutput);

        itemModels.itemModelOutput.accept(item, ItemModelUtils.conditional(new Broken(),
                ItemModelUtils.plainModel(brokenModel),
                ItemModelUtils.plainModel(normalModel)));
    }

    private static void impetusAltarBase(@NonNull BlockModelGenerators blockModels, @NonNull ItemModelGenerators itemModels, @NonNull Block block) {
        Identifier blockId = BuiltInRegistries.BLOCK.getKey(block);
        Identifier baseModel = ImpetusRift.identifier("block/" + blockId.getPath());
        Identifier cellModel = ImpetusRift.identifier("block/" + blockId.getPath() + "_cell");

        itemModels.itemModelOutput.accept(block.asItem(), ItemModelUtils.plainModel(baseModel));

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

        blockModels.blockStateOutput.accept(multipart);
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
    protected void registerModels(@NonNull BlockModelGenerators blockModels, @NonNull ItemModelGenerators itemModels) {
        customBlock(blockModels, itemModels, IRBlocks.IMPETUS_MATRIX.get());
        customBlock(blockModels, itemModels, IRBlocks.IMPETUS_MIRROR.get());
        customBlock(blockModels, itemModels, IRBlocks.IMPETUS_DRAINER.get());
        customBlock(blockModels, itemModels, IRBlocks.IMPETUS_DIFFUSER.get());
        directionalBlockState(blockModels, itemModels, IRBlocks.IMPETUS_GATE.get());
        riftFeederBlockState(blockModels, itemModels, IRBlocks.RIFT_FEEDER.get());
        customBlock(blockModels, itemModels, IRBlocks.RIFT_JAR.get());

        directionalBlockState(blockModels, itemModels, IRBlocks.IMPETUS_GENERATOR.get());
        directionalBlockState(blockModels, itemModels, IRBlocks.IMPETUS_RELAY.get());

        objBlock(blockModels, itemModels, IRBlocks.RIFT_DIOPTRA.get());
        objBlock(blockModels, itemModels, IRBlocks.RIFT_MOVER_INPUT.get());
        objBlock(blockModels, itemModels, IRBlocks.RIFT_MOVER_OUTPUT.get());

        impetusAltarBase(blockModels, itemModels, IRBlocks.IMPETUS_ALTAR_BASE.get());

        flatItem(itemModels, IRItems.IMPETUS_CELL.get());
        flatItem(itemModels, IRItems.IMPETUS_GEM.get());
        flatItem(itemModels, IRItems.RIFT_SEED_FLUX.get());
        flatItem(itemModels, IRItems.IMPETUS_LINKER.get());

        riftCompassItem(itemModels, IRItems.RIFT_COMPASS.get());
        riftTravellerBootsItem(itemModels, IRItems.RIFT_TRAVELLER_BOOTS.get());
        thaumostaticElytra(itemModels, IRItems.THAUMOSTATIC_ELYTRA.get());
        flatItem(itemModels, IRItems.ELYTRA_THRUSTER.get());
    }
}
