package com.blackmoss.impetusrift.data;

import com.blackmoss.impetusrift.ImpetusRift;
import com.blackmoss.impetusrift.block.ImpetusAltarBaseBlock;
import com.blackmoss.impetusrift.client.model.RiftCompassAngleProperty;
import com.blackmoss.impetusrift.registry.IRBlocks;
import com.blackmoss.impetusrift.registry.IRItems;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.renderer.block.dispatch.VariantMutator;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.RangeSelectItemModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
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
        Identifier modelId = ImpetusRift.IRIdentifier("block/" + blockId.getPath());
        ModelTemplates.CUBE_ALL.create(modelId, TextureMapping.cube(block), blockModels.modelOutput);
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, BlockModelGenerators.plainVariant(modelId)));
        itemModels.itemModelOutput.accept(block.asItem(), ItemModelUtils.plainModel(modelId));
    }

    private static void generateCustomBlock(@NonNull BlockModelGenerators blockModels, @NonNull ItemModelGenerators itemModels, @NonNull Block block) {
        Identifier blockId = BuiltInRegistries.BLOCK.getKey(block);
        Identifier modelId = ImpetusRift.IRIdentifier("block/" + blockId.getPath());
        MultiVariant variant = new MultiVariant(WeightedList.of(new Variant(modelId)));
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, variant));
        itemModels.itemModelOutput.accept(block.asItem(), ItemModelUtils.plainModel(modelId));
    }

    private static void generateBlockStateOnly(@NonNull BlockModelGenerators blockModels, @NonNull Block block) {
        Identifier blockId = BuiltInRegistries.BLOCK.getKey(block);
        Identifier modelId = ImpetusRift.IRIdentifier("block/" + blockId.getPath());
        blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(block, BlockModelGenerators.plainVariant(modelId)));
    }

    private static void generateItemModelOnly(@NonNull ItemModelGenerators itemModels, @NonNull Block block) {
        Identifier blockId = BuiltInRegistries.BLOCK.getKey(block);
        Identifier modelId = ImpetusRift.IRIdentifier("block/" + blockId.getPath());
        itemModels.itemModelOutput.accept(block.asItem(), ItemModelUtils.plainModel(modelId));
    }

    private static void generateObjBlock(@NonNull BlockModelGenerators blockModels, @NonNull ItemModelGenerators itemModels, @NonNull Block block) {
        Identifier blockId = BuiltInRegistries.BLOCK.getKey(block);
        Identifier modelId = ImpetusRift.IRIdentifier("block/" + blockId.getPath());
        blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(block, BlockModelGenerators.plainVariant(modelId)));
        itemModels.itemModelOutput.accept(block.asItem(), ItemModelUtils.plainModel(modelId));
    }

    // 带 FACING 的方块：blockstate 按朝向旋转（用原版 ROTATION_FACING）
    private static void generateFacingBlock(@NonNull BlockModelGenerators blockModels, @NonNull ItemModelGenerators itemModels, @NonNull Block block) {
        Identifier blockId = BuiltInRegistries.BLOCK.getKey(block);
        Identifier modelId = ImpetusRift.IRIdentifier("block/" + blockId.getPath());
        MultiVariant variant = new MultiVariant(WeightedList.of(new Variant(modelId)));

        blockModels.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(block, variant)
                        .with(BlockModelGenerators.ROTATION_FACING));
        itemModels.itemModelOutput.accept(block.asItem(), ItemModelUtils.plainModel(modelId));
    }

    private static void generateFlatItem(@NonNull ItemModelGenerators itemModels, @NonNull Item item) {
        Identifier itemId = BuiltInRegistries.ITEM.getKey(item);
        Identifier modelId = ImpetusRift.IRIdentifier("item/" + itemId.getPath());
        ModelTemplates.FLAT_ITEM.create(modelId, TextureMapping.layer0(item), itemModels.modelOutput);
        itemModels.itemModelOutput.accept(item, ItemModelUtils.plainModel(modelId));
    }

    private static void generateRiftCompassItem(@NonNull ItemModelGenerators itemModels, @NonNull Item item) {
        Identifier itemId = BuiltInRegistries.ITEM.getKey(item);
        String itemName = itemId.getPath();

        List<RangeSelectItemModel.Entry> overrides = new ArrayList<>();

        Identifier baseModelId = ImpetusRift.IRIdentifier("item/" + itemName + "_16");
        Material baseMaterial = new Material(ImpetusRift.IRIdentifier("item/" + itemName + "_16"));
        ModelTemplates.FLAT_ITEM.create(baseModelId, TextureMapping.layer0(baseMaterial), itemModels.modelOutput);
        ItemModel.Unbaked base = ItemModelUtils.plainModel(baseModelId);
        overrides.add(ItemModelUtils.override(base, 0.0F));

        for (int i = 1; i < 32; ++i) {
            int textureIndex = Mth.positiveModulo(i - 16, 32);
            String suffix = String.format(Locale.ROOT, "_%02d", textureIndex);
            Identifier modelId = ImpetusRift.IRIdentifier("item/" + itemName + suffix);
            Material material = new Material(ImpetusRift.IRIdentifier("item/" + itemName + suffix));
            ModelTemplates.FLAT_ITEM.create(modelId, TextureMapping.layer0(material), itemModels.modelOutput);
            ItemModel.Unbaked overrideModel = ItemModelUtils.plainModel(modelId);
            overrides.add(ItemModelUtils.override(overrideModel, (float) i - 0.5F));
        }

        overrides.add(ItemModelUtils.override(base, 31.5F));

        ItemModel.Unbaked rangeSelect = ItemModelUtils.rangeSelect(
                new RiftCompassAngleProperty(), 32.0F, overrides);

        itemModels.itemModelOutput.accept(item, rangeSelect);
    }

    private static void generateImpetusAltarBase(@NonNull BlockModelGenerators blockModels, @NonNull ItemModelGenerators itemModels, @NonNull Block block) {
        Identifier blockId = BuiltInRegistries.BLOCK.getKey(block);
        Identifier modelId = ImpetusRift.IRIdentifier("block/" + blockId.getPath());

        itemModels.itemModelOutput.accept(block.asItem(), ItemModelUtils.plainModel(modelId));
        blockModels.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(block)
                        .with(PropertyDispatch.initial(ImpetusAltarBaseBlock.FACING)
                                .generate((facing) -> {
                                    VariantMutator rotation = facing == Direction.DOWN
                                            ? BlockModelGenerators.X_ROT_180
                                            : BlockModelGenerators.NOP;
                                    return new MultiVariant(WeightedList.of(rotation.apply(new Variant(modelId))));
                                })));
    }

    @Override
    protected void registerModels(@NonNull BlockModelGenerators blockModels, @NonNull ItemModelGenerators itemModels) {
        generateCustomBlock(blockModels, itemModels, IRBlocks.IMPETUS_MATRIX.get());
        generateCustomBlock(blockModels, itemModels, IRBlocks.IMPETUS_MIRROR.get());
        generateCustomBlock(blockModels, itemModels, IRBlocks.IMPETUS_DRAINER.get());
        generateCustomBlock(blockModels, itemModels, IRBlocks.IMPETUS_DIFFUSER.get());
        generateCustomBlock(blockModels, itemModels, IRBlocks.IMPETUS_GATE.get());
        generateCustomBlock(blockModels, itemModels, IRBlocks.RIFT_FEEDER.get());
        generateCustomBlock(blockModels, itemModels, IRBlocks.RIFT_JAR.get());
        generateCustomBlock(blockModels, itemModels, IRBlocks.IMPETUS_RELAY.get());

        generateCubeAllBlock(blockModels, itemModels, IRBlocks.RIFT_BARRIER.get());

        generateFacingBlock(blockModels, itemModels, IRBlocks.IMPETUS_GENERATOR.get());
        generateObjBlock(blockModels, itemModels, IRBlocks.RIFT_MONITOR.get());
        generateObjBlock(blockModels, itemModels, IRBlocks.RIFT_MOVER_INPUT.get());
        generateObjBlock(blockModels, itemModels, IRBlocks.RIFT_MOVER_OUTPUT.get());

        generateImpetusAltarBase(blockModels, itemModels, IRBlocks.IMPETUS_ALTAR_BASE.get());

        generateFlatItem(itemModels, IRItems.IMPETUS_CELL.get());
        generateFlatItem(itemModels, IRItems.IMPETUS_GEM.get());
        generateFlatItem(itemModels, IRItems.RIFT_SEED_FLUX.get());
        generateFlatItem(itemModels, IRItems.IMPETUS_LINKER.get());

        generateRiftCompassItem(itemModels, IRItems.RIFT_COMPASS.get());
    }
}
