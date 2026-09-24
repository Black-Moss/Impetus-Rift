package com.blackmoss.impetusrift.client.model;

import com.leclowndu93150.thaumaturge.content.entity.EntityFluxRift;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.NeedleDirectionHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Random;

@OnlyIn(Dist.CLIENT)
public class RiftCompassAngleState extends NeedleDirectionHelper {
    private static final double SEARCH_RADIUS = 64.0;

    private final NeedleDirectionHelper.Wobbler wobbler;
    private final NeedleDirectionHelper.Wobbler noTargetWobbler;
    private final Random random = new Random();

    public RiftCompassAngleState(boolean wobble) {
        super(wobble);
        this.wobbler = this.newWobbler(0.8F);
        this.noTargetWobbler = this.newWobbler(0.8F);
    }

    private static double getAngleFromEntityToPos(ItemOwner owner, Vec3 target) {
        Vec3 ownerPos = owner.position();
        return Math.atan2(target.z() - ownerPos.z(), target.x() - ownerPos.x()) / (Math.PI * 2);
    }

    private static float getWrappedVisualRotationY(ItemOwner owner) {
        return Mth.positiveModulo(owner.getVisualRotationYInDegrees() / 360.0F, 1.0F);
    }

    private static int hash(int input) {
        return input * 1327217883;
    }

    @Override
    protected float calculate(
            @NonNull ItemStack itemStack,
            @NonNull ClientLevel level,
            int seed,
            @NonNull ItemOwner owner) {
        EntityFluxRift nearestRift = findNearestRift(level, owner);
        long gameTime = level.getGameTime();

        if (nearestRift == null) {
            return getRandomlySpinningRotation(seed, gameTime);
        }

        return getRotationTowardsTarget(owner, gameTime, nearestRift.position());
    }

    @Nullable
    private EntityFluxRift findNearestRift(ClientLevel level, ItemOwner owner) {
        Vec3 ownerPos = owner.position();
        EntityFluxRift nearest = null;
        double nearestDist = Double.MAX_VALUE;

        for (Entity entity : level.entitiesForRendering()) {
            if (entity instanceof EntityFluxRift rift) {
                double dist = rift.position().distanceTo(ownerPos);
                if (dist < SEARCH_RADIUS && dist < nearestDist) {
                    nearestDist = dist;
                    nearest = rift;
                }
            }
        }

        return nearest;
    }

    private float getRandomlySpinningRotation(int seed, long gameTime) {
        if (this.noTargetWobbler.shouldUpdate(gameTime)) {
            this.noTargetWobbler.update(gameTime, this.random.nextFloat());
        }

        float targetRotation = this.noTargetWobbler.rotation() + hash(seed) / 2.1474836E9F;
        return Mth.positiveModulo(targetRotation, 1.0F);
    }

    private float getRotationTowardsTarget(ItemOwner owner, long gameTime, Vec3 targetPos) {
        float angleToTarget = (float) getAngleFromEntityToPos(owner, targetPos);
        float ownerYRotation = getWrappedVisualRotationY(owner);
        float targetRotation;

        if (owner.asLivingEntity() instanceof Player player
                && player.isLocalPlayer()
                && player.level().tickRateManager().runsNormally()) {
            if (this.wobbler.shouldUpdate(gameTime)) {
                this.wobbler.update(gameTime, 0.5F - (ownerYRotation - 0.25F));
            }
            targetRotation = angleToTarget + this.wobbler.rotation();
        } else {
            targetRotation = 0.5F - (ownerYRotation - 0.25F - angleToTarget);
        }

        return Mth.positiveModulo(targetRotation, 1.0F);
    }
}