package com.example.chaos;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public final class EnchantmentLevelHelper {

    private EnchantmentLevelHelper() {
    }

    public static int getLevel(
            ResourceKey<Enchantment> enchantmentKey,
            ItemStack stack
    ) {
        Holder<Enchantment> enchantment =
                BuiltInRegistries.ENCHANTMENT
                        .getHolder(enchantmentKey)
                        .orElse(null);

        if (enchantment == null) {
            return 0;
        }

        return EnchantmentHelper.getItemEnchantmentLevel(
                enchantment,
                stack
        );
    }
}
