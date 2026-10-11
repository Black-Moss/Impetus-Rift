package com.blackmoss.impetusrift.mixin;

import com.blackmoss.impetusrift.registry.IRItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeAccess;
import net.minecraft.world.item.crafting.RecipePropertySet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SmithingMenu.class)
public class SmithingMenuMixin {
    @Inject(method = "createInputSlotDefinitions", at = @At("RETURN"), cancellable = true)
    private static void impetusrift$allowGliderBase(
            RecipeAccess recipes,
            CallbackInfoReturnable<ItemCombinerMenuSlotDefinition> cir) {
        RecipePropertySet template = recipes.propertySet(RecipePropertySet.SMITHING_TEMPLATE);
        RecipePropertySet base = recipes.propertySet(RecipePropertySet.SMITHING_BASE);
        RecipePropertySet addition = recipes.propertySet(RecipePropertySet.SMITHING_ADDITION);

        cir.setReturnValue(ItemCombinerMenuSlotDefinition.create()
                .withSlot(0, 8, 48, template::test)
                .withSlot(1, 26, 48, stack -> impetusrift$hasGlider(stack) || base.test(stack))
                .withSlot(2, 44, 48, stack -> stack.is(IRItems.ELYTRA_THRUSTER.get()) || addition.test(stack))
                .withResultSlot(3, 98, 48)
                .build());
    }

    @Unique
    private static boolean impetusrift$hasGlider(ItemStack stack) {
        return stack.has(DataComponents.GLIDER);
    }
}
