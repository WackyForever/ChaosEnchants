package com.example.chaos.mixin;

import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ItemEnchantments.Mutable.class)
public class UncapEnchantmentMutableMixin {

    /**
     * Remove the 255 limit when setting an enchantment level.
     */
    @ModifyConstant(
            method = "set",
            constant = @Constant(intValue = 255)
    )
    private int chaosenchants$uncapSetLevel(int value) {
        return Integer.MAX_VALUE;
    }

    /**
     * Remove the 255 limit when upgrading an enchantment.
     */
    @ModifyConstant(
            method = "upgrade",
            constant = @Constant(intValue = 255)
    )
    private int chaosenchants$uncapUpgradeLevel(int value) {
        return Integer.MAX_VALUE;
    }
}
