package com.blackmoss.impetusrift.client;

import com.blackmoss.impetusrift.ImpetusRift;
import com.blackmoss.impetusrift.client.hud.ImpetusHudOverlay;
import com.blackmoss.impetusrift.energy.ImpetusChargeProfile;
import com.blackmoss.impetusrift.energy.ImpetusRechargeAccess;
import com.blackmoss.impetusrift.item.ImpetusTravellerBootsItem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@EventBusSubscriber(modid = ImpetusRift.MODID, value = Dist.CLIENT)
public final class IREvents {
    private IREvents() {
    }

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.EXPERIENCE_LEVEL,
                ImpetusRift.identifier("impetus_hud"), new ImpetusHudOverlay());
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        if (event.getEntity() == null) {
            return;
        }
        ItemStack stack = event.getItemStack();
        ImpetusChargeProfile profile = ImpetusRechargeAccess.profile(stack);
        if (profile == null) {
            return;
        }
        event.getToolTip().add(1, Component.translatable("tooltip.impetusrift.impetus_energy",
                        ImpetusRechargeAccess.getCharge(stack),
                        profile.capacity())
                .withStyle(ChatFormatting.DARK_PURPLE));
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.isPaused()) {
            return;
        }
        LocalPlayer player = minecraft.player;
        if (player == null || !player.isAlive()) {
            return;
        }
        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        if (boots.getItem() instanceof ImpetusTravellerBootsItem) {
            ImpetusTravellerBootsItem.clientMovementTick(player, boots);
        }
    }
}
