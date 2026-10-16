package com.blackmoss.impetusrift.block;

import com.blackmoss.impetusrift.registry.IRBlockEntities;
import com.blackmoss.impetusrift.registry.IRDataComponents;
import com.blackmoss.impetusrift.registry.IRItems;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class ImpetusMirrorBlock extends BaseEntityBlock {
    public static final MapCodec<ImpetusMirrorBlock> CODEC = simpleCodec(ImpetusMirrorBlock::new);

    private static final String MSG_PREFIX = "item.impetusrift.impetus_linker.";

    public ImpetusMirrorBlock(Properties properties) {
        super(properties);
    }

    private static void message(Player player, String key, ChatFormatting color) {
        player.sendOverlayMessage(Component.translatable(MSG_PREFIX + key).withStyle(color));
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
    protected @NonNull InteractionResult useItemOn(
            @NonNull ItemStack stack,
            @NonNull BlockState state,
            @NonNull Level level,
            @NonNull BlockPos pos,
            @NonNull Player player,
            @NonNull InteractionHand hand,
            @NonNull BlockHitResult hitResult) {
        if (!stack.is(IRItems.IMPETUS_LINKER.get())) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(level.getBlockEntity(pos) instanceof ImpetusMirrorBlockEntity mirror)) {
            return InteractionResult.PASS;
        }

        if (player.isShiftKeyDown()) {
            if (mirror.isBound()) {
                mirror.unbind();
                message(player, "mirror_unbound", ChatFormatting.YELLOW);
            }
            return InteractionResult.SUCCESS;
        }

        BlockPos bound = stack.get(IRDataComponents.LINKER_BOUND_SOURCE.get());
        if (bound == null) {
            message(player, "mirror_select_source", ChatFormatting.YELLOW);
            return InteractionResult.SUCCESS;
        }
        if (bound.equals(pos)) {
            message(player, "mirror_self", ChatFormatting.RED);
            return InteractionResult.SUCCESS;
        }
        if (!(level.getBlockEntity(bound) instanceof ImpetusMirrorBlockEntity source)) {
            message(player, "mirror_need_pair", ChatFormatting.RED);
            return InteractionResult.SUCCESS;
        }

        GlobalPos thisPos = GlobalPos.of(level.dimension(), pos);
        GlobalPos otherPos = GlobalPos.of(level.dimension(), bound);

        if (mirror.isBound() && !thisPos.equals(mirror.getLink())) {
            message(player, "mirror_target_busy", ChatFormatting.RED);
            return InteractionResult.SUCCESS;
        }
        if (source.isBound() && !otherPos.equals(source.getLink())) {
            message(player, "mirror_source_busy", ChatFormatting.RED);
            return InteractionResult.SUCCESS;
        }

        mirror.bindTo(otherPos);
        stack.remove(IRDataComponents.LINKER_BOUND_SOURCE.get());
        message(player, "mirror_bound", ChatFormatting.GREEN);
        return InteractionResult.SUCCESS;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NonNull BlockPos pos, @NonNull BlockState state) {
        return new ImpetusMirrorBlockEntity(pos, state);
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
        return createTickerHelper(blockEntityType, IRBlockEntities.IMPETUS_MIRROR.get(), ImpetusMirrorBlockEntity::staticTick);
    }
}
