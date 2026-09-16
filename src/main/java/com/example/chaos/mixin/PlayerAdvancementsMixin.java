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

    @Inject(
            method = "endTrackingCompleted(Lnet/minecraft/advancements/AdvancementHolder;)V",
            at = @At("HEAD")
    )
    private void onAdvancementCompleted(
            AdvancementHolder advancement,
            CallbackInfo ci
    ) {
        AdvancementChaosHandler.onPlayerEarnAdvancement(player);
    }
}
