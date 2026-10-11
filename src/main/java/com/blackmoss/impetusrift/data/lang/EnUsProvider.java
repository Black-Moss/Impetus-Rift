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

        addItem(IRItems.IMPETUS_CELL, "Impetus Cell");
        addItem(IRItems.IMPETUS_GEM, "Impetus Gem");
        addItem(IRItems.RIFT_SEED_FLUX, "Rift Seed Flux");
        addItem(IRItems.RIFT_COMPASS, "Rift Compass");
        addItem(IRItems.IMPETUS_LINKER, "Impetus Linker");
        addItem(IRItems.RIFT_TRAVELLER_BOOTS, "Rift Traveller Boots");
        addItem(IRItems.THAUMOSTATIC_ELYTRA, "Thaumostatic Elytra");

        research();
        jade();
        tooltip();
        impetusLinker();
    }

    private void research() {
        add("research_category.impetusrift.impetuology", "Impetuology");

        addResearch("unlock_impetuology", "Discovery: Impetuology",
                "Impetus is a kind of energy drawn out of flux rifts. Until now I could only use a Void Siphon to condense it into Void Seeds, or keep a little in a gauntlet's cell — not much more than that. I want to automate the whole process.<BR>The first step is the Impetus Gem. It doesn't generate energy by itself; it only receives and passes Impetus along. So I first need a source — a way to pull Impetus out of a flux rift. A Void Siphon can do that, but it needs a nether star, which is far too expensive. I'm going to build a device based on the same principle that doesn't need one.<BR>There's still plenty of work ahead, but compared with the problems that stalled me before, the remaining steps are clear enough.",
                "Now I can really start using Impetus.<BR>I reworked the Void Siphon's design and built several new devices that gather, transfer, and distribute Impetus automatically. Impetuology is a bit convoluted — the best way to understand it is to set up a simple system and run it yourself.<BR>The sections below explain how the whole thing works.");

        addResearch("base_impetuology", "Basic Impetuology",
                "There are two kinds of Impetus.<BR>Ordinary Impetus is what seeps out of a rift. Devices can draw it from nearby, and touching it does no harm.<BR>Shifting Impetus is ordinary Impetus after processing. It's stronger, travels farther, and forms a visible beam — the more energy it carries, the brighter the beam. It has to be routed through connections between nodes; it can't be drawn straight from a nearby rift the way ordinary Impetus can.<BR>Any block connected to a beam is called an Impetus Node (nothing to do with aura nodes). Every node has input and output slots: producers have outputs only, carriers have both, and consumers have inputs only.",
                "How to use the Linker:<BR>· Hold the Linker and sneak-right-click a node to select it (blue highlight). Sneak-right-click the same node again to deselect.<BR>· Once selected, right-click another node to link them (the first one outputs, the second one inputs). Right-click the second one again to remove the link.<BR>· To send energy to several nodes: select the source, then right-click each target in turn. Mind the slot limits.<BR>Connection limits: beams reach at most 8 blocks and can't pass through opaque blocks; glass and other transparent blocks let them through. When a node is selected, nodes feeding it show a red frame, nodes it feeds show green, and nodes doing both show purple. If you can't tell, just follow the direction of the beam.<BR>Common nodes:<BR>· Impetus Drainer: place it next to a flux rift. It converts ordinary Impetus into Shifting Impetus and outputs it. Smaller rifts yield less, and draining makes the rift shrink and destabilise, so keep an eye on it. A redstone signal shuts it down.<BR>· Impetus Relay: 2 in, 2 out. It only relays beams and extends their reach.<BR>· Impetus Diffuser: absorbs Shifting Impetus and diffuses ordinary Impetus outward, charging devices that would otherwise run off a rift (a gauntlet with an Impetus Cell, a Void Siphon, and so on). Never let it feed a Drainer — that creates an endless loop.");

//        addResearch("impetus_generator", "Impetus Generator",
//                "The Drainer can pull Impetus out of a rift, but it isn't meant for steady generation — it just converts the energy and passes it on. I want a machine built to generate power: the Impetus Generator. Place it next to a rift and it keeps turning Impetus into usable energy.",
//                "The Impetus Generator works when placed next to a rift. The bigger and more stable the rift, the more it produces.<BR>Its output changes with the rift's size and stability, so to keep it running long-term you have to watch that rift the same way you would with a Drainer.");
//
//        addResearch("impetus_matrix", "Impetus Matrix",
//                "Once Impetus is drawn out, it needs somewhere to be stored. A rift itself can't serve as a battery — it's too unstable, and you can't draw a measured amount from it. I need a device that can safely hold a lot of Impetus.<BR>Ordinary Impetus can be kept in a gauntlet's cell, in Void Seeds, or in a special Impetus Cell, but Shifting Impetus is too active for any of those. My idea is to reuse the Runic Matrix from infusion: if it can steady the infusion process, the same principle should steady Shifting Impetus too.",
//                "Using the Impetus Matrix:<BR>· Build: place a Runic Matrix between two eldritch pedestals, one above and one below, then activate the structure with Salis Mundus. Break any part and the whole thing falls apart.<BR>· Cells: use an Impetus Cell on any side of a pedestal to insert it. One per side, four per pedestal, eight in total. More cells means more storage and faster throughput. Use an empty hand on that side to take a cell out — but any energy beyond the remaining cells' capacity is lost.<BR>· Stability: the fuller it gets, the less stable it becomes. Place arcane items within a 5x5 area to raise stability, and place them symmetrically, or they'll make things worse. Repeating the same item has diminishing returns, so use a variety. When unstable it fires harmful arcs, shakes cells loose, and so on. With Goggles of Revealing you can see the stability gain and loss.<BR>· Usage: link in or out with the Linker — it's 1 in, 1 out. The rune colour shows the level: red for empty, yellow for half, bright green for full. A redstone comparator next to it outputs a matching signal.");
//
//        addResearch("impetus_gate", "Impetus Gate",
//                "Right now the only way to stop energy flowing between two nodes is to delete the connection — there's no way to block it temporarily, because I can't stop the beam inside a node; I can only redirect it. Redstone is usually good at suppressing magic, but it can't hold back a beam of Shifting Impetus; the energy in it is too concentrated.<BR>I'll try warded arcana instead. Its protective magic should let a device refuse incoming beams, and that ward can be controlled from inside the device.",
//                "The Impetus Gate is done. Without a redstone signal it behaves like a normal Relay. With a signal, an internal shield turns half-solid and blocks beams in every direction. Cut the signal and the shield collapses at once, letting energy through again.");
//
//        addResearch("impetus_mirror", "Impetus Mirror",
//                "Sending energy over long distances is still a hassle: beams only travel a limited distance, so linking distant nodes means stacking up a pile of Relays. Mirrors can carry items and essentia across long distances, so the same principle should carry a beam too — as long as I stop Shifting Impetus from dispersing in the Void and reverting to ordinary energy.",
//                "Using the Impetus Mirrors:<BR>· Craft a pair, place one in the world, and right-click it with the other to bind them. Then place the second one wherever you like; energy can travel between them.<BR>· The link works across distance, even across dimensions. But if you're too far away, time stops around the mirror, so either stand nearby or build something to counter that effect.<BR>· Each mirror is 1 in, 1 out and links with the Linker; the mirror-to-mirror link itself is a two-way channel.<BR>A few cautions: transfer speed is limited (to keep energy from dispersing in the Void), so it's not suited to high throughput; heavy use puts pressure on the nearby aura and creates flux at both mirrors; and they cost far more than a Relay, so use them sparingly.");
//
//        addResearch("impetus_traveller_boots", "Impetus Traveller Boots",
//                "If Impetus can run machines, it should be able to boost my own movement too. I want a pair of boots that let me jump higher, move steadier, and land without getting hurt.",
//                "Effects of the Impetus Traveller Boots:<BR>· While sprinting they consume Impetus and double both step height and jump height.<BR>· On landing they spend Impetus to absorb the fall: each point cancels 5 blocks of falling distance. If there isn't enough energy, the rest of the fall hurts as normal.<BR>· Energy can be replenished with something like the Impetus Diffuser.");
    }

    private void addResearch(String researchId, String title, String... stage) {
        add("research.%s.%s.title".formatted(ImpetusRift.MODID, researchId), title);
        for (int i = 0; i < stage.length; i++) {
            add("research.%s.%s.stage_%d".formatted(ImpetusRift.MODID, researchId, i), stage[i]);
        }
    }

    private void jade() {
        add("config.jade.plugin_impetusrift.impetusrift", "Impetus Rift");
        add("jade.impetusrift.input_nodes", "Input Nodes: %1$s/%2$s");
        add("jade.impetusrift.output_nodes", "Output Nodes: %1$s/%2$s");
        add("jade.impetusrift.energy", "Impetus Energy: %1$s/%2$s");
        add("jade.impetusrift.rift_size", "Rift Size: %1$s");
        add("jade.impetusrift.stability", "Stability: %1$s");
    }

    private void tooltip() {
        add("tooltip.impetusrift.node.unknown", "Unknown Node");
        add("tooltip.impetusrift.impetus_energy", "Impetus Energy: %s / %s");
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
    }
}
