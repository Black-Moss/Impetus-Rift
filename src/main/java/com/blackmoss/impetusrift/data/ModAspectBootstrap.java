package com.blackmoss.impetusrift.data;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.data.worldgen.aspect.AspectBootstrap;
import net.minecraft.data.worldgen.BootstrapContext;

public final class ModAspectBootstrap {
    private ModAspectBootstrap() {
    }

    public static void bootstrap(BootstrapContext<IAspect> ctx) {
        AspectBootstrap.bootstrap(ctx);
    }
}