package com.blackmoss.impetusrift.client;

import com.blackmoss.impetusrift.ImpetusRift;
import com.blackmoss.impetusrift.client.hud.ImpetusHudOverlay;
import com.blackmoss.impetusrift.client.model.RiftCompassAngleProperty;
import com.blackmoss.impetusrift.client.render.ImpetusDrainerRenderer;
import com.blackmoss.impetusrift.client.render.ImpetusMatrixRenderer;
import com.blackmoss.impetusrift.client.render.ImpetusNodeRenderer;
import com.blackmoss.impetusrift.client.render.RiftJarRenderer;
import com.blackmoss.impetusrift.energy.ElytraThrusterAccess;
import com.blackmoss.impetusrift.energy.ImpetusChargeProfile;
import com.blackmoss.impetusrift.energy.ImpetusRechargeAccess;
import com.blackmoss.impetusrift.item.RiftTravellerBootsItem;
import com.blackmoss.impetusrift.registry.IRBlockEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterRangeSelectItemModelPropertyEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@EventBusSubscriber(modid = ImpetusRift.MODID, value = Dist.CLIENT)
@Mod(value = ImpetusRift.MODID, dist = {Dist.CLIENT})
public final class ImpetusRiftClient {
    public ImpetusRiftClient(ModContainer modContainer) {
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(IRBlockEntities.RIFT_JAR.get(), RiftJarRenderer::new);
        event.registerBlockEntityRenderer(IRBlockEntities.IMPETUS_MATRIX.get(), ImpetusMatrixRenderer::new);
        event.registerBlockEntityRenderer(IRBlockEntities.IMPETUS_DRAINER.get(), ImpetusDrainerRenderer::new);
        event.registerBlockEntityRenderer(IRBlockEntities.IMPETUS_GENERATOR.get(), ImpetusNodeRenderer::new);
        event.registerBlockEntityRenderer(IRBlockEntities.IMPETUS_RELAY.get(), ImpetusNodeRenderer::new);
    }

    @SubscribeEvent
    public static void onRegisterRangeSelectProperties(RegisterRangeSelectItemModelPropertyEvent event) {
        event.register(
                ImpetusRift.identifier("rift_compass_angle"),
                RiftCompassAngleProperty.MAP_CODEC
        );
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
        boolean hasThruster = ElytraThrusterAccess.hasThruster(stack);
        if (profile == null && !hasThruster) {
            return;
        }

        int line = 1;
        if (profile != null) {
            event.getToolTip().add(line++, Component.translatable("tooltip.impetusrift.thaumostatic_elytra.charge",
                            ImpetusRechargeAccess.getCharge(stack),
                            profile.capacity())
                    .withStyle(ChatFormatting.DARK_PURPLE));
        }
        if (hasThruster) {
            event.getToolTip().add(line, Component.translatable("tooltip.impetusrift.thaumostatic_elytra.thruster",
                            ElytraThrusterAccess.getFuel(stack),
                            ElytraThrusterAccess.getCapacity(stack))
                    .withStyle(ChatFormatting.DARK_PURPLE));
        }
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
        if (boots.getItem() instanceof RiftTravellerBootsItem) {
            RiftTravellerBootsItem.clientMovementTick(player, boots);
        }
    }
}
