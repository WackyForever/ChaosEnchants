package com.example.chaos.mixin;

import com.example.chaos.event.AdvancementChaosHandler;
import net.minecraft.advancements.AdvancementHolder;
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
            at = @At("RETURN")
    )
    private void onAdvancementAwarded(
            AdvancementHolder advancement,
            String criterionKey,
            CallbackInfoReturnable<Boolean> cir
    ) {
        // Only continue if a new criterion was actually awarded.
        if (!cir.getReturnValue()) {
            return;
        }

        // Check whether the entire advancement is now completed.
        if (player.getAdvancements()
                .getOrStartProgress(advancement)
                .isDone()) {

            AdvancementChaosHandler.onPlayerEarnAdvancement(player);
        }
    }
}
