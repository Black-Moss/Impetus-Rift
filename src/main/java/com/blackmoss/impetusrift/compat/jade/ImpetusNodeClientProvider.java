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

    private static final String PREFIX = "jade.impulserift.";

    @Override
    public @NonNull Identifier getUid() {
        return ImpetusRift.IRIdentifier("impetus_node");
    }

    @Override
    public void appendTooltip(@NonNull ITooltip tooltip, BlockAccessor accessor, @NonNull IPluginConfig config) {
        CompoundTag data = accessor.getServerData();
        if (data.isEmpty()) {
            return;
        }

        int maxInputs = data.getIntOr(ImpetusNodeDataProvider.KEY_MAX_INPUTS, 0);
        int maxOutputs = data.getIntOr(ImpetusNodeDataProvider.KEY_MAX_OUTPUTS, 0);
        long energy = data.getLongOr(ImpetusNodeDataProvider.KEY_ENERGY, 0L);
        long maxEnergy = data.getLongOr(ImpetusNodeDataProvider.KEY_MAX_ENERGY, 0L);

        if (maxInputs > 0) {
            int inputCount = data.getIntOr(ImpetusNodeDataProvider.KEY_INPUT_COUNT, 0);
            tooltip.add(Component.translatable(PREFIX + "input_nodes", inputCount, maxInputs));
        }
        if (maxOutputs > 0) {
            int outputCount = data.getIntOr(ImpetusNodeDataProvider.KEY_OUTPUT_COUNT, 0);
            tooltip.add(Component.translatable(PREFIX + "output_nodes", outputCount, maxOutputs));
        }

        if (energy > 0) {
            tooltip.add(Component.translatable(PREFIX + "energy", energy, maxEnergy));
        }

        if (data.contains(ImpetusNodeDataProvider.KEY_HAS_RIFT)) {
            appendDrainerDiagnostic(tooltip, data);
        }
    }

    private static void appendDrainerDiagnostic(ITooltip tooltip, CompoundTag data) {
        boolean enabled = data.getBooleanOr(ImpetusNodeDataProvider.KEY_ENABLED, true);
        int riftCount = data.getIntOr(ImpetusNodeDataProvider.KEY_RIFT_COUNT, 0);
        boolean hasRift = data.getBooleanOr(ImpetusNodeDataProvider.KEY_HAS_RIFT, false);

        if (!enabled) {
            tooltip.add(Component.translatable(PREFIX + "disabled"));
            return;
        }

        if (hasRift) {
            int size = data.getIntOr(ImpetusNodeDataProvider.KEY_RIFT_SIZE, 0);
            float stability = data.getFloatOr(ImpetusNodeDataProvider.KEY_RIFT_STABILITY, 0.0F);
            boolean collapse = data.getBooleanOr(ImpetusNodeDataProvider.KEY_RIFT_COLLAPSE, false);

            tooltip.add(Component.translatable(PREFIX + "draining")
                    .append(Component.literal("  "))
                    .append(Component.translatable(PREFIX + "rift_size", size))
                    .append(Component.literal("  "))
                    .append(Component.translatable(PREFIX + "stability", String.format("%.1f", stability))));

            if (collapse) {
                tooltip.add(Component.translatable(PREFIX + "collapsing"));
            }
        } else {
            tooltip.add(Component.translatable(PREFIX + "no_rift")
                    .append(Component.literal("  "))
                    .append(Component.translatable(PREFIX + "nearby_rifts", riftCount)));
        }
    }
}
