package com.example.chaos;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public final class EnchantmentLevelHelper {

    private EnchantmentLevelHelper() {
    }

    public static int getLevel(
            ResourceKey<Enchantment> enchantmentKey,
            ItemStack stack,
            Registry<Enchantment> enchantmentRegistry
    ) {
        Holder<Enchantment> enchantment =
                enchantmentRegistry.getHolder(enchantmentKey).orElse(null);

        if (enchantment == null) {
            return 0;
        }

        return EnchantmentHelper.getItemEnchantmentLevel(enchantment, stack);
    }
}
