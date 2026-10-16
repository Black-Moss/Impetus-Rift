package com.blackmoss.impetusrift.block;

import com.blackmoss.impetusrift.registry.IRBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


public class ImpetusGateBlock extends BaseEntityBlock {
    public static final MapCodec<ImpetusGateBlock> CODEC = simpleCodec(ImpetusGateBlock::new);

    private static final VoxelShape SHAPE_UP = Block.box(5.0, 0.0, 5.0, 11.0, 8.5, 11.0);
    private static final VoxelShape SHAPE_DOWN = Block.box(5.0, 7.5, 5.0, 11.0, 16.0, 11.0);
    private static final VoxelShape SHAPE_NORTH = Block.box(5.0, 5.0, 7.5, 11.0, 11.0, 16.0);
    private static final VoxelShape SHAPE_SOUTH = Block.box(5.0, 5.0, 0.0, 11.0, 11.0, 8.5);
    private static final VoxelShape SHAPE_EAST = Block.box(0.0, 5.0, 5.0, 8.5, 11.0, 11.0);
    private static final VoxelShape SHAPE_WEST = Block.box(7.5, 5.0, 5.0, 16.0, 11.0, 11.0);

    public ImpetusGateBlock(Properties properties) {
        super(properties);
        registerDefaultState(getStateDefinition().any().setValue(BlockStateProperties.FACING, Direction.UP));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(@NonNull BlockPlaceContext context) {
        return defaultBlockState().setValue(BlockStateProperties.FACING, context.getClickedFace());
    }

    @Override
    protected @NonNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @NonNull RenderShape getRenderShape(@NonNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @NonNull VoxelShape getShape(
            @NonNull BlockState state,
            @NonNull BlockGetter level,
            @NonNull BlockPos pos,
            @NonNull CollisionContext context) {
        return switch (state.getValue(BlockStateProperties.FACING)) {
            case DOWN -> SHAPE_DOWN;
            case UP -> SHAPE_UP;
            case NORTH -> SHAPE_NORTH;
            case SOUTH -> SHAPE_SOUTH;
            case EAST -> SHAPE_EAST;
            case WEST -> SHAPE_WEST;
        };
    }

    @Override
    public void onPlace(
            @NonNull BlockState state,
            @NonNull Level level,
            @NonNull BlockPos pos,
            @NonNull BlockState oldState,
            boolean moved) {
        super.onPlace(state, level, pos, oldState, moved);
        updateFromRedstone(level, pos);
    }

    @Override
    protected void neighborChanged(
            @NonNull BlockState state,
            @NonNull Level level,
            @NonNull BlockPos pos,
            @NonNull Block block,
            @Nullable Orientation orientation,
            boolean moved) {
        updateFromRedstone(level, pos);
    }

    private void updateFromRedstone(@NonNull Level level, @NonNull BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof ImpetusGateBlockEntity gate) {
            gate.updateFromRedstone();
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NonNull BlockPos pos, @NonNull BlockState state) {
        return new ImpetusGateBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            @NonNull Level level,
            @NonNull BlockState state,
            @NonNull BlockEntityType<T> blockEntityType) {
        if (level.isClientSide()) {
            return null;
        }
        return createTickerHelper(blockEntityType, IRBlockEntities.IMPETUS_GATE.get(), ImpetusGateBlockEntity::staticTick);
    }
}
