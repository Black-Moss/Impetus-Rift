package com.blackmoss.impetusrift.compat.jade;

import com.blackmoss.impetusrift.ImpetusRift;
import com.blackmoss.impetusrift.block.ImpetusDrainerBlockEntity;
import com.blackmoss.impetusrift.block.ImpetusGateBlockEntity;
import com.blackmoss.impetusrift.block.RiftDioptraBlockEntity;
import com.blackmoss.impetusrift.energy.network.IImpetusNode;
import com.blackmoss.impetusrift.energy.network.ImpetusNetworkManager;
import com.leclowndu93150.thaumaturge.content.entity.EntityFluxRift;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jspecify.annotations.NonNull;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

public enum ImpetusNodeDataProvider implements IServerDataProvider<BlockAccessor> {
    INSTANCE;

    public static final String KEY_INPUT_COUNT = "inputCount";
    public static final String KEY_MAX_INPUTS = "maxInputs";
    public static final String KEY_OUTPUT_COUNT = "outputCount";
    public static final String KEY_MAX_OUTPUTS = "maxOutputs";
    public static final String KEY_HAS_RIFT = "hasRift";
    public static final String KEY_RIFT_SIZE = "riftSize";
    public static final String KEY_RIFT_STABILITY = "riftStability";
    public static final String KEY_GATE_ENABLED = "gateEnabled";
    public static final String KEY_MONITOR_MODE = "monitorMode";

    @Override
    public @NonNull Identifier getUid() {
        return ImpetusRift.identifier("impetus_node");
    }

    @Override
    public void appendServerData(@NonNull CompoundTag tag, BlockAccessor accessor) {
        BlockEntity be = accessor.getBlockEntity();
        if (!(be instanceof IImpetusNode node)) {
            return;
        }

        tag.putInt(KEY_MAX_INPUTS, node.getMaxInputs());
        tag.putInt(KEY_MAX_OUTPUTS, node.getMaxOutputs());

        if (node.getNodeLevel() instanceof ServerLevel serverLevel) {
            ImpetusNetworkManager mgr = ImpetusNetworkManager.get(serverLevel);
            if (mgr != null) {
                tag.putInt(KEY_INPUT_COUNT, mgr.getConnectionsTo(node.getNodePos()).size());
                tag.putInt(KEY_OUTPUT_COUNT, mgr.getConnectionsFrom(node.getNodePos()).size());
            }
        }

        if (be instanceof ImpetusGateBlockEntity gate) {
            tag.putBoolean(KEY_GATE_ENABLED, gate.isEnabled());
        }

        if (be instanceof RiftDioptraBlockEntity monitor) {
            tag.putInt(KEY_MONITOR_MODE, monitor.getMode());
            EntityFluxRift target = monitor.resolveTarget();
            tag.putBoolean(KEY_HAS_RIFT, target != null);
            if (target != null) {
                tag.putInt(KEY_RIFT_SIZE, target.getRiftSize());
                tag.putFloat(KEY_RIFT_STABILITY, target.getRiftStability());
            }
            return;
        }

        if (be instanceof ImpetusDrainerBlockEntity drainer) {
            boolean hasRift = drainer.hasBoundRift();
            tag.putBoolean(KEY_HAS_RIFT, hasRift);

            EntityFluxRift rift = drainer.getBoundRift();
            if (hasRift && rift != null) {
                tag.putInt(KEY_RIFT_SIZE, rift.getRiftSize());
                tag.putFloat(KEY_RIFT_STABILITY, rift.getRiftStability());
            }
        }
    }
}
