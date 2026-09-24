package com.blackmoss.impetusrift.block;

import com.blackmoss.impetusrift.registry.IRBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class ImpetusDrainerBlock extends BaseEntityBlock {
    public static final MapCodec<ImpetusDrainerBlock> CODEC = simpleCodec(ImpetusDrainerBlock::new);
    public static final BooleanProperty ENABLED = BooleanProperty.create("enabled");
    private static final VoxelShape SHAPE = box(
            3.0F, 0.0F, 3.0F,
            13.0F, 16.0F, 13.0F);

    public ImpetusDrainerBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(ENABLED, true));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ENABLED);
    }

    @Override
    protected @NonNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected @NonNull VoxelShape getShape(
            @NonNull BlockState state,
            @NonNull BlockGetter level,
            @NonNull BlockPos pos,
            @NonNull CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected @NonNull VoxelShape getCollisionShape(
            @NonNull BlockState state,
            @NonNull BlockGetter level,
            @NonNull BlockPos pos,
            @NonNull CollisionContext context) {
        return SHAPE;
    }

    @Override
    public @NonNull RenderShape getRenderShape(@NonNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected void onPlace(
            @NonNull BlockState state,
            Level level,
            @NonNull BlockPos pos,
            @NonNull BlockState oldState,
            boolean moving) {
        if (!level.isClientSide()) {
            updateEnabledState(level, pos, state);
        }
    }

    private void updateEnabledState(Level level, BlockPos pos, BlockState state) {
        boolean powered = level.hasNeighborSignal(pos);
        boolean currentlyEnabled = state.getValue(ENABLED);
        if (powered == currentlyEnabled) {
            level.setBlock(pos, state.setValue(ENABLED, !powered), Block.UPDATE_ALL);
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NonNull BlockPos pos, @NonNull BlockState state) {
        return new ImpetusDrainerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level,
            @NonNull BlockState state,
            @NonNull BlockEntityType<T> blockEntityType) {
        if (level.isClientSide()) {
            return null;
        }
        return createTickerHelper(blockEntityType, IRBlockEntities.IMPETUS_DRAINER.get(), ImpetusDrainerBlockEntity::staticTick);
    }
}
