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
            method = "award(Lnet/minecraft/advancements/AdvancementHolder;Ljava/lang/String;)Z",
            at = @At("RETURN")
    )
    private void onAdvancementCriterionAwarded(
            AdvancementHolder advancement,
            String criterionKey,
            CallbackInfoReturnable<Boolean> cir
    ) {
        // The criterion must have actually been newly awarded.
        if (!cir.getReturnValue()) {
            return;
        }

        // Check the progress of the entire advancement.
        AdvancementProgress progress =
                ((PlayerAdvancements) (Object) this)
                        .getOrStartProgress(advancement);

        // Only trigger ChaosEnchants when the COMPLETE
        // advancement has been finished.
        if (!progress.isDone()) {
            return;
        }

        AdvancementChaosHandler.onPlayerEarnAdvancement(player);
    }
}
