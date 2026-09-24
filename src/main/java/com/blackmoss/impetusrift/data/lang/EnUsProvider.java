package com.blackmoss.impetusrift.data.lang;

import com.blackmoss.impetusrift.ImpetusRift;
import com.blackmoss.impetusrift.registry.IRBlocks;
import com.blackmoss.impetusrift.registry.IRItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class EnUsProvider extends LanguageProvider {

    public EnUsProvider(PackOutput output) {
        super(output, ImpetusRift.MODID, "en_us");
    }

    @Override
    protected void addTranslations() {
        add("itemGroup.impetusrift", "Impetus Rift");

        addBlock(IRBlocks.IMPETUS_DRAINER, "Impetus Drainer");
        addBlock(IRBlocks.IMPETUS_DIFFUSER, "Impetus Diffuser");
        addBlock(IRBlocks.IMPETUS_GENERATOR, "Impetus Generator");
        addBlock(IRBlocks.IMPETUS_MATRIX, "Impetus Matrix");
        addBlock(IRBlocks.IMPETUS_ALTAR_BASE, "Impetus Altar Base");
        addBlock(IRBlocks.IMPETUS_MIRROR, "Impetus Mirror");
        addBlock(IRBlocks.IMPETUS_RELAY, "Impetus Relay");
        addBlock(IRBlocks.IMPETUS_GATE, "Impetus Gate");
        addBlock(IRBlocks.RIFT_FEEDER, "Rift Feeder");
        addBlock(IRBlocks.RIFT_MOVER_INPUT, "Rift Mover Input");
        addBlock(IRBlocks.RIFT_MOVER_OUTPUT, "Rift Mover Output");
        addBlock(IRBlocks.RIFT_JAR, "Rift Jar");
        addBlock(IRBlocks.RIFT_MONITOR, "Rift Monitor");
        addBlock(IRBlocks.RIFT_BARRIER, "Rift Barrier");

        addItem(IRItems.IMPETUS_CELL, "Impetus Cell");
        addItem(IRItems.IMPETUS_GEM, "Impetus Gem");
        addItem(IRItems.RIFT_SEED_FLUX, "Rift Seed Flux");
        addItem(IRItems.RIFT_COMPASS, "Rift Compass");
        addItem(IRItems.IMPETUS_LINKER, "Impetus Linker");

        addResearch("impetus_drainer", "Impetus Drainer",
                "The Impetus Drainer is a device that can drain Impetus energy from nearby sources.",
                "This device can be used to collect Impetus energy for use in other machines. It requires a nearby Impetus source to function.");

        addResearch("rift_feeder", "Rift Feeder",
                "The Rift Feeder is a device that can feed items into a rift.",
                "This device can be used to automatically feed items into a rift for processing or storage.");

        jade();
        impetusLinker();
    }

    private void addResearch(String researchId, String title, String... stage) {
        add("research.%s.%s.title".formatted(ImpetusRift.MODID, researchId), title);
        for (int i = 0; i < stage.length; i++) {
            add("research.%s.%s.stage_%d".formatted(ImpetusRift.MODID, researchId, i), stage[i]);
        }
    }

    private void jade() {
        add("config.jade.plugin_impetusrift.input_nodes", "Input Nodes");
        add("jade.impetusrift.input_nodes", "Input Nodes: %1$s/%2$s");
        add("jade.impetusrift.output_nodes", "Output Nodes: %1$s/%2$s");
        add("jade.impetusrift.energy", "Impetus Energy: %1$s/%2$s");
        add("jade.impetusrift.rift_size", "Rift Size: %1$s");
        add("jade.impetusrift.stability", "Stability: %1$s");
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
        add("item.impetusrift.node.unknown", "Unknown Node");
    }
}