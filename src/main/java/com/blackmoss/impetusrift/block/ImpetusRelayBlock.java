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
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class ImpetusRelayBlock extends BaseEntityBlock {
    public static final MapCodec<ImpetusRelayBlock> CODEC = simpleCodec(ImpetusRelayBlock::new);

    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

    private static final double RELAY_CORE = 0.3125, RELAY_CORE_HI = 0.6875, RELAY_FACE = 0.53125;

    public ImpetusRelayBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    private static VoxelShape shapeFor(Direction dir) {
        double lo = RELAY_CORE, hi = RELAY_CORE_HI, face = RELAY_FACE;
        boolean positive = dir.getAxisDirection() == Direction.AxisDirection.POSITIVE;
        return switch (dir.getAxis()) {
            case X -> positive ? Shapes.box(0.0, lo, lo, face, hi, hi)
                    : Shapes.box(1 - face, lo, lo, 1.0, hi, hi);
            case Y -> positive ? Shapes.box(lo, 0.0, lo, hi, face, hi)
                    : Shapes.box(lo, 1 - face, lo, hi, 1.0, hi);
            case Z -> positive ? Shapes.box(lo, lo, 0.0, hi, hi, face)
                    : Shapes.box(lo, lo, 1 - face, hi, hi, 1.0);
        };
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(@NonNull BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getClickedFace());
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
        return shapeFor(state.getValue(FACING));
    }

    @Override
    public @NonNull VoxelShape getInteractionShape(
            @NonNull BlockState state,
            @NonNull BlockGetter level,
            @NonNull BlockPos pos) {
        return shapeFor(state.getValue(FACING));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NonNull BlockPos pos, @NonNull BlockState state) {
        return new ImpetusRelayBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            @NonNull Level level,
            @NonNull BlockState state,
            @NonNull BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, IRBlockEntities.IMPETUS_RELAY.get(), ImpetusRelayBlockEntity::staticTick);
    }
}
