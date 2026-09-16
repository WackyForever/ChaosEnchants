package com.example.chaos.mixin;

import com.example.chaos.event.AdvancementChaosHandler;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Mixin(PlayerAdvancements.class)
public class PlayerAdvancementsMixin {

    @Shadow
    private ServerPlayer player;

    /*
     * Minecraft's saved/current advancement progress.
     */
    @Shadow
    private Map<AdvancementHolder, AdvancementProgress> progress;

    /*
     * Advancements that have already been counted by ChaosEnchants.
     *
     * This prevents the same advancement from multiplying the
     * multiplier more than once.
     */
    private final Set<AdvancementHolder> chaosEnchants$processed =
            new HashSet<>();

    /*
     * True while Minecraft is loading/reloading advancement data.
     *
     * Anything that happens during this period must NOT count as
     * a newly earned advancement.
     */
    private boolean chaosEnchants$loading = false;

    /*
     * Minecraft loads the player's saved advancement progress here.
     *
     * We completely disable our award handler while loading.
     */
    @Inject(
            method = "load",
            at = @At("HEAD")
    )
    private void chaosEnchants$startLoading(
            ServerAdvancementManager advancementManager,
            CallbackInfo ci
    ) {
        chaosEnchants$loading = true;
    }

    /*
     * Once loading is finished, remember every advancement that is
     * already complete.
     *
     * These were earned previously, so they must NOT increase the
     * multiplier just because the player joined the world.
     */
    @Inject(
            method = "load",
            at = @At("RETURN")
    )
    private void chaosEnchants$finishLoading(
            ServerAdvancementManager advancementManager,
            CallbackInfo ci
    ) {
        chaosEnchants$processed.clear();

        for (Map.Entry<AdvancementHolder, AdvancementProgress> entry
                : progress.entrySet()) {

            AdvancementHolder advancement = entry.getKey();
            AdvancementProgress advancementProgress = entry.getValue();

            if (advancementProgress.isDone()) {
                chaosEnchants$processed.add(advancement);
            }
        }

        chaosEnchants$loading = false;
    }

    /*
     * Called whenever Minecraft awards a criterion.
     */
    @Inject(
            method = "award",
            at = @At("RETURN")
    )
    private void chaosEnchants$onAward(
            AdvancementHolder advancement,
            String criterionKey,
            CallbackInfoReturnable<Boolean> cir
    ) {
        /*
         * Never react to anything happening while Minecraft is
         * loading/reloading advancement data.
         */
        if (chaosEnchants$loading) {
            return;
        }

        /*
         * The criterion wasn't newly awarded.
         */
        if (!cir.getReturnValue()) {
            return;
        }

        /*
         * Recipe advancements are used for recipe unlocking.
         *
         * They are explicitly NOT supposed to increase the multiplier.
         *
         * This works for vanilla and modded recipe advancements because
         * we check the path rather than just the "minecraft" namespace.
         */
        if (advancement.id().getPath().startsWith("recipes/")) {
            return;
        }

        AdvancementProgress advancementProgress =
                progress.get(advancement);

        /*
         * The advancement doesn't have progress data.
         */
        if (advancementProgress == null) {
            return;
        }

        /*
         * The criterion may have been awarded, but the entire
         * advancement isn't complete yet.
         */
        if (!advancementProgress.isDone()) {
            return;
        }

        /*
         * If we've already counted this advancement, don't count it
         * again.
         */
        if (!chaosEnchants$processed.add(advancement)) {
            return;
        }

        /*
         * This is a genuine newly completed advancement.
         */
        AdvancementChaosHandler.onPlayerEarnAdvancement(player);
    }

    /*
     * Revoking an advancement must NEVER increase the multiplier.
     *
     * We only remove it from our processed set so that if the player
     * later earns that advancement again, the new completion can count.
     */
    @Inject(
            method = "revoke",
            at = @At("RETURN")
    )
    private void chaosEnchants$onRevoke(
            AdvancementHolder advancement,
            String criterionKey,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (!cir.getReturnValue()) {
            return;
        }

        chaosEnchants$processed.remove(advancement);
    }
}
