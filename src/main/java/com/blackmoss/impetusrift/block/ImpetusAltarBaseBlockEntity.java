package com.blackmoss.impetusrift.block;

import com.blackmoss.impetusrift.item.ImpetusCellItem;
import com.blackmoss.impetusrift.registry.IRBlockEntities;
import com.blackmoss.impetusrift.registry.IRItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ImpetusAltarBaseBlockEntity extends BlockEntity {

    public static final int MAX_UNITS_PER_FACE = 1;
    public static final int MAX_TOTAL_UNITS = 8;
    public static final long ENERGY_PER_UNIT = 500L;

    public ImpetusAltarBaseBlockEntity(BlockPos pos, BlockState state) {
        super(IRBlockEntities.IMPETUS_ALTAR_BASE.get(), pos, state);
    }

    public InteractionResult handleInteraction(Player player, InteractionHand hand, Direction clickedFace) {
        if (level == null) return InteractionResult.PASS;

        if (clickedFace == Direction.UP || clickedFace == Direction.DOWN) {
            return InteractionResult.PASS;
        }

        BlockState state = getBlockState();
        boolean present = ImpetusAltarBaseBlock.hasCell(state, clickedFace);

        ItemStack heldItem = player.getItemInHand(hand);
        boolean isCell = heldItem.getItem() instanceof ImpetusCellItem;
        boolean isEmptyAndHasUnit = heldItem.isEmpty() && present;

        if (!isCell && !isEmptyAndHasUnit) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (isCell) {
            if (present) {
                return InteractionResult.PASS;
            }

            if (!player.getAbilities().instabuild) {
                heldItem.shrink(1);
            }
            level.setBlockAndUpdate(worldPosition, ImpetusAltarBaseBlock.setCell(state, clickedFace, true));
            level.playSound(null, worldPosition, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.5F, 1.0F);
            notifyMatrix();
            return InteractionResult.SUCCESS;
        }

        level.setBlockAndUpdate(worldPosition, ImpetusAltarBaseBlock.setCell(state, clickedFace, false));
        if (!player.getAbilities().instabuild) {
            player.getInventory().add(new ItemStack(getItemForUnit()));
        }
        level.playSound(
                null,
                worldPosition,
                SoundEvents.ITEM_PICKUP,
                SoundSource.BLOCKS,
                0.5F,
                0.8F);
        notifyMatrix();
        return InteractionResult.SUCCESS;
    }

    public int getInstalledUnitCount() {
        return ImpetusAltarBaseBlock.countCells(getBlockState());
    }

    public boolean hasUnitOnFace(Direction face) {
        return ImpetusAltarBaseBlock.hasCell(getBlockState(), face);
    }

    public long getAdditionalCapacity() {
        return getInstalledUnitCount() * ENERGY_PER_UNIT;
    }

    private void notifyMatrix() {
        if (level == null) return;
        BlockPos above = worldPosition.above();
        BlockPos below = worldPosition.below();
        if (level.getBlockEntity(above) instanceof ImpetusMatrixBlockEntity matrix) {
            matrix.onCapacityChanged();
        }
        if (level.getBlockEntity(below) instanceof ImpetusMatrixBlockEntity matrix) {
            matrix.onCapacityChanged();
        }
    }

    public boolean ejectRandomUnit(Level level, BlockPos pos) {
        for (Direction face : new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST}) {
            BlockState state = getBlockState();
            if (ImpetusAltarBaseBlock.hasCell(state, face)) {
                level.setBlockAndUpdate(worldPosition, ImpetusAltarBaseBlock.setCell(state, face, false));
                notifyMatrix();
                spawnEjectedUnit(face, level, pos);
                return true;
            }
        }
        return false;
    }

    private void spawnEjectedUnit(Direction face, Level level, BlockPos pos) {
        ItemStack stack = new ItemStack(getItemForUnit());
        double x = pos.getX() + 0.5 + face.getStepX() * 0.6;
        double y = pos.getY() + 0.5 + face.getStepY() * 0.6;
        double z = pos.getZ() + 0.5 + face.getStepZ() * 0.6;
        ItemEntity entity = new ItemEntity(level, x, y, z, stack);
        entity.setDeltaMovement(face.getStepX() * 0.12, 0.15, face.getStepZ() * 0.12);
        level.addFreshEntity(entity);
    }

    private Item getItemForUnit() {
        return IRItems.IMPETUS_CELL.get();
    }
}
