package com.blackmoss.impetusrift.block;

import com.blackmoss.impetusrift.ImpetusRift;
import com.blackmoss.impetusrift.energy.IImpetusRechargeable;
import com.blackmoss.impetusrift.energy.ImpetusChargeHelper;
import com.blackmoss.impetusrift.energy.ImpetusEnergyStorage;
import com.blackmoss.impetusrift.energy.network.IImpetusNode;
import com.blackmoss.impetusrift.energy.network.ImpetusNetworkManager;
import com.blackmoss.impetusrift.energy.network.ImpetusNodePorts;
import com.blackmoss.impetusrift.registry.IRBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class ImpetusDiffuserBlockEntity extends BlockEntity implements IImpetusNode {
    private static final long BUFFER_CAPACITY = 500L;
    private static final long CHARGE_PER_ITEM = 25L;
    private static final int SCAN_INTERVAL = 20;
    private static final double SCAN_RADIUS = 7.0;

    private final ImpetusEnergyStorage energyStorage = new ImpetusEnergyStorage(BUFFER_CAPACITY, BUFFER_CAPACITY, BUFFER_CAPACITY);
    private boolean active = false;
    private int ticks;

    public ImpetusDiffuserBlockEntity(BlockPos pos, BlockState state) {
        super(IRBlockEntities.IMPETUS_DIFFUSER.get(), pos, state);
        this.ticks = (int) (Math.random() * SCAN_INTERVAL);
    }

    public static void staticTick(
            Level level,
            BlockPos pos,
            BlockState state,
            ImpetusDiffuserBlockEntity diffuser) {
        diffuser.tick();
    }

    private void tick() {
        if (level == null || level.isClientSide()) return;
        ticks++;
        if (ticks % SCAN_INTERVAL == 0) {
            boolean charged = chargeNearbyItems();
            setActive(charged);
        }
    }

    @Override
    public ImpetusNodePorts getPorts() {
        return ImpetusNodePorts.of(2, 0);
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
    public long receiveEnergy(long maxReceive, boolean simulate) {
        long received = energyStorage.receiveEnergy(maxReceive, simulate);
        if (received > 0 && !simulate) {
            setChanged();
        }
        return received;
    }

    @Override
    public long extractEnergy(long maxExtract, boolean simulate) {
        return 0;
    }

    @Override
    public long getEnergyStored() {
        return energyStorage.getEnergyStored();
    }

    @Override
    public long getMaxEnergyStored() {
        return energyStorage.getMaxEnergyStored();
    }

    @Override
    public int getConnectedInputCount() {
        if (level == null) return 0;
        ImpetusNetworkManager mgr = ImpetusNetworkManager.get(level);
        return mgr != null
                ? mgr.getConnectionsTo(worldPosition).size()
                : 0;
    }

    @Override
    public void onEnergyChanged() {
        setChanged();
    }

    private boolean chargeNearbyItems() {
        if (level == null || energyStorage.getEnergyStored() <= 0) return false;

        AABB area = new AABB(
                worldPosition.getX() - SCAN_RADIUS,
                worldPosition.getY() - SCAN_RADIUS,
                worldPosition.getZ() - SCAN_RADIUS,
                worldPosition.getX() + SCAN_RADIUS + 1,
                worldPosition.getY() + SCAN_RADIUS + 1,
                worldPosition.getZ() + SCAN_RADIUS + 1);

        List<Player> players = level.getEntitiesOfClass(Player.class, area);
        boolean anyCharged = false;

        for (Player player : players) {
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                if (energyStorage.getEnergyStored() <= 0) break;
                ItemStack stack = player.getInventory().getItem(i);
                if (stack.isEmpty()) continue;
                // 物品侧元动能源：IImpetusRechargeable + ImpetusChargeHelper（IMPETUS_ENERGY 数据组件）
                if (!(stack.getItem() instanceof IImpetusRechargeable rechargeable)) continue;

                int current = ImpetusChargeHelper.getCharge(stack);
                long max = rechargeable.getMaxCharge(stack, player);
                int room = (int) Math.max(0L, max - current);
                if (room <= 0) continue;

                int toCharge = (int) Math.min(CHARGE_PER_ITEM, Math.min(room, energyStorage.getEnergyStored()));
                if (toCharge <= 0) continue;

                ImpetusChargeHelper.setCharge(stack, current + toCharge);
                energyStorage.extractEnergy(toCharge, false);
                anyCharged = true;
            }
        }
        return anyCharged;
    }

    protected final void syncToClient() {
        if (level == null || level.isClientSide()) return;
        BlockState current = getBlockState();
        level.sendBlockUpdated(getBlockPos(), current, current, 3);
    }

    @Override
    protected void saveAdditional(@NonNull ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean("Active", active);
        output.putInt("Ticks", ticks);
        energyStorage.writeToNBT(output.child("EnergyStorage"));
    }

    @Override
    protected void loadAdditional(@NonNull ValueInput input) {
        super.loadAdditional(input);
        active = input.getBooleanOr("Active", false);
        ticks = input.getIntOr("Ticks", 0);
        input.child("EnergyStorage").ifPresent(energyStorage::readFromNBT);
    }

    @Override
    public @NonNull CompoundTag getUpdateTag(HolderLookup.@NonNull Provider registries) {
        CompoundTag nbt = super.getUpdateTag(registries);
        try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(this.problemPath(), ImpetusRift.LOGGER)) {
            TagValueOutput output = TagValueOutput.createWithContext(reporter, registries);
            saveAdditional(output);
            nbt.merge(output.buildResult());
        }
        return nbt;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public ImpetusEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    public boolean isActive() {
        return active;
    }

    private void setActive(boolean active) {
        if (this.active != active) {
            this.active = active;
            setChanged();
            syncToClient();
        }
    }
}