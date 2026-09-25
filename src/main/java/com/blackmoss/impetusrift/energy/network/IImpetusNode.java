package com.blackmoss.impetusrift.energy.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

import java.util.Collections;
import java.util.List;

public interface IImpetusNode {
    default Component getNodeLabel() {
        Level level = getNodeLevel();
        BlockPos pos = getNodePos();
        if (level != null && pos != null) {
            return Component.translatable(level.getBlockState(pos).getBlock().getDescriptionId());
        }
        return Component.translatable("tooltip.impetusrift.node.unknown");
    }

    default boolean isBulkStorage() {
        return false;
    }

    BlockPos getNodePos();

    Level getNodeLevel();

    ImpetusNodePorts getPorts();

    default int getMaxInputs() {
        return getPorts().inputs();
    }

    default int getMaxOutputs() {
        return getPorts().outputs();
    }

    default boolean canOutput() {
        return getMaxOutputs() > 0;
    }

    default boolean canInput() {
        return getMaxInputs() > 0;
    }

    default boolean isBidirectional() {
        return canOutput() && canInput();
    }

    long receiveEnergy(long maxReceive, boolean simulate);

    long extractEnergy(long maxExtract, boolean simulate);

    long getEnergyStored();

    long getMaxEnergyStored();

    default boolean canReceive() {
        return getEnergyStored() < getMaxEnergyStored();
    }

    default boolean canExtract() {
        return getEnergyStored() > 0;
    }

    default int getConnectedOutputCount() {
        return 0;
    }

    default int getConnectedInputCount() {
        return 0;
    }

    default void onEnergyChanged() {
    }

    default float getStability() {
        return 0.0F;
    }

    default List<BlockPos> getBeamTargetPositions() {
        return Collections.emptyList();
    }

    default boolean hasActiveTransfer() {
        return canExtract();
    }

    default boolean hasRecentEnergy() {
        return false;
    }
}