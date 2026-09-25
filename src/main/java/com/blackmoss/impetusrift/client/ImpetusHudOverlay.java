package com.blackmoss.impetusrift.client;

import com.blackmoss.impetusrift.ImpetusRift;
import com.blackmoss.impetusrift.energy.IImpetusRechargeable;
import com.blackmoss.impetusrift.energy.ImpetusChargeHelper;
import com.leclowndu93150.thaumaturge.TCIds;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.gui.GuiLayer;
import org.jspecify.annotations.NonNull;

import java.util.EnumMap;
import java.util.Map;

public final class ImpetusHudOverlay implements GuiLayer {
    private static final Identifier HUD_FRAME = TCIds.rl("textures/gui/hud.png");
    private static final int HUD_TEX_SIZE = 256;
    private static final Identifier BEAM = ImpetusRift.identifier("textures/misc/impetus_energy_beam.png");
    private static final int BEAM_TEX_SIZE = 64;
    private static final float BEAM_BAR_V = 30.0F;
    private static final int BEAM_BAR_H = 8;
    private static final int BEAM_BAR_LENGTH = 64;
    private static final float BEAM_SCROLL_SPEED = 1.0F;

    private static final int PERIODIC_SHOW_TICKS = 60;
    private static final int ANCHOR_X = 5;
    private static final int ANCHOR_BOTTOM = 48;
    private static final int ENTRY_SPACING = 26;
    private static final int ITEM_SIZE = 16;
    private static final int BAR_GAP_X = 3;
    private static final float BAR_SPACE_SCALE = 0.5F;
    private static final int BAR_MAX_HEIGHT = 30;
    private static final int BAR_BOTTOM = 35;
    private static final int BAR_FILL_W = 8;
    private static final int BAR_FRAME_X = -8;
    private static final int BAR_FRAME_Y = -3;
    private static final float BAR_FRAME_U = 72.0F;
    private static final int BAR_FRAME_W = 16;
    private static final int BAR_FRAME_H = 42;
    private static final float BAR_FILL_ALPHA = 0.8F;
    private static final int AMOUNT_TEXT_X = -32;
    private static final int AMOUNT_TEXT_Y = -14;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int ENERGY_COLOR = 0xAA55FF;

    private final Map<EquipmentSlot, Integer> lastCharge = new EnumMap<>(EquipmentSlot.class);
    private final Map<EquipmentSlot, Integer> changeTick = new EnumMap<>(EquipmentSlot.class);

    @Override
    public void render(@NonNull GuiGraphicsExtractor graphics, @NonNull DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.options.hideGui) {
            return;
        }

        int shown = 0;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = player.getItemBySlot(slot);
            if (!(stack.getItem() instanceof IImpetusRechargeable rechargeable)) {
                lastCharge.remove(slot);
                changeTick.remove(slot);
                continue;
            }

            IImpetusRechargeable.ChargeDisplay display = rechargeable.showInHud(stack, player);
            if (display == IImpetusRechargeable.ChargeDisplay.NEVER) {
                continue;
            }

            int charge = ImpetusChargeHelper.getCharge(stack);
            Integer previous = lastCharge.put(slot, charge);
            if (previous == null || previous != charge) {
                changeTick.put(slot, player.tickCount);
            }

            boolean held = slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND;
            if (!held
                    && display == IImpetusRechargeable.ChargeDisplay.PERIODIC
                    && player.tickCount - changeTick.getOrDefault(slot, Integer.MIN_VALUE) > PERIODIC_SHOW_TICKS) {
                continue;
            }

            drawMeter(graphics, mc, stack, (int) rechargeable.getMaxCharge(stack, player), charge,
                    shown++, player.isShiftKeyDown());
        }
    }

    private static void drawMeter
            (GuiGraphicsExtractor graphics,
             Minecraft mc,
             ItemStack stack,
             int max,
             int charge,
             int index,
             boolean showAmount) {
        int y = graphics.guiHeight() - ANCHOR_BOTTOM - index * ENTRY_SPACING;
        graphics.item(stack, ANCHOR_X, y - ITEM_SIZE);
        graphics.pose().pushMatrix();
        graphics.pose().translate(
                ANCHOR_X + ITEM_SIZE + BAR_GAP_X + BAR_FRAME_W * BAR_SPACE_SCALE / 2.0F,
                y - ITEM_SIZE + BAR_FRAME_Y * BAR_SPACE_SCALE);
        graphics.pose().scale(BAR_SPACE_SCALE, BAR_SPACE_SCALE);

        int loc = max > 0 ? (int) (BAR_MAX_HEIGHT * (float) charge / max) : 0;
        if (loc > 0) {
            int tick = mc.player != null ? mc.player.tickCount : 0;
            float scroll = (tick * BEAM_SCROLL_SPEED) % BEAM_BAR_LENGTH;
            if (scroll < 0) scroll += BEAM_BAR_LENGTH;
            int color = ARGB.color(Math.round(BAR_FILL_ALPHA * 255.0F), ENERGY_COLOR);

            graphics.pose().pushMatrix();
            graphics.pose().translate(0.0F, BAR_BOTTOM - loc / 2.0F);
            graphics.pose().rotate((float) Math.toRadians(-90.0));
            int x = -loc / 2;
            int remaining = loc;
            float u = scroll;
            while (remaining > 0) {
                int seg = (int) Math.min(remaining, BEAM_BAR_LENGTH - u);
                graphics.blit(RenderPipelines.GUI_TEXTURED, BEAM,
                        x, -BAR_FILL_W / 2,
                        u, BEAM_BAR_V,
                        seg, BAR_FILL_W,
                        seg, BEAM_BAR_H,
                        BEAM_TEX_SIZE, BEAM_TEX_SIZE,
                        color);
                x += seg;
                remaining -= seg;
                u = 0.0F;
            }
            graphics.pose().popMatrix();
        }

        graphics.blit(RenderPipelines.GUI_TEXTURED, HUD_FRAME,
                BAR_FRAME_X, BAR_FRAME_Y, BAR_FRAME_U, 0.0F,
                BAR_FRAME_W, BAR_FRAME_H, BAR_FRAME_W, BAR_FRAME_H,
                HUD_TEX_SIZE, HUD_TEX_SIZE);

        if (showAmount) {
            graphics.pose().pushMatrix();
            graphics.pose().rotate((float) Math.toRadians(-90.0));
            graphics.text(mc.font, charge + " / " + max, AMOUNT_TEXT_X, AMOUNT_TEXT_Y, WHITE, false);
            graphics.pose().popMatrix();
        }
        graphics.pose().popMatrix();
    }
}
