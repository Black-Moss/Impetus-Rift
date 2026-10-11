package com.blackmoss.impetusrift.event;

import com.blackmoss.impetusrift.ImpetusRift;
import com.blackmoss.impetusrift.config.IRClientConfig;
import com.blackmoss.impetusrift.config.IRCommonConfig;
import com.blackmoss.impetusrift.energy.ElytraThrusterAccess;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = ImpetusRift.MODID)
public final class ElytraThrusterEvents {
    private static final int FUEL_PER_BOOST = 1;
    /** 超级模式下两次自动加速之间的最小间隔（tick） */
    private static final int SUPER_RECHARGE_COOLDOWN = 10;
    private static final double BOOST_DIRECT = 0.1;
    private static final double BOOST_TARGET = 1.5;
    private static final double BOOST_LERP = 0.5;
    private static final Map<UUID, Boolean> LAST_JUMP = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> HOLD_TICKS = new ConcurrentHashMap<>();
    /** 本次按住是否已进入超级加速模式 */
    private static final Map<UUID, Boolean> SUPER_FIRED = new ConcurrentHashMap<>();
    /** 下次允许自动再次超级加速的 tickCount */
    private static final Map<UUID, Integer> SUPER_RECHARGE_AT = new ConcurrentHashMap<>();

    private ElytraThrusterEvents() {
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        UUID id = player.getUUID();

        boolean jump = player.getLastClientInput().jump();
        boolean lastJump = LAST_JUMP.getOrDefault(id, false);
        LAST_JUMP.put(id, jump);

        if (!jump) {
            HOLD_TICKS.remove(id);
            SUPER_FIRED.remove(id);
            SUPER_RECHARGE_AT.remove(id);
        } else {
            HOLD_TICKS.merge(id, 1, Integer::sum);
        }

        if (!player.isFallFlying()) {
            return;
        }

        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!ElytraThrusterAccess.hasThruster(chest)) {
            return;
        }

        int superMultiplier = IRCommonConfig.THRUSTER_SUPER_BOOST_MULTIPLIER.getAsInt();

        if (jump && !lastJump && ElytraThrusterAccess.consumeFuel(chest, FUEL_PER_BOOST)) {
            fireworkBoost(player, 1.0D, false);
            return;
        }

        if (jump
                && HOLD_TICKS.getOrDefault(id, 0) >= superHoldTicks()
                && !SUPER_FIRED.getOrDefault(id, false)
                && ElytraThrusterAccess.consumeFuel(chest, FUEL_PER_BOOST * superMultiplier)) {
            SUPER_FIRED.put(id, true);
            SUPER_RECHARGE_AT.put(id, player.tickCount + SUPER_RECHARGE_COOLDOWN);
            fireworkBoost(player, superMultiplier, true);
            return;
        }

        if (jump
                && SUPER_FIRED.getOrDefault(id, false)
                && player.tickCount >= SUPER_RECHARGE_AT.getOrDefault(id, 0)
                && player.getDeltaMovement().length() < IRCommonConfig.THRUSTER_SUPER_BOOST_MIN_SPEED.getAsDouble()
                && ElytraThrusterAccess.consumeFuel(chest, FUEL_PER_BOOST * superMultiplier)) {
            SUPER_RECHARGE_AT.put(id, player.tickCount + SUPER_RECHARGE_COOLDOWN);
            fireworkBoost(player, superMultiplier, true);
        }
    }

    private static int superHoldTicks() {
        return IRClientConfig.SPEC.isLoaded()
                ? IRClientConfig.THRUSTER_SUPER_BOOST_HOLD_TIME.getAsInt()
                : 20;
    }

    private static void fireworkBoost(ServerPlayer player, double multiplier, boolean superBoost) {
        Vec3 look = player.getLookAngle();
        Vec3 velocity = player.getDeltaMovement();
        player.push(
                multiplier * (look.x * BOOST_DIRECT + (look.x * BOOST_TARGET - velocity.x) * BOOST_LERP),
                multiplier * (look.y * BOOST_DIRECT + (look.y * BOOST_TARGET - velocity.y) * BOOST_LERP),
                multiplier * (look.z * BOOST_DIRECT + (look.z * BOOST_TARGET - velocity.z) * BOOST_LERP));
        player.hurtMarked = true;

        if (player.level() instanceof ServerLevel level) {
            if (superBoost) {
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.PLAYERS, 2.0F, 2.0F);
            } else {
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.PLAYERS, 1.0F, 1.0F);
            }
        }
    }
}
