package com.blackmoss.impetusrift.compat.jade;

import com.blackmoss.impetusrift.ImpetusRift;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin(ImpetusRift.MODID)
public class ImpetusRiftJadePlugin implements IWailaPlugin {
    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(
                ImpetusNodeDataProvider.INSTANCE,
                BlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(
                ImpetusNodeClientProvider.INSTANCE,
                Block.class);
    }
}
