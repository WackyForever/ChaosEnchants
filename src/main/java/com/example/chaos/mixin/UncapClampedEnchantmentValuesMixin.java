
package com.example.chaos.mixin;

import net.minecraft.world.item.enchantment.LevelBasedValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelBasedValue.Clamped.class)
public class UncapClampedEnchantmentValuesMixin {

    @Inject(
            method = "calculate",
            at = @At("HEAD"),
            cancellable = true
    )
    private void chaosenchants$removeMaximumClamp(
            int level,
            CallbackInfoReturnable<Float> cir
    ) {
        LevelBasedValue.Clamped clamped =
                (LevelBasedValue.Clamped) (Object) this;

        float calculated = clamped.value().calculate(level);

        // Keep the minimum, but ignore the configured maximum.
        cir.setReturnValue(Math.max(clamped.min(), calculated));
    }
}
