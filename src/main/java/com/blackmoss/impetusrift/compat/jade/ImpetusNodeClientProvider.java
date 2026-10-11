package com.blackmoss.impetusrift.compat.jade;

import com.blackmoss.impetusrift.ImpetusRift;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum ImpetusNodeClientProvider implements IBlockComponentProvider {
    INSTANCE;

    private static final String PREFIX = "jade.impetusrift.impetus_node.";

    /** 阀门开关状态。 */
    private static void appendGateStatus(ITooltip tooltip, CompoundTag data) {
        boolean enabled = data.getBooleanOr(ImpetusNodeDataProvider.KEY_GATE_ENABLED, true);
        tooltip.add(Component.translatable(PREFIX + (enabled ? "gate_enabled" : "gate_disabled")));
    }

    /** 监测仪：当前模式 + 目标裂隙读数。 */
    private static void appendMonitorDiagnostic(ITooltip tooltip, CompoundTag data) {
        int mode = data.getIntOr(ImpetusNodeDataProvider.KEY_MONITOR_MODE, 0);
        tooltip.add(Component.translatable(
                PREFIX + (mode == 1 ? "monitor_mode_stability" : "monitor_mode_size")));
        appendRiftReadout(tooltip, data);
    }

    /** 汲取仪：已绑定裂隙的读数。 */
    private static void appendRiftReadout(ITooltip tooltip, CompoundTag data) {
        boolean hasRift = data.getBooleanOr(ImpetusNodeDataProvider.KEY_HAS_RIFT, false);
        if (!hasRift) {
            tooltip.add(Component.translatable(PREFIX + "no_target"));
            return;
        }
        int size = data.getIntOr(ImpetusNodeDataProvider.KEY_RIFT_SIZE, 0);
        float stability = data.getFloatOr(ImpetusNodeDataProvider.KEY_RIFT_STABILITY, 0.0F);

        tooltip.add(Component.translatable(PREFIX + "rift_size", size)
                .append(Component.literal("  "))
                .append(Component.translatable(PREFIX + "stability", String.format("%.1f", stability))));
    }

    @Override
    public @NonNull Identifier getUid() {
        return ImpetusRift.identifier("impetus_node");
    }

    @Override
    public void appendTooltip(@NonNull ITooltip tooltip, BlockAccessor accessor, @NonNull IPluginConfig config) {
        CompoundTag data = accessor.getServerData();
        if (data.isEmpty()) {
            return;
        }

        int maxInputs = data.getIntOr(ImpetusNodeDataProvider.KEY_MAX_INPUTS, 0);
        int maxOutputs = data.getIntOr(ImpetusNodeDataProvider.KEY_MAX_OUTPUTS, 0);

        if (maxInputs > 0) {
            int inputCount = data.getIntOr(ImpetusNodeDataProvider.KEY_INPUT_COUNT, 0);
            tooltip.add(Component.translatable(PREFIX + "input_nodes", inputCount, maxInputs));
        }
        if (maxOutputs > 0) {
            int outputCount = data.getIntOr(ImpetusNodeDataProvider.KEY_OUTPUT_COUNT, 0);
            tooltip.add(Component.translatable(PREFIX + "output_nodes", outputCount, maxOutputs));
        }

        if (data.contains(ImpetusNodeDataProvider.KEY_GATE_ENABLED)) {
            appendGateStatus(tooltip, data);
            return;
        }

        if (data.contains(ImpetusNodeDataProvider.KEY_MONITOR_MODE)) {
            appendMonitorDiagnostic(tooltip, data);
            return;
        }

        if (data.contains(ImpetusNodeDataProvider.KEY_HAS_RIFT)) {
            appendRiftReadout(tooltip, data);
        }
    }
}
