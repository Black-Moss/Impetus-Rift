package com.blackmoss.impetusrift.block;

import com.blackmoss.impetusrift.registry.IRBlocks;
import com.blackmoss.impetusrift.registry.IRItems;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ImpetusAltarBaseBlock extends BaseEntityBlock {
    public static final EnumProperty<Direction> FACING =
            EnumProperty.create("facing", Direction.class, Direction.UP, Direction.DOWN);
    public static final BooleanProperty CELL_NORTH = BooleanProperty.create("cell_north");
    public static final BooleanProperty CELL_SOUTH = BooleanProperty.create("cell_south");
    public static final BooleanProperty CELL_WEST = BooleanProperty.create("cell_west");
    public static final BooleanProperty CELL_EAST = BooleanProperty.create("cell_east");

    public ImpetusAltarBaseBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, CELL_NORTH, CELL_SOUTH, CELL_WEST, CELL_EAST);
    }

    public static BooleanProperty cellProperty(Direction dir) {
        return switch (dir) {
            case NORTH -> CELL_NORTH;
            case SOUTH -> CELL_SOUTH;
            case WEST -> CELL_WEST;
            case EAST -> CELL_EAST;
            default -> null;
        };
    }

    public static boolean hasCell(BlockState state, Direction dir) {
        BooleanProperty property = cellProperty(dir);
        return property != null && state.getValue(property);
    }

    public static BlockState setCell(BlockState state, Direction dir, boolean present) {
        BooleanProperty property = cellProperty(dir);
        return property == null ? state : state.setValue(property, present);
    }

    public static int countCells(BlockState state) {
        int count = 0;
        for (Direction dir : new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST}) {
            if (hasCell(state, dir)) {
                count++;
            }
        }
        return count;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(@NonNull BlockPlaceContext context) {
        return defaultBlockState()
                .setValue(FACING, Direction.UP)
                .setValue(CELL_NORTH, false)
                .setValue(CELL_SOUTH, false)
                .setValue(CELL_WEST, false)
                .setValue(CELL_EAST, false);
    }

    @Override
    public @NonNull List<ItemStack> getDrops(@NonNull BlockState state, LootParams.@NonNull Builder params) {
        List<ItemStack> drops = new ArrayList<>();
        drops.add(new ItemStack(TCBlocks.PEDESTAL_ELDRITCH.get()));
        int cells = countCells(state);
        if (cells > 0) {
            drops.add(new ItemStack(IRItems.IMPETUS_CELL.get(), cells));
        }
        return drops;
    }

    @Override
    protected void affectNeighborsAfterRemoval(@NonNull BlockState state, @NonNull ServerLevel level, @NonNull BlockPos pos, boolean movedByPiston) {
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
        if (!movedByPiston) {
            revertSibling(level, pos);
        }
    }

    private void revertSibling(ServerLevel level, BlockPos pos) {
        BlockPos below = pos.below();
        if (level.getBlockState(below).is(IRBlocks.IMPETUS_MATRIX.get())) {
            BlockPos siblingPos = below.below();
            BlockState sibling = level.getBlockState(siblingPos);
            if (sibling.getBlock() instanceof ImpetusAltarBaseBlock) {
                revertBase(level, siblingPos, sibling);
            }
            return;
        }

        BlockPos above = pos.above();
        if (level.getBlockState(above).is(IRBlocks.IMPETUS_MATRIX.get())) {
            BlockPos siblingPos = above.above();
            BlockState sibling = level.getBlockState(siblingPos);
            if (sibling.getBlock() instanceof ImpetusAltarBaseBlock) {
                revertBase(level, siblingPos, sibling);
            }
        }
    }

    private void revertBase(ServerLevel level, BlockPos pos, BlockState state) {
        int cells = countCells(state);
        if (cells > 0) {
            popResource(level, pos, new ItemStack(IRItems.IMPETUS_CELL.get(), cells));
        }
        level.setBlockAndUpdate(pos, TCBlocks.PEDESTAL_ELDRITCH.get().defaultBlockState());
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