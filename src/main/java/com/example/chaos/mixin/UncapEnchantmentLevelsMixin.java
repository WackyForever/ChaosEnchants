package com.example.chaos.mixin;

import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ItemEnchantments.class)
public class UncapEnchantmentLevelsMixin {

    /**
     * Remove the vanilla 255 level limit from ItemEnchantments.
     */
    @ModifyConstant(
            method = "<init>",
            constant = @Constant(intValue = 255)
    )
    private int chaosenchants$uncapConstructorLevel(int value) {
        return Integer.MAX_VALUE;
    }

    /**
     * Remove the 255 limit from the ItemEnchantments codec.
     */
    @ModifyConstant(
            method = "<clinit>",
            constant = @Constant(intValue = 255)
    )
    private static int chaosenchants$uncapCodecLevel(int value) {
        return Integer.MAX_VALUE;
    }
}
