package com.example.chaos.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.item.EnderpearlItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnderpearlItem.class)
public class EnderPearlMultishotMixin {

    @Inject(
        method = "use",
        at = @At("HEAD")
    )
    private void chaosenchants$fireExtraPearls(
            Level level,
            Player player,
            InteractionHand hand,
            CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir
    ) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        ItemStack stack = player.getItemInHand(hand);

        if (stack.isEmpty()
                || player.getCooldowns().isOnCooldown(stack.getItem())) {
            return;
        }

        int multishotLevel = EnchantmentHelper.getItemEnchantmentLevel(
                Enchantments.MULTISHOT,
                stack
        );

        if (multishotLevel <= 0) {
            return;
        }

        // Multishot I = 3 total pearls, II = 4, III = 5, and so on.
        // No artificial maximum is applied.
        long totalPearls = (long) multishotLevel + 2L;
        long extraPearls = totalPearls - 1L;

        for (long i = 0; i < extraPearls; i++) {
            float yawOffset = (float) (
                    (i - (extraPearls - 1) / 2.0) * 10.0
            );

            ThrownEnderpearl pearl =
                    new ThrownEnderpearl(serverLevel, player);

            pearl.shootFromRotation(
                    player,
                    player.getXRot(),
                    player.getYRot() + yawOffset,
                    0.0F,
                    1.5F,
                    1.0F
            );

            serverLevel.addFreshEntity(pearl);
        }
    }
}
