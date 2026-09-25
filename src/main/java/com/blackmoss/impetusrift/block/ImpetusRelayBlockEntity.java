package com.blackmoss.impetusrift.block;

import com.blackmoss.impetusrift.energy.network.IImpetusNode;
import com.blackmoss.impetusrift.energy.network.ImpetusNetworkManager;
import com.blackmoss.impetusrift.energy.network.ImpetusNodePorts;
import com.blackmoss.impetusrift.registry.IRBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ImpetusRelayBlockEntity extends BlockEntity implements IImpetusNode {
    private static final long RELAY_TRANSFER = 1000L;

    private long buffer = 0L;

    public ImpetusRelayBlockEntity(BlockPos pos, BlockState state) {
        super(IRBlockEntities.IMPETUS_RELAY.get(), pos, state);
    }

    @Override
    public BlockPos getNodePos() {
        return worldPosition;
    }

    @Override
    public Level getNodeLevel() {
        return level;
    }

    @Override
    public ImpetusNodePorts getPorts() {
        return ImpetusNodePorts.of(2, 2);
    }

    @Override
    public int getConnectedInputCount() {
        ImpetusNetworkManager mgr = level == null ? null : ImpetusNetworkManager.get(level);
        return mgr == null ? 0 : mgr.getConnectionsTo(worldPosition).size();
    }

    @Override
    public int getConnectedOutputCount() {
        ImpetusNetworkManager mgr = level == null ? null : ImpetusNetworkManager.get(level);
        return mgr == null ? 0 : mgr.getConnectionsFrom(worldPosition).size();
    }

    @Override
    public long receiveEnergy(long maxReceive, boolean simulate) {
        long free = RELAY_TRANSFER - buffer;
        if (free <= 0) return 0;
        long accepted = Math.min(maxReceive, free);
        if (!simulate) buffer += accepted;
        return accepted;
    }

    @Override
    public long extractEnergy(long maxExtract, boolean simulate) {
        long extracted = Math.min(maxExtract, buffer);
        if (!simulate) buffer -= extracted;
        return extracted;
    }

    @Override
    public long getEnergyStored() {
        return buffer;
    }

    @Override
    public long getMaxEnergyStored() {
        return RELAY_TRANSFER;
    }
}
