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

import java.util.HashSet;
import java.util.Set;

@Mixin(PlayerAdvancements.class)
public class PlayerAdvancementsMixin {

    @Shadow
    private ServerPlayer player;

    /*
     * Advancements that we have already processed for this player.
     *
     * This prevents the same completed advancement from increasing
     * the multiplier multiple times.
     */
    private final Set<AdvancementHolder> chaosEnchants$processed =
            new HashSet<>();

    @Inject(
            method = "award",
            at = @At("RETURN")
    )
    private void chaosEnchants$onAward(
            AdvancementHolder advancement,
            String criterionKey,
            CallbackInfoReturnable<Boolean> cir
    ) {
        // The criterion wasn't newly awarded.
        if (!cir.getReturnValue()) {
            return;
        }

        AdvancementProgress progress =
                ((PlayerAdvancements) (Object) this)
                        .getOrStartProgress(advancement);

        // The advancement isn't complete yet.
        if (!progress.isDone()) {
            return;
        }

        /*
         * If this advancement was already processed, don't trigger
         * the multiplier again.
         */
        if (!chaosEnchants$processed.add(advancement)) {
            return;
        }

        AdvancementChaosHandler.onPlayerEarnAdvancement(player);
    }
}
