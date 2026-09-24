package com.blackmoss.impetusrift.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class ImpetusAltarBaseBlock extends BaseEntityBlock {
    public static final EnumProperty<Direction> FACING =
            EnumProperty.create("facing", Direction.class, Direction.UP, Direction.DOWN);

    public ImpetusAltarBaseBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(@NonNull BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, Direction.UP);
    }

    @Override
    protected @NonNull InteractionResult useWithoutItem(
            @NonNull BlockState state,
            @NonNull Level level,
            @NonNull BlockPos pos,
            @NonNull Player player,
            @NonNull BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof ImpetusAltarBaseBlockEntity altarBase) {
            Direction clickedFace = hitResult.getDirection();
            InteractionResult result = altarBase.handleInteraction(player, InteractionHand.MAIN_HAND, clickedFace);
            if (result.consumesAction()) {
                return result;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    protected @NonNull InteractionResult useItemOn(
            @NonNull ItemStack stack,
            @NonNull BlockState state,
            @NonNull Level level,
            @NonNull BlockPos pos,
            @NonNull Player player,
            @NonNull InteractionHand hand,
            @NonNull BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof ImpetusAltarBaseBlockEntity altarBase) {
            Direction clickedFace = hitResult.getDirection();
            InteractionResult result = altarBase.handleInteraction(player, hand, clickedFace);
            if (result.consumesAction()) {
                return result;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NonNull BlockPos pos, @NonNull BlockState state) {
        return new ImpetusAltarBaseBlockEntity(pos, state);
    }

    @Override
    public @NonNull RenderShape getRenderShape(@NonNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public boolean hasAnalogOutputSignal(@NonNull BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(
            @NonNull BlockState state,
            @NonNull Level level,
            @NonNull BlockPos pos,
            @NonNull Direction direction) {
        BlockPos abovePos = pos.above();
        BlockPos belowPos = pos.below();

        if (level.getBlockEntity(abovePos) instanceof ImpetusMatrixBlockEntity matrix) {
            return matrix.getComparatorOutput();
        }
        if (level.getBlockEntity(belowPos) instanceof ImpetusMatrixBlockEntity matrix) {
            return matrix.getComparatorOutput();
        }
        return 0;
    }

    @Override
    protected @NonNull MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(ImpetusAltarBaseBlock::new);
    }
}