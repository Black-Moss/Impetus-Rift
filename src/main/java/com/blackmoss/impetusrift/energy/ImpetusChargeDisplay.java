package com.blackmoss.impetusrift.energy;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import org.jspecify.annotations.NonNull;

public enum ImpetusChargeDisplay implements StringRepresentable {
    NEVER("never"),
    ALWAYS("always"),
    ON_CHANGE("on_change");

    public static final Codec<ImpetusChargeDisplay> CODEC = StringRepresentable.fromEnum(ImpetusChargeDisplay::values);

    private final String name;

    ImpetusChargeDisplay(String name) {
        this.name = name;
    }

    @Override
    public @NonNull String getSerializedName() {
        return name;
    }
}
