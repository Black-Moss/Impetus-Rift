package com.blackmoss.impetusrift.energy.network;

import com.blackmoss.impetusrift.ImpetusRift;
import com.blackmoss.impetusrift.network.ClientboundConnectionsSyncPayload;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImpetusNetworkManager extends SavedData {
    public static final Codec<ImpetusNetworkManager> CODEC = RecordCodecBuilder.create(
            builder -> builder
                    .group(ImpetusConnection.CODEC.listOf()
                            .fieldOf("connections")
                            .forGetter(m -> new ArrayList<>(m.connections))
                    ).apply(builder, connectionList -> new ImpetusNetworkManager(new ArrayList<>(connectionList))));
    private static final String DATA_NAME = "impetus_network";
    public static final SavedDataType<ImpetusNetworkManager> TYPE = new SavedDataType<>(
            ImpetusRift.identifier(DATA_NAME),
            ImpetusNetworkManager::new,
            CODEC,
            DataFixTypes.LEVEL);

    private final List<ImpetusConnection> connections;
    private final List<ImpetusConnection> toRemove = new ArrayList<>();
    private final List<ImpetusConnection> toAdd = new ArrayList<>();

    private final Map<BlockPos, Long> matrixTransferAccumulator = new HashMap<>();
    private static final double TRANSFER_PER_SECOND_MIN = 1.0;
    private static final double TRANSFER_PER_SECOND_MAX = 50.0;
    private final Map<ImpetusConnection, Double> transferAccumulator = new HashMap<>();
    private final Map<ImpetusConnection, Double> transferRate = new HashMap<>();

    public ImpetusNetworkManager() {
        this(new ArrayList<>());
    }

    private ImpetusNetworkManager(List<ImpetusConnection> connections) {
        this.connections = connections;
    }

    public static ImpetusNetworkManager get(Level level) {
        if (level instanceof ServerLevel serverLevel) {
            return serverLevel.getDataStorage().computeIfAbsent(TYPE);
        }
        return null;
    }

    public boolean addConnection(BlockPos source, BlockPos target, Level level) {
        if (source.distManhattan(target) > ImpetusConnection.MAX_DISTANCE) {
            return false;
        }

        ImpetusConnection conn = new ImpetusConnection(source, target);
        if (connections.contains(conn)) {
            return false;
        }

        IImpetusNode sourceNode = getNodeAt(level, source);
        IImpetusNode targetNode = getNodeAt(level, target);
        if (sourceNode == null || targetNode == null) {
            return false;
        }

        if (!sourceNode.canOutput()) {
            return false;
        }
        if (sourceNode.getConnectedOutputCount() >= sourceNode.getMaxOutputs()) {
            return false;
        }

        if (!targetNode.canInput()) {
            return false;
        }
        if (targetNode.getConnectedInputCount() >= targetNode.getMaxInputs()) {
            return false;
        }

        toAdd.add(conn);
        setDirty();
        return true;
    }

    public void removeConnectionsAt(BlockPos pos) {
        for (ImpetusConnection conn : connections) {
            if (conn.involves(pos)) {
                toRemove.add(conn);
            }
        }
        if (!toRemove.isEmpty()) {
            setDirty();
        }
    }

    public void removeConnection(BlockPos source, BlockPos target) {
        ImpetusConnection conn = new ImpetusConnection(source, target);
        toRemove.add(conn);
        setDirty();
    }

    public List<ImpetusConnection> getAllConnections() {
        return List.copyOf(connections);
    }

    public List<ImpetusConnection> getConnectionsFrom(BlockPos source) {
        List<ImpetusConnection> result = new ArrayList<>();
        for (ImpetusConnection conn : connections) {
            if (conn.sourcePos().equals(source)) {
                result.add(conn);
            }
        }
        return result;
    }

    public List<ImpetusConnection> getConnectionsTo(BlockPos target) {
        List<ImpetusConnection> result = new ArrayList<>();
        for (ImpetusConnection conn : connections) {
            if (conn.targetPos().equals(target)) {
                result.add(conn);
            }
        }
        return result;
    }

    public List<ImpetusConnection> getConnectionsAt(BlockPos pos) {
        List<ImpetusConnection> result = new ArrayList<>();
        for (ImpetusConnection conn : connections) {
            if (conn.involves(pos)) {
                result.add(conn);
            }
        }
        return result;
    }

    public void tick(Level level) {
        boolean changed = false;

        if (!toAdd.isEmpty()) {
            connections.addAll(toAdd);
            toAdd.clear();
            changed = true;
        }
        if (!toRemove.isEmpty()) {
            connections.removeAll(toRemove);
            toRemove.forEach(c -> {
                transferAccumulator.remove(c);
                transferRate.remove(c);
            });
            toRemove.clear();
            changed = true;
        }

        if (changed && level instanceof ServerLevel serverLevel) {
            syncToClients(serverLevel);
        }

        if (level.getGameTime() % 20 == 0) {
            matrixTransferAccumulator.clear();
            for (ImpetusConnection conn : connections) {
                double r = TRANSFER_PER_SECOND_MIN
                        + (TRANSFER_PER_SECOND_MAX - TRANSFER_PER_SECOND_MIN) * level.getRandom().nextDouble();
                transferRate.put(conn, r);
            }
        }

        for (ImpetusConnection conn : connections) {
            processConnection(conn, level);
        }
    }

    private void syncToClients(ServerLevel serverLevel) {
        var packet = new ClientboundConnectionsSyncPayload(
                List.copyOf(connections));
        for (var player : serverLevel.players()) {
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player, packet);
        }
    }

    private void processConnection(ImpetusConnection conn, Level level) {
        IImpetusNode source = getNodeAt(level, conn.sourcePos());
        IImpetusNode target = getNodeAt(level, conn.targetPos());

        if (source == null || target == null) {
            toRemove.add(conn);
            return;
        }

        if (!hasLineOfSight(level, conn.sourcePos(), conn.targetPos())) {
            return;
        }

        if (!source.canOutput() || !target.canInput()) {
            return;
        }

        if (!source.canExtract() || !target.canReceive()) {
            return;
        }

        long transferAmount = calculateTransfer(conn, source, target);

        if (transferAmount <= 0) {
            return;
        }

        long extracted = source.extractEnergy(transferAmount, true);
        if (extracted > 0) {
            long received = target.receiveEnergy(extracted, false);
            if (received > 0) {
                source.extractEnergy(received, false);
                source.onEnergyChanged();
                target.onEnergyChanged();

                if (level instanceof ServerLevel serverLevel) {
                    NetworkTickHandler.damageEntitiesInBeam(
                            serverLevel, conn.sourcePos(), conn.targetPos(), received);
                }
            }
        }
    }

    private long calculateTransfer(ImpetusConnection conn, IImpetusNode source, IImpetusNode target) {
        if (source.isBulkStorage() && target.isBulkStorage()) {
            return calculateMatrixToMatrixTransfer(source);
        }

        double rate = transferRate.getOrDefault(conn, (TRANSFER_PER_SECOND_MIN + TRANSFER_PER_SECOND_MAX) / 2.0);
        double perTick = rate / 20.0;
        double acc = transferAccumulator.getOrDefault(conn, 0.0) + perTick;
        long budget = (long) Math.floor(acc);
        if (budget <= 0) {
            transferAccumulator.put(conn, acc);
            return 0;
        }
        long available = Math.min(source.getEnergyStored(), target.getMaxEnergyStored() - target.getEnergyStored());
        long result = Math.min(budget, available);
        transferAccumulator.put(conn, acc - result);
        return result;
    }

    private long calculateMatrixToMatrixTransfer(IImpetusNode source) {
        long maxPerSecond = (long) (source.getMaxEnergyStored() * ImpetusConnection.MATRIX_TO_MATRIX_RATE);
        long alreadyTransferred = matrixTransferAccumulator.getOrDefault(source.getNodePos(), 0L);
        long remaining = maxPerSecond - alreadyTransferred;

        if (remaining <= 0) {
            return 0;
        }

        long perTick = Math.max(1, maxPerSecond / 20);
        return Math.min(perTick, remaining);
    }

    private boolean hasLineOfSight(Level level, BlockPos from, BlockPos to) {
        Vec3 start = Vec3.atCenterOf(from);
        Vec3 end = Vec3.atCenterOf(to);

        BlockHitResult hit = level.clip(new ClipContext(
                start, end,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                CollisionContext.empty()));

        if (hit.getType() == HitResult.Type.BLOCK) {
            BlockPos hitPos = hit.getBlockPos();
            return hitPos.equals(from) || hitPos.equals(to);
        }

        return true;
    }

    @Nullable
    private IImpetusNode getNodeAt(Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof IImpetusNode node) {
            return node;
        }
        return null;
    }
}