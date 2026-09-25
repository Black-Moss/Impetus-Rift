package com.blackmoss.impetusrift.mixin;

import com.blackmoss.impetusrift.energy.ImpetusEnergyStorage;
import com.blackmoss.impetusrift.energy.network.IImpetusNode;
import com.blackmoss.impetusrift.energy.network.ImpetusNodePorts;
import com.leclowndu93150.thaumaturge.content.device.BlockEntityVoidSiphon;
import com.leclowndu93150.thaumaturge.content.entity.EntityFluxRift;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntityVoidSiphon.class)
@Implements(@Interface(iface = IImpetusNode.class, prefix = "impetusrift$"))
public abstract class BlockEntityVoidSiphonMixin extends BlockEntity {
    @Shadow
    private int counter;

    @Shadow
    private int progress;

    @Final
    @Shadow
    private ItemStacksResourceHandler output;

    @Shadow
    private boolean hasOutputRoom() {
        throw new AssertionError();
    }

    @Shadow
    private java.util.List<EntityFluxRift> getValidRifts(Level level, BlockPos pos) {
        throw new AssertionError();
    }

    @Unique
    private final ImpetusEnergyStorage impetusrift$energyStorage = new ImpetusEnergyStorage(100, 100, 0);

    public BlockEntityVoidSiphonMixin(BlockEntityType<?> type, BlockPos worldPosition, BlockState blockState) {
        super(type, worldPosition, blockState);
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

    @Unique
    private void impetus_Rift$emitSeed() {
        ItemStack current = output.getResource(0).toStack(output.getAmountAsInt(0));
        if (current.isEmpty()) {
            output.set(0, ItemResource.of(new ItemStack(TCItems.VOID_SEED.get())), 1);
        } else {
            output.set(0, output.getResource(0), current.getCount() + 1);
        }
    }

    /**
     * @author Black_Moss
     * @reason Allow Void Siphon to convert Impetus Energy into Void Seed.
     */
    @Overwrite
    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityVoidSiphon siphon) {
        BlockEntityVoidSiphonMixin self = (BlockEntityVoidSiphonMixin) (Object) siphon;
        if (self == null) {
            return;
        }
        ++self.counter;
        if (!state.getValue(BlockStateProperties.ENABLED) || self.counter % 20 != 0 || !self.hasOutputRoom()) {
            return;
        }

        boolean changed = false;

        long energy = self.impetusrift$energyStorage.getEnergyStored();
        if (energy > 0L) {
            self.impetusrift$energyStorage.addEnergy(-energy);
            self.progress += (int) (energy * 20L);
        }

        if (self.impetusrift$energyStorage.getEnergyStored() < 100L) {
            for (EntityFluxRift rift : self.getValidRifts(level, pos)) {
                double d = Math.sqrt(rift.getRiftSize());
                self.progress += (int) d;
                rift.setRiftStability((float) (rift.getRiftStability() - d / 15.0F));
                if (level.getRandom().nextInt(33) == 0) {
                    rift.setRiftSize(rift.getRiftSize() - 1);
                }
            }
        }

        while (self.progress >= 2000 && self.hasOutputRoom()) {
            self.progress -= 2000;
            self.impetus_Rift$emitSeed();
            changed = true;
        }

        if (changed) {
            self.setChanged();
        }
    }

    @Inject(method = "saveAdditional", at = @At("TAIL"))
    private void impetusriftSaveEnergy(ValueOutput output_, CallbackInfo ci) {
        impetusrift$energyStorage.writeToNBT(output_);
    }

    @Inject(method = "loadAdditional", at = @At("TAIL"))
    private void impetusriftLoadEnergy(ValueInput input, CallbackInfo ci) {
        impetusrift$energyStorage.readFromNBT(input);
    }
}
