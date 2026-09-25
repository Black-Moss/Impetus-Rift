package com.blackmoss.impetusrift.event;

import com.blackmoss.impetusrift.ImpetusRift;
import com.blackmoss.impetusrift.energy.network.IImpetusNode;
import com.blackmoss.impetusrift.item.ImpetusLinkerItem;
import net.minecraft.core.BlockPos;
import net.minecraft.util.TriState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = ImpetusRift.MODID)
public final class IRLinkEvents {
    private IRLinkEvents() {
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();

        ItemStack main = player.getItemInHand(InteractionHand.MAIN_HAND);
        ItemStack off = player.getItemInHand(InteractionHand.OFF_HAND);
        ItemStack linker = main.getItem() instanceof ImpetusLinkerItem
                ? main
                : (off.getItem() instanceof ImpetusLinkerItem ? off : null);
        if (linker == null) {
            return;
        }

        BlockPos pos = event.getPos();
        BlockEntity be = event.getLevel().getBlockEntity(pos);
        if (!(be instanceof IImpetusNode)) {
            return;
        }

        if (event.getLevel().isClientSide()) {
            return;
        }

        event.setUseBlock(TriState.FALSE);
    }
}
