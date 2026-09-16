package com.example.chaos.mixin;

import com.example.chaos.event.AdvancementChaosHandler;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerAdvancements.class)
public class PlayerAdvancementsMixin {

    @Shadow
    private ServerPlayer player;

    /*
     * This method is called by Minecraft when an advancement
     * has actually been completed.
     *
     * This is much better than injecting into award(), because
     * award() is called for individual criteria.
     */
    @Inject(
            method = "endTrackingCompleted",
            at = @At("HEAD")
    )
    private void onAdvancementCompleted(
            AdvancementHolder advancement,
            CallbackInfo ci
    ) {
        AdvancementChaosHandler.onPlayerEarnAdvancement(player);
    }
}
