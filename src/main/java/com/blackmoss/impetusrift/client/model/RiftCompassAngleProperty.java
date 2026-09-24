package com.blackmoss.impetusrift.client.model;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@OnlyIn(Dist.CLIENT)
public class RiftCompassAngleProperty implements RangeSelectItemModelProperty {
    public static final MapCodec<RiftCompassAngleProperty> MAP_CODEC = MapCodec.unit(RiftCompassAngleProperty::new);

    private final RiftCompassAngleState state = new RiftCompassAngleState(true);

    public RiftCompassAngleProperty() {
    }

    @Override
    public float get(
            @NonNull ItemStack itemStack,
            @Nullable ClientLevel level,
            @Nullable ItemOwner owner, int seed) {
        if (level == null || owner == null) {
            return 0.0F;
        }
        return state.get(itemStack, level, owner, seed);
    }

    @Override
    public @NonNull MapCodec<? extends RangeSelectItemModelProperty> type() {
        return MAP_CODEC;
    }
}