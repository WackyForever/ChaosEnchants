package com.example.chaos.mixin;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Enchantment.class)
public class InfiniteEnchantmentMixin {
    // Overrides vanilla rules so ANY item or block accepts ANY enchantment
    @Inject(method = "canEnchant", at = @At("HEAD"), cancellable = true)
    private void allowAnything(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }
}
