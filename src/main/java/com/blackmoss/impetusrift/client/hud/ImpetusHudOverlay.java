package com.blackmoss.impetusrift.client.hud;

import com.blackmoss.impetusrift.ImpetusRift;
import com.blackmoss.impetusrift.energy.ElytraThrusterAccess;
import com.blackmoss.impetusrift.energy.ImpetusChargeDisplay;
import com.blackmoss.impetusrift.energy.ImpetusChargeProfile;
import com.blackmoss.impetusrift.energy.ImpetusRechargeAccess;
import com.blackmoss.impetusrift.registry.IRItems;
import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.items.ChargeDisplay;
import com.leclowndu93150.thaumaturge.api.items.ChargeProfile;
import com.leclowndu93150.thaumaturge.api.items.RechargeAccess;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.gui.GuiLayer;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

/**
 * 元动能源 HUD：复刻 Thaumaturge {@code RechargeHudOverlay} 的布局与逻辑，另加两点定制：
 * <ul>
 *   <li>元动条接在 Thaumaturge 灵气条<b>上方</b>继续堆叠（{@link #countThaumaturgeEntries}），避免相互覆盖；</li>
 *   <li>装有助推器的物品额外显示一条「助推器燃料」，与本体能量同时存在时用「物品 + 助推器」双图标。</li>
 * </ul>
 */
public final class ImpetusHudOverlay implements GuiLayer {
    private static final Identifier HUD = TTIds.rl("textures/gui/hud.png");
    private static final int TEX_SIZE = 256;
    private static final int PERIODIC_SHOW_TICKS = 60;
    private static final int ANCHOR_X = 4;
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
    private static final Identifier BEAM = ImpetusRift.identifier("textures/misc/impetus_energy_beam.png");
    private static final int BEAM_TEX_SIZE = 64;
    private static final float BEAM_BAR_V = 30.0F;
    private static final int BEAM_BAR_H = 8;
    private static final int BEAM_BAR_LENGTH = 64;
    private static final float BEAM_SCROLL_SPEED = 1.0F;
    private static final float SUB_ICON_SCALE = 0.75F;
    private static final int ENERGY_COLOR = 0xAA55FF;
    private static @Nullable ItemStack thrusterIcon;

    private final Map<EquipmentSlot, Integer> lastCharge = new EnumMap<>(EquipmentSlot.class);
    private final Map<EquipmentSlot, Integer> changeTick = new EnumMap<>(EquipmentSlot.class);
    private final Map<EquipmentSlot, Integer> thrusterLastCharge = new EnumMap<>(EquipmentSlot.class);
    private final Map<EquipmentSlot, Integer> thrusterChangeTick = new EnumMap<>(EquipmentSlot.class);
    private final Map<EquipmentSlot, Integer> tcLastCharge = new EnumMap<>(EquipmentSlot.class);
    private final Map<EquipmentSlot, Integer> tcChangeTick = new EnumMap<>(EquipmentSlot.class);

    @Override
    public void render(@NonNull GuiGraphicsExtractor graphics, @NonNull DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.options.hideGui) {
            return;
        }

        int offset = countThaumaturgeEntries(player);
        int shown = 0;

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = player.getItemBySlot(slot);
            ImpetusChargeProfile profile = ImpetusRechargeAccess.profile(stack);
            boolean hasThruster = ElytraThrusterAccess.hasThruster(stack);
            if (profile == null && !hasThruster) {
                lastCharge.remove(slot);
                changeTick.remove(slot);
                thrusterLastCharge.remove(slot);
                thrusterChangeTick.remove(slot);
                continue;
            }

            boolean held = slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND;

            if (profile != null) {
                ImpetusChargeDisplay display = profile.display();
                if (display != ImpetusChargeDisplay.NEVER) {
                    int charge = ImpetusRechargeAccess.getCharge(stack);
                    markChanged(lastCharge, changeTick, slot, charge, player);
                    if (held || display != ImpetusChargeDisplay.ON_CHANGE
                            || player.tickCount - changeTick.getOrDefault(slot, Integer.MIN_VALUE) <= PERIODIC_SHOW_TICKS) {
                        drawMeter(graphics, mc, stack, profile.capacity(), charge,
                                offset + shown++, player.isShiftKeyDown(), null);
                    }
                }
            }

            if (hasThruster) {
                int fuel = ElytraThrusterAccess.getFuel(stack);
                markChanged(thrusterLastCharge, thrusterChangeTick, slot, fuel, player);
                if (held
                        || player.tickCount - thrusterChangeTick.getOrDefault(slot, Integer.MIN_VALUE) <= PERIODIC_SHOW_TICKS) {
                    drawMeter(graphics, mc, stack, ElytraThrusterAccess.getCapacity(stack), fuel,
                            offset + shown++, player.isShiftKeyDown(),
                            profile != null ? thrusterIcon() : null);
                }
            }
        }
    }

    private static ItemStack thrusterIcon() {
        if (thrusterIcon == null) {
            thrusterIcon = new ItemStack(IRItems.ELYTRA_THRUSTER.get());
        }
        return thrusterIcon;
    }

    private static void markChanged(Map<EquipmentSlot, Integer> last, Map<EquipmentSlot, Integer> ticks,
                                    EquipmentSlot slot, int value, LocalPlayer player) {
        Integer previous = last.put(slot, value);
        if (previous == null || previous != value) {
            ticks.put(slot, player.tickCount);
        }
    }

    private int countThaumaturgeEntries(LocalPlayer player) {
        int count = 0;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = player.getItemBySlot(slot);
            ChargeProfile profile = RechargeAccess.profile(stack);
            if (profile == null) {
                tcLastCharge.remove(slot);
                tcChangeTick.remove(slot);
                continue;
            }

            ChargeDisplay display = profile.display();
            if (display == ChargeDisplay.NEVER) {
                continue;
            }

            int charge = RechargeAccess.getCharge(stack);
            markChanged(tcLastCharge, tcChangeTick, slot, charge, player);

            boolean held = slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND;
            if (held || display != ChargeDisplay.ON_CHANGE
                    || player.tickCount - tcChangeTick.getOrDefault(slot, Integer.MIN_VALUE) <= PERIODIC_SHOW_TICKS) {
                count++;
            }
        }
        return count;
    }

    private static void drawMeter(
            GuiGraphicsExtractor graphics,
            Minecraft mc,
            ItemStack stack,
            int max,
            int charge,
            int index,
            boolean showAmount,
            @Nullable ItemStack subIcon) {
        int y = graphics.guiHeight() - ANCHOR_BOTTOM - index * ENTRY_SPACING;
        drawIcon(graphics, stack, y, subIcon);

        graphics.pose().pushMatrix();
        graphics.pose().translate(
                ANCHOR_X + ITEM_SIZE + BAR_GAP_X + BAR_FRAME_W * BAR_SPACE_SCALE / 2.0F,
                y - ITEM_SIZE + BAR_FRAME_Y * BAR_SPACE_SCALE);
        graphics.pose().scale(BAR_SPACE_SCALE, BAR_SPACE_SCALE);

        drawFill(graphics, mc, max, charge);

        graphics.blit(RenderPipelines.GUI_TEXTURED, HUD,
                BAR_FRAME_X, BAR_FRAME_Y, BAR_FRAME_U, 0.0F,
                BAR_FRAME_W, BAR_FRAME_H, BAR_FRAME_W, BAR_FRAME_H,
                TEX_SIZE, TEX_SIZE);

        if (showAmount) {
            graphics.pose().pushMatrix();
            graphics.pose().rotate((float) Math.toRadians(-90.0));
            graphics.text(mc.font, Component.translatable("gui.thaumaturge.fraction", charge, max),
                    AMOUNT_TEXT_X, AMOUNT_TEXT_Y, WHITE, false);
            graphics.pose().popMatrix();
        }

        graphics.pose().popMatrix();
    }

    private static void drawIcon(GuiGraphicsExtractor graphics, ItemStack stack, int y, @Nullable ItemStack subIcon) {
        if (subIcon == null) {
            graphics.item(stack, ANCHOR_X, y - ITEM_SIZE);
            return;
        }
        graphics.pose().pushMatrix();
        graphics.pose().translate(ANCHOR_X, y - ITEM_SIZE);
        graphics.pose().scale(SUB_ICON_SCALE, SUB_ICON_SCALE);
        graphics.item(stack, 0, 0);
        graphics.pose().popMatrix();

        graphics.pose().pushMatrix();
        graphics.pose().translate(ANCHOR_X + ITEM_SIZE * (1.0F - SUB_ICON_SCALE),
                y - ITEM_SIZE * SUB_ICON_SCALE);
        graphics.pose().scale(SUB_ICON_SCALE, SUB_ICON_SCALE);
        graphics.item(subIcon, 0, 0);
        graphics.pose().popMatrix();
    }

    private static void drawFill(GuiGraphicsExtractor graphics, Minecraft mc, int max, int charge) {
        int loc = max > 0 ? (int) (BAR_MAX_HEIGHT * (float) charge / max) : 0;
        if (loc <= 0) {
            return;
        }

        int tick = mc.player != null ? mc.player.tickCount : 0;
        float scroll = (tick * BEAM_SCROLL_SPEED) % BEAM_BAR_LENGTH;
        if (scroll < 0) {
            scroll += BEAM_BAR_LENGTH;
        }
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
                    x, -BAR_FILL_W / 2, u, BEAM_BAR_V,
                    seg, BAR_FILL_W, seg, BEAM_BAR_H,
                    BEAM_TEX_SIZE, BEAM_TEX_SIZE, color);
            x += seg;
            remaining -= seg;
            u = 0.0F;
        }
        graphics.pose().popMatrix();
    }
}
