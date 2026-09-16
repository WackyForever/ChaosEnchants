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

```
@Shadow
private ServerPlayer player;

@Inject(
        method = "award",
        at = @At("RETURN")
)
private void chaosEnchants$afterAward(
        AdvancementHolder advancement,
        String criterionKey,
        CallbackInfoReturnable<Boolean> cir
) {
    // The criterion was not newly awarded.
    if (!cir.getReturnValue()) {
        return;
    }

    // Get the advancement's progress after the criterion was awarded.
    AdvancementProgress progress =
            ((PlayerAdvancements) (Object) this)
                    .getOrStartProgress(advancement);

    // Only trigger ChaosEnchants when the entire advancement
    // has just become complete.
    if (!progress.isDone()) {
        return;
    }

    AdvancementChaosHandler.onPlayerEarnAdvancement(player);
}
```

}
