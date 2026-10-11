package com.blackmoss.impetusrift.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class IRClientConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue THRUSTER_SUPER_BOOST_HOLD_TIME = BUILDER
            .defineInRange("thruster_super_boost_hold_time", 20, 10, Integer.MAX_VALUE);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private IRClientConfig() {
    }
}
