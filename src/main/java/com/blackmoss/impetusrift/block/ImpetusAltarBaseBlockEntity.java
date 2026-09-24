package com.blackmoss.impetusrift.block;

import com.blackmoss.impetusrift.ImpetusRift;
import com.blackmoss.impetusrift.item.ImpetusCellItem;
import com.blackmoss.impetusrift.registry.IRBlockEntities;
import com.blackmoss.impetusrift.registry.IRItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.NonNull;

public class ImpetusAltarBaseBlockEntity extends BlockEntity {

    public static final int MAX_UNITS_PER_FACE = 1;
    public static final int MAX_TOTAL_UNITS = 8;
    public static final long ENERGY_PER_UNIT = 500L;

    private final boolean[] installedUnits = new boolean[4];

    public ImpetusAltarBaseBlockEntity(BlockPos pos, BlockState state) {
        super(IRBlockEntities.IMPETUS_ALTAR_BASE.get(), pos, state);
    }

    public InteractionResult handleInteraction(Player player, InteractionHand hand, Direction clickedFace) {
        if (level == null) return InteractionResult.PASS;

        if (clickedFace == Direction.UP || clickedFace == Direction.DOWN) {
            return InteractionResult.PASS;
        }

        int faceIndex = directionToIndex(clickedFace);
        if (faceIndex < 0) return InteractionResult.PASS;

        ItemStack heldItem = player.getItemInHand(hand);
        boolean isCell = heldItem.getItem() instanceof ImpetusCellItem;
        boolean isEmptyAndHasUnit = heldItem.isEmpty() && installedUnits[faceIndex];

        if (!isCell && !isEmptyAndHasUnit) {
            return InteractionResult.PASS;
        }

        // 客户端：返回 SUCCESS（表示交互成功，触发手臂动画和网络同步）
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (isCell) {
            if (installedUnits[faceIndex]) {
                return InteractionResult.PASS;
            }

            installedUnits[faceIndex] = true;
            if (!player.getAbilities().instabuild) {
                heldItem.shrink(1);
            }
            level.playSound(null, worldPosition, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.5F, 1.0F);
            setChanged();
            syncToClient();
            notifyMatrix();
            return InteractionResult.SUCCESS;
        }

        installedUnits[faceIndex] = false;
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
        setChanged();
        syncToClient();
        notifyMatrix();
        return InteractionResult.SUCCESS;
    }

    public int getInstalledUnitCount() {
        int count = 0;
        for (boolean unit : installedUnits) {
            if (unit) count++;
        }
        return count;
    }

    public boolean hasUnitOnFace(Direction face) {
        int index = directionToIndex(face);
        return index >= 0 && installedUnits[index];
    }

    public long getAdditionalCapacity() {
        return getInstalledUnitCount() * ENERGY_PER_UNIT;
    }

    private int directionToIndex(Direction face) {
        return switch (face) {
            case NORTH -> 0;
            case SOUTH -> 1;
            case WEST -> 2;
            case EAST -> 3;
            default -> -1;
        };
    }

    private Direction indexToDirection(int index) {
        return switch (index) {
            case 1 -> Direction.SOUTH;
            case 2 -> Direction.WEST;
            case 3 -> Direction.EAST;
            default -> Direction.NORTH;
        };
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
        for (int i = 0; i < 4; i++) {
            if (installedUnits[i]) {
                installedUnits[i] = false;
                Direction face = indexToDirection(i);
                setChanged();
                syncToClient();
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

    protected final void syncToClient() {
        if (level == null || level.isClientSide()) return;
        BlockState current = getBlockState();
        level.sendBlockUpdated(getBlockPos(), current, current, 3);
    }

    @Override
    protected void saveAdditional(@NonNull ValueOutput output) {
        super.saveAdditional(output);
        for (int i = 0; i < 4; i++) {
            output.putBoolean("Unit" + i, installedUnits[i]);
        }
    }

    @Override
    protected void loadAdditional(@NonNull ValueInput input) {
        super.loadAdditional(input);
        for (int i = 0; i < 4; i++) {
            installedUnits[i] = input.getBooleanOr("Unit" + i, false);
        }
    }

    @Override
    public @NonNull CompoundTag getUpdateTag(HolderLookup.@NonNull Provider registries) {
        CompoundTag nbt = super.getUpdateTag(registries);
        try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(this.problemPath(), ImpetusRift.LOGGER)) {
            TagValueOutput output = TagValueOutput.createWithContext(reporter, registries);
            saveAdditional(output);
            nbt.merge(output.buildResult());
        }
        return nbt;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}