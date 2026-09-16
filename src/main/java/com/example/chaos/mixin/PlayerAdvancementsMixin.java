package com.example.chaos.mixin;

import com.example.chaos.event.AdvancementChaosHandler;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerAdvancements.class)
public class PlayerAdvancementsMixin {

@Shadow
private ServerPlayer player;

@Unique
private AdvancementHolder chaosEnchants$lastAdvancement;


@Unique
private boolean chaosEnchants$wasComplete;

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

    chaosEnchants$lastAdvancement = advancement;
    chaosEnchants$wasComplete = progress.isDone();
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
    if (!cir.getReturnValue()) {
        return;
    }

    if (chaosEnchants$lastAdvancement != advancement) {
        return;
    }

    AdvancementProgress progress =
        ((PlayerAdvancements) (Object) this)
            .getOrStartProgress(advancement);

    if (chaosEnchants$wasComplete) {
        return;
    }

    if (!progress.isDone()) {
        return;
    }

    AdvancementChaosHandler.onPlayerEarnAdvancement(player);

    chaosEnchants$lastAdvancement = null;
    chaosEnchants$wasComplete = false;
}

}
