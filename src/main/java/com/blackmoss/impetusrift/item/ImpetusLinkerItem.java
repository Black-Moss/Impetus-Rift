package com.blackmoss.impetusrift.item;

import com.blackmoss.impetusrift.energy.network.IImpetusNode;
import com.blackmoss.impetusrift.energy.network.ImpetusConnection;
import com.blackmoss.impetusrift.energy.network.ImpetusNetworkManager;
import com.blackmoss.impetusrift.registry.IRDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class ImpetusLinkerItem extends Item {

    public ImpetusLinkerItem(Properties properties) {
        super(properties);
    }

    private static String formatPos(BlockPos pos) {
        return "(" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")";
    }

    private static MutableComponent colored(String key, ChatFormatting color, Object... args) {
        return Component.translatable(key, args).withStyle(color);
    }

    private static @Nullable MutableComponent diagnoseConnectionFailure(Level level, BlockPos sourcePos, BlockPos targetPos) {
        if (sourcePos.distManhattan(targetPos) > ImpetusConnection.MAX_DISTANCE) {
            return Component.translatable("item.impulserift.impetus_linker.too_far",
                    ImpetusConnection.MAX_DISTANCE);
        }

        // 视线检查：不能穿透不透明方块
        if (!hasLineOfSight(level, sourcePos, targetPos)) {
            return Component.translatable("item.impulserift.impetus_linker.blocked");
        }

        IImpetusNode sourceNode = getNodeAt(level, sourcePos);
        IImpetusNode targetNode = getNodeAt(level, targetPos);

        if (sourceNode == null) return Component.translatable("item.impulserift.impetus_linker.no_source");
        if (targetNode == null) return Component.translatable("item.impulserift.impetus_linker.no_target");

        if (!sourceNode.canOutput()) {
            return Component.translatable("item.impulserift.impetus_linker.cannot_output",
                    sourceNode.getNodeLabel());
        }
        if (sourceNode.getConnectedOutputCount() >= sourceNode.getMaxOutputs()) {
            return Component.translatable("item.impulserift.impetus_linker.output_full",
                    sourceNode.getConnectedOutputCount(), sourceNode.getMaxOutputs());
        }

        if (!targetNode.canInput()) {
            return Component.translatable("item.impulserift.impetus_linker.cannot_input",
                    targetNode.getNodeLabel());
        }
        if (targetNode.getConnectedInputCount() >= targetNode.getMaxInputs()) {
            return Component.translatable("item.impulserift.impetus_linker.input_full",
                    targetNode.getConnectedInputCount(), targetNode.getMaxInputs());
        }

        return null;
    }

    /**
     * 检查两个方块中心之间是否有不透明方块阻挡。
     * 使用简单的步进检查，从源到目标逐格检查。
     */
    private static boolean hasLineOfSight(Level level, BlockPos from, BlockPos to) {
        // 使用 Bresenham 风格的步进检查
        int dx = to.getX() - from.getX();
        int dy = to.getY() - from.getY();
        int dz = to.getZ() - from.getZ();

        int steps = Math.max(Math.abs(dx), Math.max(Math.abs(dy), Math.abs(dz)));
        if (steps == 0) return true;

        double stepX = (double) dx / steps;
        double stepY = (double) dy / steps;
        double stepZ = (double) dz / steps;

        for (int i = 1; i < steps; i++) {
            int checkX = from.getX() + (int) Math.round(stepX * i);
            int checkY = from.getY() + (int) Math.round(stepY * i);
            int checkZ = from.getZ() + (int) Math.round(stepZ * i);

            BlockPos checkPos = new BlockPos(checkX, checkY, checkZ);
            if (checkPos.equals(from) || checkPos.equals(to)) continue;

            BlockState state = level.getBlockState(checkPos);
            if (state.isSolidRender()) {
                return false;
            }
        }

        return true;
    }

    private static IImpetusNode getNodeAt(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof IImpetusNode node) return node;
        return null;
    }

    @Override
    public @NonNull InteractionResult useOn(@NonNull UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.FAIL;

        BlockPos clickedPos = context.getClickedPos();
        BlockEntity blockEntity = level.getBlockEntity(clickedPos);

        if (!(blockEntity instanceof IImpetusNode)) {
            return InteractionResult.FAIL;
        }

        ItemStack stack = context.getItemInHand();
        BlockPos boundSource = stack.get(IRDataComponents.LINKER_BOUND_SOURCE.get());

        if (player.isShiftKeyDown()) {
            if (level.isClientSide()) return InteractionResult.SUCCESS;

            if (boundSource != null && boundSource.equals(clickedPos)) {
                stack.remove(IRDataComponents.LINKER_BOUND_SOURCE.get());
            } else {
                stack.set(IRDataComponents.LINKER_BOUND_SOURCE.get(), clickedPos);
            }
            return InteractionResult.SUCCESS;
        }

        if (boundSource == null) {
            if (level.isClientSide()) return InteractionResult.SUCCESS;
            BlockPos highlight = stack.get(IRDataComponents.LINKER_HIGHLIGHT_NODE.get());
            if (highlight != null && highlight.equals(clickedPos)) {
                stack.remove(IRDataComponents.LINKER_HIGHLIGHT_NODE.get());
            } else {
                stack.set(IRDataComponents.LINKER_HIGHLIGHT_NODE.get(), clickedPos);
            }
            return InteractionResult.SUCCESS;
        }

        if (level.isClientSide()) return InteractionResult.SUCCESS;

        if (boundSource.equals(clickedPos)) {
            stack.remove(IRDataComponents.LINKER_BOUND_SOURCE.get());
            return InteractionResult.SUCCESS;
        }

        ImpetusNetworkManager manager = ImpetusNetworkManager.get(level);
        if (manager == null) {
            stack.remove(IRDataComponents.LINKER_BOUND_SOURCE.get());
            return InteractionResult.FAIL;
        }

        List<ImpetusConnection> existing = manager.getConnectionsFrom(boundSource);
        boolean alreadyConnected = false;
        for (ImpetusConnection conn : existing) {
            if (conn.targetPos().equals(clickedPos)) {
                alreadyConnected = true;
                break;
            }
        }

        if (alreadyConnected) {
            manager.removeConnection(boundSource, clickedPos);
            player.sendSystemMessage(colored("item.impulserift.impetus_linker.disconnected",
                    ChatFormatting.YELLOW, formatPos(boundSource), formatPos(clickedPos)));
        } else {
            MutableComponent failReason = diagnoseConnectionFailure(level, boundSource, clickedPos);
            if (failReason != null) {
                player.sendSystemMessage(failReason.copy().withStyle(ChatFormatting.RED));
            } else {
                boolean success = manager.addConnection(boundSource, clickedPos, level);
                if (success) {
                    player.sendSystemMessage(colored("item.impulserift.impetus_linker.connected",
                            ChatFormatting.GREEN, formatPos(boundSource), formatPos(clickedPos)));
                } else {
                    player.sendSystemMessage(colored("item.impulserift.impetus_linker.failed",
                            ChatFormatting.RED));
                }
            }
        }

        stack.remove(IRDataComponents.LINKER_BOUND_SOURCE.get());
        return InteractionResult.SUCCESS;
    }
}