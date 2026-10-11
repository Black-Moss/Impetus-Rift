package com.blackmoss.impetusrift.compat.jade;

import com.blackmoss.impetusrift.ImpetusRift;
import com.blackmoss.impetusrift.energy.network.IImpetusNode;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jspecify.annotations.NonNull;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

public enum ImpetusEnergyDataProvider implements IServerDataProvider<BlockAccessor> {
    INSTANCE;

    public static final String KEY_ENERGY = "energy";
    public static final String KEY_MAX_ENERGY = "maxEnergy";

    @Override
    public @NonNull Identifier getUid() {
        return ImpetusRift.identifier("impetus_energy");
    }

    @Override
    public void appendServerData(@NonNull CompoundTag tag, BlockAccessor accessor) {
        BlockEntity be = accessor.getBlockEntity();
        if (!(be instanceof IImpetusNode node)) {
            return;
        }

        tag.putLong(KEY_ENERGY, node.getEnergyStored());
        tag.putLong(KEY_MAX_ENERGY, node.getMaxEnergyStored());
    }
}
