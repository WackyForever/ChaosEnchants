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
        AdvancementProgress progress =
                ((PlayerAdvancements) (Object) this)
                        .getOrStartProgress(advancement);

        PlayerAdvancementState.setWasComplete(
                advancement,
                progress.isDone()
        );
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
        AdvancementProgress progress =
                ((PlayerAdvancements) (Object) this)
                        .getOrStartProgress(advancement);

        boolean wasComplete =
                PlayerAdvancementState.wasComplete(advancement);

        PlayerAdvancementState.remove(advancement);

        /*
         * award() returns true when the criterion was actually awarded.
         *
         * We also require the advancement to have changed from
         * incomplete -> complete.
         */
        if (!cir.getReturnValue()) {
            return;
        }

        if (wasComplete) {
            return;
        }

        if (!progress.isDone()) {
            return;
        }

        AdvancementChaosHandler.onPlayerEarnAdvancement(player);
    }
}
```
