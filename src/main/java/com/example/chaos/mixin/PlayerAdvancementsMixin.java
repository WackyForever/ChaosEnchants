```java
package com.example.chaos.mixin;

import com.example.chaos.event.AdvancementChaosHandler;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerAdvancements.class)
public class PlayerAdvancementsMixin {

    @Shadow
    private ServerPlayer player;

    @Inject(
            method = "award",
            at = @At("HEAD")
    )
    private void chaosEnchants$beforeAward(
            AdvancementHolder advancement,
            String criterionKey,
            CallbackInfoReturnable<Boolean> cir
    ) {
        /*
         * Nothing is done here.
         *
         * We intentionally do not store advancement state.
         * PlayerAdvancements.award() can be called while Minecraft
         * is loading/restoring advancement information.
         */
    }

    @Inject(
            method = "award",
            at = @At("RETURN")
    )
    private void chaosEnchants$afterAward(
            AdvancementHolder advancement,
            String criterionKey,
            CallbackInfoReturnable<Boolean> cir
    ) {
        /*
         * If award() returned false, this criterion was not newly awarded.
         */
        if (!cir.getReturnValue()) {
            return;
        }

        /*
         * Get the advancement's current progress AFTER the criterion
         * was awarded.
         */
        AdvancementProgress progress =
                ((PlayerAdvancements) (Object) this)
                        .getOrStartProgress(advancement);

        /*
         * Only react when the ENTIRE advancement is now complete.
         *
         * This prevents individual criteria from increasing the
         * multiplier before the advancement itself is finished.
         */
        if (!progress.isDone()) {
            return;
        }

        /*
         * At this point:
         *
         * 1. A criterion was actually awarded.
         * 2. The advancement is completely finished.
         *
         * Trigger ChaosEnchants exactly once for this completion.
         */
        AdvancementChaosHandler.onPlayerEarnAdvancement(player);
    }
}
```
