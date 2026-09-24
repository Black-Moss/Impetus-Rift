package com.blackmoss.impetusrift.energy;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class ImpetusEnergyStorage implements IImpetusEnergyStorage {
    private final long maxReceive;
    private final long maxExtract;
    private long energy;
    private long capacity;

    public ImpetusEnergyStorage(long capacity) {
        this(capacity, capacity, capacity);
    }

    public ImpetusEnergyStorage(long capacity, long maxTransfer) {
        this(capacity, maxTransfer, maxTransfer);
    }

    public ImpetusEnergyStorage(long capacity, long maxReceive, long maxExtract) {
        this.capacity = capacity;
        this.maxReceive = maxReceive;
        this.maxExtract = maxExtract;
    }

    @Override
    public long receiveEnergy(long maxReceive, boolean simulate) {
        if (!canReceive()) {
            return 0;
        }

        long energyReceived = Math.min(capacity - energy, Math.min(this.maxReceive, maxReceive));
        if (!simulate) {
            energy += energyReceived;
            onEnergyChanged();
        }
        return energyReceived;
    }

    @Override
    public long extractEnergy(long maxExtract, boolean simulate) {
        if (!canExtract()) {
            return 0;
        }

        long energyExtracted = Math.min(energy, Math.min(this.maxExtract, maxExtract));
        if (!simulate) {
            energy -= energyExtracted;
            onEnergyChanged();
        }
        return energyExtracted;
    }

    @Override
    public long getEnergyStored() {
        return energy;
    }

    @Override
    public long getMaxEnergyStored() {
        return capacity;
    }

    @Override
    public boolean canExtract() {
        return this.energy > 0;
    }

    @Override
    public boolean canReceive() {
        return this.energy < this.capacity;
    }

    public void readFromNBT(ValueInput input) {
        this.energy = input.getLongOr("Energy", 0);
    }

    public void writeToNBT(ValueOutput output) {
        output.putLong("Energy", this.energy);
    }

    public void setEnergy(long energy) {
        this.energy = Math.clamp(energy, 0, capacity);
        onEnergyChanged();
    }

    public void setCapacity(long newCapacity) {
        this.capacity = newCapacity;
        if (this.energy > this.capacity) {
            this.energy = this.capacity;
        }
    }

    public void addEnergy(long amount) {
        receiveEnergy(amount, false);
    }

    public long removeEnergy(long amount) {
        return extractEnergy(amount, false);
    }

    public boolean hasEnergy(long amount) {
        return this.energy >= amount;
    }
}