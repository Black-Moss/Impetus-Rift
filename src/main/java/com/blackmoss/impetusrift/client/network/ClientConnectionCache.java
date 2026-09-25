package com.blackmoss.impetusrift.client.network;

import com.blackmoss.impetusrift.energy.network.ImpetusConnection;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ClientConnectionCache {
    private static List<ImpetusConnection> connections = Collections.emptyList();

    private ClientConnectionCache() {
    }

    public static void update(List<ImpetusConnection> newConnections) {
        connections = List.copyOf(newConnections);
    }

    public static List<ImpetusConnection> getAllConnections() {
        return connections;
    }

    public static List<ImpetusConnection> getConnectionsFrom(BlockPos source) {
        List<ImpetusConnection> result = new ArrayList<>();
        for (ImpetusConnection conn : connections) {
            if (conn.sourcePos().equals(source)) {
                result.add(conn);
            }
        }
        return result;
    }

    public static List<ImpetusConnection> getConnectionsTo(BlockPos target) {
        List<ImpetusConnection> result = new ArrayList<>();
        for (ImpetusConnection conn : connections) {
            if (conn.targetPos().equals(target)) {
                result.add(conn);
            }
        }
        return result;
    }

    public static List<ImpetusConnection> getConnectionsAt(BlockPos pos) {
        List<ImpetusConnection> result = new ArrayList<>();
        for (ImpetusConnection conn : connections) {
            if (conn.involves(pos)) {
                result.add(conn);
            }
        }
        return result;
    }
}