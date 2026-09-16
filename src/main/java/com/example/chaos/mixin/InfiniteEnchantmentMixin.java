package com.example.chaos.mixin;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Enchantment.class)
public class InfiniteEnchantmentMixin {

    /**
     * Allows ANY enchantment to be applied to ANY item.
     * This includes dirt, blocks, food, sticks, etc.
     */
    @Inject(
            method = "canEnchant",
            at = @At("HEAD"),
            cancellable = true
    )
    private void allowAnything(
            ItemStack stack,
            CallbackInfoReturnable<Boolean> cir
    ) {
        cir.setReturnValue(true);
    }

    /**
     * Allows ANY enchantment to coexist with ANY other enchantment.
     * This removes vanilla incompatibility restrictions such as:
     * Sharpness + Smite
     * Fortune + Silk Touch
     * Protection + Fire Protection
     * Infinity + Mending
     * etc.
     */
    @Inject(
            method = "canCombine",
            at = @At("HEAD"),
            cancellable = true
    )
    private void allowAllEnchantments(
            Enchantment other,
            CallbackInfoReturnable<Boolean> cir
    ) {
        cir.setReturnValue(true);
    }
}
