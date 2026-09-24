package com.blackmoss.impetusrift.energy;

public interface IImpetusEnergyStorage {
    long receiveEnergy(long maxReceive, boolean simulate);

    long extractEnergy(long maxExtract, boolean simulate);

    long getEnergyStored();

    long getMaxEnergyStored();

    boolean canExtract();

    boolean canReceive();

    default void onEnergyChanged() {
    }
}