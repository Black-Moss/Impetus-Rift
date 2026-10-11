package com.blackmoss.impetusrift.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class IRCommonConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue THRUSTER_SUPER_BOOST_MULTIPLIER = BUILDER
            .defineInRange("thruster_super_boost_multiplier", 9, 1, Integer.MAX_VALUE);

    public static final ModConfigSpec.DoubleValue THRUSTER_SUPER_BOOST_MIN_SPEED = BUILDER
            .defineInRange("thruster_super_boost_min_speed", 0.8D, 0.0D, 10.0D);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private IRCommonConfig() {
    }
}
