package com.blackmoss.impetusrift.energy;

public interface IImpetusEnergyReceiver {
    long receiveEnergy(long maxReceive, boolean simulate);
}