package com.blackmoss.impetusrift.compat.jade;

import com.blackmoss.impetusrift.ImpetusRift;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum ImpetusEnergyClientProvider implements IBlockComponentProvider {
    INSTANCE;

    private static final String PREFIX = "jade.impetusrift.impetus_energy.";

    @Override
    public @NonNull Identifier getUid() {
        return ImpetusRift.identifier("impetus_energy");
    }

    @Override
    public void appendTooltip(@NonNull ITooltip tooltip, BlockAccessor accessor, @NonNull IPluginConfig config) {
        CompoundTag data = accessor.getServerData();
        if (data.isEmpty()) {
            return;
        }

        long energy = data.getLongOr(ImpetusEnergyDataProvider.KEY_ENERGY, 0L);
        long maxEnergy = data.getLongOr(ImpetusEnergyDataProvider.KEY_MAX_ENERGY, 0L);

        if (energy > 0) {
            tooltip.add(Component.translatable(PREFIX + "energy", energy, maxEnergy));
        }
    }
}
