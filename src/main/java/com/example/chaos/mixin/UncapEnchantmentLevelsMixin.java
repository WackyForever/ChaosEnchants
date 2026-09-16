package com.example.chaos.mixin;

import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ItemEnchantments.class)
public class UncapEnchantmentLevelsMixin {

    /**
     * Minecraft 1.21 normally uses 255 as the maximum
     * enchantment level when validating ItemEnchantments.
     *
     * Replace that limit with Integer.MAX_VALUE.
     */
    @ModifyConstant(
            method = "<init>",
            constant = @Constant(intValue = 255)
    )
    private int chaosenchants$uncapConstructorLevel(int value) {
        return Integer.MAX_VALUE;
    }
}
