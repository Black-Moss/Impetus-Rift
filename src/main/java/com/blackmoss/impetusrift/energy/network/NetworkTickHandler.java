package com.blackmoss.impetusrift.energy.network;

import com.blackmoss.impetusrift.ImpetusRift;
import com.blackmoss.impetusrift.network.ClientboundConnectionsSyncPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

@EventBusSubscriber(modid = ImpetusRift.MODID)
public class NetworkTickHandler {

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        for (ServerLevel level : event.getServer().getAllLevels()) {
            ImpetusNetworkManager manager = ImpetusNetworkManager.get(level);
            if (manager != null) {
                manager.tick(level);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ImpetusNetworkManager manager = ImpetusNetworkManager.get(player.level());
            if (manager != null) {
                var packet = new ClientboundConnectionsSyncPayload(manager.getAllConnections());
                PacketDistributor.sendToPlayer(player, packet);
            }
        }
    }

    public static void damageEntitiesInBeam(ServerLevel level, BlockPos from, BlockPos to, long transferAmount) {
        if (transferAmount <= 0) return;

        float damage;
        if (transferAmount <= 10) {
            damage = 1.0F;
        } else {
            damage = transferAmount / 10.0F;
        }

        Vec3 start = Vec3.atCenterOf(from);
        Vec3 end = Vec3.atCenterOf(to);
        AABB beamAABB = new AABB(start, end).inflate(0.5);

        List<Entity> entities = level.getEntitiesOfClass(Entity.class, beamAABB,
                entity -> entity instanceof LivingEntity && entity.isAlive());

        for (Entity entity : entities) {
            float halfDamage = damage / 2.0F;
            entity.hurt(level.damageSources().generic(), halfDamage);
            entity.hurt(level.damageSources().magic(), halfDamage);
        }
    }
}