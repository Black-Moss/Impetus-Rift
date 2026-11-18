package com.blackmoss.impetusrift.data.lang;

import com.blackmoss.impetusrift.ImpetusRift;
import com.blackmoss.impetusrift.registry.IRBlocks;
import com.blackmoss.impetusrift.registry.IRCreativeModeTabs;
import com.blackmoss.impetusrift.registry.IRItems;
import com.blackmoss.lazycat.lang.LazyCatLanguageProvider;
import net.minecraft.data.PackOutput;

public class EnUsProvider extends LazyCatLanguageProvider {
    public EnUsProvider(PackOutput output) {
        super(output, ImpetusRift.MODID, "en_us");
    }

    @Override
    protected void addTranslations() {
        addCreativeModeTab(IRCreativeModeTabs.IMPETUS_RIFT.get(), "Impetus Rift");

        addBlock(IRBlocks.IMPETUS_DRAINER, "Impetus Drainer");
        addBlock(IRBlocks.IMPETUS_DIFFUSER, "Impetus Diffuser");
        addBlock(IRBlocks.IMPETUS_GENERATOR, "Impetus Generator");
        addBlock(IRBlocks.IMPETUS_MATRIX, "Impetus Matrix");
        addBlock(IRBlocks.IMPETUS_ALTAR_BASE, "Impetus Altar Base");
        addBlock(IRBlocks.IMPETUS_MIRROR, "Impetus Mirror");
        addBlock(IRBlocks.IMPETUS_RELAY, "Impetus Relay");
        addBlock(IRBlocks.IMPETUS_GATE, "Impetus Gate");
        addBlock(IRBlocks.FLUX_INDUCER, "Flux Inducer");
        addBlock(IRBlocks.METASPATIAL_ACCUMULATOR, "Metaspatial Accumulator");
        addBlock(IRBlocks.METASPATIAL_EXTRUDER, "Metaspatial Extruder");
        addBlock(IRBlocks.RIFT_JAR, "Rift Jar");
        addBlock(IRBlocks.EXTRADIMENSIONAL_MONITOR, "Extradimensional Monitor");

        addItem(IRItems.IMPETUS_CELL, "Impetus Cell");
        addItem(IRItems.IMPETUS_GEM, "Impetus Gem");
        addItem(IRItems.RIFT_SEED_FLUX, "Rift Seed Flux");
        addItem(IRItems.RIFT_COMPASS, "Rift Compass");
        addItem(IRItems.IMPETUS_LINKER, "Impetus Linker");
        addItem(IRItems.RIFT_TRAVELLER_BOOTS, "Rift Traveller Boots");
        addItem(IRItems.THAUMOSTATIC_ELYTRA, "Thaumostatic Elytra");
        addItem(IRItems.ELYTRA_THRUSTER, "Elytra Thruster");

        research();
        configuration();
        jade();
        tooltip();
        impetusLinker();
    }

    private void research() {
        addResearchCategory("impetuology", "Impetuology");
        addResearch("unlock_impetuology", "Discovery: Impetuology");
    }

    private void configuration() {
        addConfiguration("title", "Impetus Rife Configuration");

        addConfiguration("thruster_super_boost_hold_time", "Thruster Super Boost Hold Time");

        addConfiguration("thruster_super_boost_multiplier", "Thruster Super Boost Multiplier");
        addConfiguration("thruster_super_boost_min_speed", "Thruster Super Boost Min Speed");
    }

    private void jade() {
        add("config.jade.plugin_impetusrift.impetus_node", "Impetus Node");
        add("config.jade.plugin_impetusrift.impetus_energy", "Impetus Energy");
        add("jade.impetusrift.impetus_node.input_nodes", "Input Nodes: %1$s/%2$s");
        add("jade.impetusrift.impetus_node.output_nodes", "Output Nodes: %1$s/%2$s");
        add("jade.impetusrift.impetus_node.rift_size", "Rift Size: %1$s");
        add("jade.impetusrift.impetus_node.stability", "Stability: %1$s");
        add("jade.impetusrift.impetus_node.no_target", "No rift detected");
        add("jade.impetusrift.impetus_node.gate_enabled", "State: Passing");
        add("jade.impetusrift.impetus_node.gate_disabled", "State: Blocked");
        add("jade.impetusrift.impetus_node.monitor_mode_size", "Mode: Size");
        add("jade.impetusrift.impetus_node.monitor_mode_stability", "Mode: Stability");
        add("jade.impetusrift.impetus_energy.energy", "Impetus Energy: %1$s/%2$s");
    }

    private void tooltip() {
        addTooltip("unknown", "Unknown Node");
        addTooltip("impetus_energy", "Impetus Energy: %s / %s");
        addTooltip("thaumostatic_elytra.charge", "Impetus Energy: %s / %s");
        addTooltip("thaumostatic_elytra.thruster", "Thruster Fuel: %s / %s");
    }

    // 元动谐振器
    private void impetusLinker() {
        add("item.impetusrift.impetus_linker.too_far", "Too far (max %1$s blocks)");
        add("item.impetusrift.impetus_linker.blocked", "Blocked by an opaque block");
        add("item.impetusrift.impetus_linker.no_source", "Source node does not exist");
        add("item.impetusrift.impetus_linker.no_target", "Target node does not exist");
        add("item.impetusrift.impetus_linker.cannot_output", "Source (%1$s) cannot output");
        add("item.impetusrift.impetus_linker.output_full", "Source output ports full (%1$s/%2$s)");
        add("item.impetusrift.impetus_linker.cannot_input", "Target (%1$s) cannot input");
        add("item.impetusrift.impetus_linker.input_full", "Target input ports full (%1$s/%2$s)");
        add("item.impetusrift.impetus_linker.disconnected", "Disconnected: %1$s ✕ %2$s");
        add("item.impetusrift.impetus_linker.connected", "Connected: %1$s → %2$s");
        add("item.impetusrift.impetus_linker.failed", "Connection failed (unknown reason)");

        add("item.impetusrift.impetus_linker.mirror_bound", "Impetus Mirrors bound");
        add("item.impetusrift.impetus_linker.mirror_unbound", "Impetus Mirror unbound");
        add("item.impetusrift.impetus_linker.mirror_need_pair", "Two Impetus Mirrors are required");
        add("item.impetusrift.impetus_linker.mirror_select_source", "Sneak-right-click a mirror first to pick the source");
        add("item.impetusrift.impetus_linker.mirror_self", "A mirror cannot be bound to itself");
        add("item.impetusrift.impetus_linker.mirror_target_busy", "This mirror is already bound to another");
        add("item.impetusrift.impetus_linker.mirror_source_busy", "The source mirror is already bound to another");
    }
}
