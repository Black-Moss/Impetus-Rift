package com.blackmoss.impetusrift.registry;

import com.blackmoss.impetusrift.ImpetusRift;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class IRCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ImpetusRift.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> IMPETUS_RIFT = CREATIVE_MODE_TABS.register(ImpetusRift.MODID,
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.impetusrift"))
                    .icon(() -> IRItems.RIFT_JAR.get().getDefaultInstance())
                    .displayItems((_, output) -> {
                        output.accept(IRItems.IMPETUS_DRAINER.get());
                        output.accept(IRItems.IMPETUS_DIFFUSER.get());
                        output.accept(IRItems.IMPETUS_GENERATOR.get());
                        output.accept(IRItems.IMPETUS_MATRIX.get());
                        output.accept(IRItems.IMPETUS_ALTAR_BASE.get());
                        output.accept(IRItems.IMPETUS_MIRROR.get());
                        output.accept(IRItems.IMPETUS_RELAY.get());
                        output.accept(IRItems.IMPETUS_GATE.get());

                        output.accept(IRItems.RIFT_FEEDER.get());
                        output.accept(IRItems.RIFT_MOVER_INPUT.get());
                        output.accept(IRItems.RIFT_MOVER_OUTPUT.get());
                        output.accept(IRItems.RIFT_JAR.get());
                        output.accept(IRItems.RIFT_MONITOR.get());

                        output.accept(IRItems.IMPETUS_CELL.get());
                        output.accept(IRItems.IMPETUS_GEM.get());
                        output.accept(IRItems.RIFT_SEED_FLUX.get());

                        output.accept(IRItems.RIFT_COMPASS.get());
                        output.accept(IRItems.IMPETUS_LINKER.get());

                        output.accept(IRItems.RIFT_TRAVELLER_BOOTS.get());

                        output.accept(IRItems.THAUMOSTATIC_ELYTRA.get());
                        output.accept(IRItems.ELYTRA_THRUSTER.get());
                    }).build());
}