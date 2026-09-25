package com.blackmoss.impetusrift.block;

import com.blackmoss.impetusrift.ImpetusRift;
import com.blackmoss.impetusrift.energy.network.IImpetusNode;
import com.blackmoss.impetusrift.energy.network.ImpetusConnection;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public class ImpetusRelayBlockEntity extends BlockEntity implements IImpetusNode {
    private static final long RELAY_TRANSFER = 1000L;
    private static final long RECENT_ENERGY_WINDOW = 20L;

    private long buffer = 0L;
    private int ticks = 0;
    private long lastEnergyGameTime = -1L;
    private List<BlockPos> connectedTargets = new ArrayList<>();

    public ImpetusRelayBlockEntity(BlockPos pos, BlockState state) {
        super(IRBlockEntities.IMPETUS_RELAY.get(), pos, state);
    }

    public static void staticTick(Level level, BlockPos pos, BlockState state, ImpetusRelayBlockEntity be) {
        be.tick();
    }

    private void tick() {
        if (level == null || level.isClientSide()) return;
        ticks++;
        if (ticks % 10 == 0) {
            updateConnectedTargets();
            syncToClient();
        }
    }

    private void markRecentEnergy() {
        if (level != null) lastEnergyGameTime = level.getGameTime();
    }

    private void updateConnectedTargets() {
        if (level == null || level.isClientSide()) return;
        ImpetusNetworkManager mgr = ImpetusNetworkManager.get(level);
        if (mgr == null) return;
        List<BlockPos> newTargets = mgr.getConnectionsFrom(worldPosition).stream()
                .map(ImpetusConnection::targetPos)
                .toList();
        if (!connectedTargets.equals(newTargets)) {
            connectedTargets = new ArrayList<>(newTargets);
            setChanged();
            syncToClient();
        }
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
        if (!simulate) {
            buffer += accepted;
            markRecentEnergy();
        }
        return accepted;
    }

    @Override
    public long extractEnergy(long maxExtract, boolean simulate) {
        long extracted = Math.min(maxExtract, buffer);
        if (!simulate) {
            buffer -= extracted;
            if (extracted > 0) markRecentEnergy();
        }
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

    @Override
    public boolean hasRecentEnergy() {
        return level != null && (level.getGameTime() - lastEnergyGameTime) <= RECENT_ENERGY_WINDOW;
    }

    @Override
    public List<BlockPos> getBeamTargetPositions() {
        return connectedTargets;
    }

    protected final void syncToClient() {
        if (level == null || level.isClientSide()) return;
        BlockState current = getBlockState();
        level.sendBlockUpdated(getBlockPos(), current, current, 3);
    }

    @Override
    protected void saveAdditional(@NonNull ValueOutput output) {
        super.saveAdditional(output);
        // 仅持久化连接目标与能量时间戳；buffer 为瞬时缓存，不写入磁盘（保持"不存储能量"）
        output.putLong("LastEnergyTime", lastEnergyGameTime);
        output.putInt("BeamTargets", connectedTargets.size());
        for (int i = 0; i < connectedTargets.size(); i++) {
            BlockPos t = connectedTargets.get(i);
            output.putInt("BT" + i + "X", t.getX());
            output.putInt("BT" + i + "Y", t.getY());
            output.putInt("BT" + i + "Z", t.getZ());
        }
    }

    @Override
    protected void loadAdditional(@NonNull ValueInput input) {
        super.loadAdditional(input);
        lastEnergyGameTime = input.getLongOr("LastEnergyTime", -1L);
        int count = input.getIntOr("BeamTargets", 0);
        connectedTargets = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            int x = input.getIntOr("BT" + i + "X", 0);
            int y = input.getIntOr("BT" + i + "Y", 0);
            int z = input.getIntOr("BT" + i + "Z", 0);
            connectedTargets.add(new BlockPos(x, y, z));
        }
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
}
