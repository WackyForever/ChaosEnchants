package com.example.chaos.mixin;

import com.example.chaos.EnchantmentLevelHelper;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(Player.class)
public class SweepingEdgeRangeMixin {

    @ModifyArgs(
            method = "sweepAttack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/phys/AABB;inflate(DDD)Lnet/minecraft/world/phys/AABB;"
            )
    )
    private void chaosenchants$expandSweepRadius(Args args) {
        Player player = (Player) (Object) this;

        Registry<Enchantment> enchantmentRegistry =
                player.level().registryAccess()
                        .registryOrThrow(Registries.ENCHANTMENT);

        int level = EnchantmentLevelHelper.getLevel(
                Enchantments.SWEEPING_EDGE,
                player.getMainHandItem(),
                enchantmentRegistry
        );

        if (level <= 0) {
            return;
        }

        double originalHorizontal = args.get(0);
        double originalVertical = args.get(1);

        double horizontal = Math.min(
                12.0,
                originalHorizontal + (double) level * 0.5
        );

        double vertical = Math.min(
                6.0,
                originalVertical + (double) level * 0.25
        );

        args.set(0, horizontal);
        args.set(1, vertical);
        args.set(2, horizontal);
    }
}
