//package com.blackmoss.impetusrift.compat.curios;
//
//import com.blackmoss.impetusrift.registry.IRItems;
//import com.leclowndu93150.thaumaturge.compat.curio.HoverHarnessCurio;
//import com.leclowndu93150.thaumaturge.compat.curio.client.HoverHarnessCurioRenderer;
//import net.minecraft.core.registries.BuiltInRegistries;
//import net.minecraft.world.entity.ai.attributes.AttributeModifier;
//import net.minecraft.world.item.Item;
//import net.minecraft.world.item.ItemStack;
//import net.neoforged.bus.api.IEventBus;
//import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
//import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
//import net.neoforged.neoforge.common.NeoForge;
//import net.neoforged.neoforge.items.IItemHandlerModifiable;
//import top.theillusivec4.curios.api.CuriosCapability;
//import top.theillusivec4.curios.api.client.ICurioRenderer;
//import top.theillusivec4.curios.api.event.CurioAttributeModifierEvent;
//
//public final class ImpetusRiftCuriosCompat {
//    public ImpetusRiftCuriosCompat() {
//    }
//
//    public static void init(IEventBus modBus) {
//        modBus.addListener(ImpetusRiftCuriosCompat::registerCurio);
//        modBus.addListener(ImpetusRiftCuriosCompat::onClientSetup);
//        NeoForge.EVENT_BUS.addListener(ImpetusRiftCuriosCompat::onItemAttributeModifier);
//    }
//
//    private static void registerCurio(RegisterCapabilitiesEvent event) {
//        event.registerItem(CuriosCapability.ITEM, (stack, ctx) -> new HoverHarnessCurio(stack), IRItems.THAUMOSTATIC_ELYTRA.get());
//    }
//
//    private static void registerPlain(RegisterCapabilitiesEvent event, Item... items) {
//        for(Item item : items) {
//            event.registerItem(CuriosCapability.ITEM, (stack, ctx) -> () -> stack, item);
//        }
//    }
//
//    private static void onClientSetup(FMLClientSetupEvent event) {
//        ICurioRenderer.register(IRItems.THAUMOSTATIC_ELYTRA.get(), HoverHarnessCurioRenderer::new);
//    }
//
//    @SuppressWarnings("removal")
//    public static record CurioPouchRef(IItemHandlerModifiable handler, int slot) {
//        public ItemStack stack() {
//            return this.handler.getStackInSlot(this.slot);
//        }
//    }
//}
