package com.blackmoss.impetusrift.item;

import com.blackmoss.impetusrift.ImpetusRift;
import com.blackmoss.impetusrift.energy.IImpetusRechargeable;
import com.blackmoss.impetusrift.energy.ImpetusChargeHelper;
import com.leclowndu93150.thaumaturge.api.items.RechargeAccess;
import com.leclowndu93150.thaumaturge.content.equipment.VoidGearItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = ImpetusRift.MODID)
public class ImpetusTravellerBootsItem extends VoidGearItem implements IImpetusRechargeable {
    public static final float BLOCKS_PER_ENERGY = 5.0F;

    private static final AttributeModifier STEP_MODIFIER = new AttributeModifier(
            ImpetusRift.identifier("impetus_traveller_step"), 1.0F, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    private static final AttributeModifier JUMP_MODIFIER = new AttributeModifier(
            ImpetusRift.identifier("impetus_traveller_jump"), 1.0F, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

    private static final Vec3 FORWARD = new Vec3(0.0, 0.0, 1.0);

    public ImpetusTravellerBootsItem(Properties properties) {
        super(properties);
    }

    public static void clientMovementTick(Player player, ItemStack stack) {
        if (RechargeAccess.getCharge(stack) > 0 && !player.getAbilities().flying && !(player.zza <= 0.0F)) {
            if (player.onGround()) {
                float bonus = 0.05F;
                if (player.isInWater()) {
                    bonus /= 4.0F;
                }

                player.moveRelative(bonus, FORWARD);
            } else if (player.isInWater()) {
                player.moveRelative(0.025F, FORWARD);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        LivingEntity entity = event.getEntity();
        ItemStack feet = entity.getItemBySlot(EquipmentSlot.FEET);
        feet.getItem();
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);
        if (slot != EquipmentSlot.FEET || !(entity instanceof ServerPlayer player)) {
            return;
        }

        if (player.tickCount % 20 == 0 && player.isSprinting()) {
            int charge = ImpetusChargeHelper.getCharge(stack);
            if (charge > 0) {
                ImpetusChargeHelper.setCharge(stack, charge - 1);
            }
        }

        boolean charged = ImpetusChargeHelper.getCharge(stack) > 0;
        boolean active = charged && player.isSprinting() && !player.getAbilities().flying;
        AttributeInstance stepHeight = player.getAttribute(Attributes.STEP_HEIGHT);
        AttributeInstance jumpHeight = player.getAttribute(Attributes.JUMP_STRENGTH);
        if (stepHeight != null) {
            if (active && !stepHeight.hasModifier(STEP_MODIFIER.id())) {
                stepHeight.addTransientModifier(STEP_MODIFIER);
            } else if (!active && stepHeight.hasModifier(STEP_MODIFIER.id())) {
                stepHeight.removeModifier(STEP_MODIFIER.id());
            }
        }
        if (jumpHeight != null) {
            if (active && !jumpHeight.hasModifier(JUMP_MODIFIER.id())) {
                jumpHeight.addTransientModifier(JUMP_MODIFIER);
            } else if (!active && jumpHeight.hasModifier(JUMP_MODIFIER.id())) {
                jumpHeight.removeModifier(JUMP_MODIFIER.id());
            }
        }
    }

    @Override
    public long getMaxCharge(@NonNull ItemStack stack, @Nullable LivingEntity entity) {
        return ImpetusChargeHelper.CAPACITY;
    }

    @Override
    public IImpetusRechargeable.ChargeDisplay showInHud(ItemStack stack, LivingEntity holder) {
        return IImpetusRechargeable.ChargeDisplay.PERIODIC;
    }
}
