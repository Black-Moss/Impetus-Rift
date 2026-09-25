package com.blackmoss.impetusrift.client;

import com.blackmoss.impetusrift.ImpetusRift;
import com.blackmoss.impetusrift.energy.IImpetusRechargeable;
import com.blackmoss.impetusrift.energy.ImpetusChargeHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@EventBusSubscriber(
        modid = ImpetusRift.MODID,
        value = {Dist.CLIENT}
)
public class IRTooltipEvents {
    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        if (event.getEntity() != null) {
            Item item = event.getItemStack().getItem();
            if (item instanceof IImpetusRechargeable impetusRechargeable) {
                event.getToolTip().add(1, Component.translatable("tooltip.impetusrift.impetus_energy",
                                ImpetusChargeHelper.getCharge(event.getItemStack()),
                                impetusRechargeable.getMaxCharge(event.getItemStack(),
                                        event.getEntity()))
                        .withStyle(ChatFormatting.DARK_PURPLE));
            }
        }
    }
}
