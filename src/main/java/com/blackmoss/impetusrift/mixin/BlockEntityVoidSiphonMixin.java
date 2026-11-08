package com.blackmoss.impetusrift.mixin;

import com.blackmoss.impetusrift.energy.ImpetusEnergyStorage;
import com.blackmoss.impetusrift.energy.network.IImpetusNode;
import com.blackmoss.impetusrift.energy.network.ImpetusNodePorts;
import com.leclowndu93150.thaumaturge.content.device.BlockEntityVoidSiphon;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntityVoidSiphon.class)
@Implements(@Interface(iface = IImpetusNode.class, prefix = "impetusrift$"))
public abstract class BlockEntityVoidSiphonMixin extends BlockEntity {
    @Unique
    public final ImpetusEnergyStorage impetusrift$energyStorage = new ImpetusEnergyStorage(100, 100, 0);

    @Shadow
    private int progress;

    @Shadow
    private int ticks;

    public BlockEntityVoidSiphonMixin(BlockEntityType<?> type, BlockPos worldPosition, BlockState blockState) {
        super(type, worldPosition, blockState);
    }

    @Inject(method = "serverTick", at = @At("TAIL"))
    private static void convertEnergyToProgress(
            Level level,
            BlockPos pos,
            BlockState state,
            BlockEntityVoidSiphon siphon,
            CallbackInfo ci) {
        BlockEntityVoidSiphonMixin self = (BlockEntityVoidSiphonMixin) (Object) siphon;
        if (self != null && (!state.getValue(BlockStateProperties.ENABLED) || self.ticks % 20 != 0)) {
            return;
        }
        long energy = 0;
        if (self != null) {
            energy = self.impetusrift$energyStorage.getEnergyStored();
        }
        if (energy <= 0L) {
            return;
        }
        self.impetusrift$energyStorage.addEnergy(-energy);
        self.progress += (int) (energy * 20L);
        self.setChanged();
    }

    @Unique
    public BlockPos impetusrift$getNodePos() {
        return worldPosition;
    }

    @Unique
    public Level impetusrift$getNodeLevel() {
        return level;
    }

    @Unique
    public ImpetusNodePorts impetusrift$getPorts() {
        return ImpetusNodePorts.of(1, 0);
    }

    @Unique
    public long impetusrift$receiveEnergy(long maxReceive, boolean simulate) {
        long received = impetusrift$energyStorage.receiveEnergy(maxReceive, simulate);
        if (received > 0 && !simulate) setChanged();
        return received;
    }

    @Unique
    public long impetusrift$extractEnergy(long maxExtract, boolean simulate) {
        return impetusrift$energyStorage.extractEnergy(maxExtract, simulate);
    }

    @Unique
    public long impetusrift$getEnergyStored() {
        return impetusrift$energyStorage.getEnergyStored();
    }

    @Unique
    public long impetusrift$getMaxEnergyStored() {
        return impetusrift$energyStorage.getMaxEnergyStored();
    }

    @Inject(method = "saveAdditional", at = @At("TAIL"))
    private void impetusriftSaveEnergy(ValueOutput valueOutput, CallbackInfo ci) {
        impetusrift$energyStorage.writeToNBT(valueOutput);
    }

    @Inject(method = "loadAdditional", at = @At("TAIL"))
    private void impetusriftLoadEnergy(ValueInput input, CallbackInfo ci) {
        impetusrift$energyStorage.readFromNBT(input);
    }
}
