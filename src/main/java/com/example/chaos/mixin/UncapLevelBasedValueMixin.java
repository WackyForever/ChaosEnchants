package com.example.chaos.mixin;

import net.minecraft.world.item.enchantment.LevelBasedValue;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LevelBasedValue.class)
public interface UncapLevelBasedValueMixin {
}
