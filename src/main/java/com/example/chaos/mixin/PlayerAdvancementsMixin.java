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

        // Store whether this advancement was already complete.
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
        // The criterion must have actually been newly awarded.
        if (!cir.getReturnValue()) {
            return;
        }

        AdvancementProgress progress =
                ((PlayerAdvancements) (Object) this)
                        .getOrStartProgress(advancement);

        boolean wasComplete =
                PlayerAdvancementState.wasComplete(advancement);

        // Only fire when the advancement changed from
        // incomplete -> complete.
        if (!wasComplete && progress.isDone()) {
            AdvancementChaosHandler.onPlayerEarnAdvancement(player);
        }

        PlayerAdvancementState.remove(advancement);
    }
}
