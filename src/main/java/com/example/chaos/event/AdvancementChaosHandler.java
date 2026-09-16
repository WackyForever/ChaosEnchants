package com.example.chaos.event;

import com.example.chaos.state.GlobalMultiplierState;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.Optional;

public class AdvancementChaosHandler {

    public static void onPlayerEarnAdvancement(ServerPlayer player) {

        // Get current multiplier
        long currentMultiplier =
                GlobalMultiplierState.getMultiplier(player);

        // Double the multiplier
        long nextMultiplier = currentMultiplier * 2;

        // Save the new multiplier
        GlobalMultiplierState.setMultiplier(
                player,
                nextMultiplier
        );

        // Update HUD
        GlobalMultiplierState.syncToClient(
                player,
                nextMultiplier
        );

        // Get the enchantment registry
        Registry<Enchantment> registry =
                player.registryAccess()
                        .registryOrThrow(Registries.ENCHANTMENT);

        // Go through every item in the player's inventory
        for (int i = 0;
             i < player.getInventory().getContainerSize();
             i++) {

            ItemStack itemStack =
                    player.getInventory().getItem(i);

            // Skip empty slots
            if (itemStack.isEmpty()) {
                continue;
            }

            // Pick a completely random enchantment
            Optional<Holder.Reference<Enchantment>> randomEnchant =
                    registry.getRandom(player.getRandom());

            if (randomEnchant.isEmpty()) {
                continue;
            }

            Holder<Enchantment> enchantment =
                    randomEnchant.get();

            // Add the enchantment DIRECTLY to the item's
            // enchantment component.
            //
            // This bypasses the normal item/enchantment
            // compatibility checks.
            EnchantmentHelper.updateEnchantments(
                    itemStack,
                    mutableEnchantments -> {

                        mutableEnchantments.set(
                                enchantment,
                                (int) Math.min(
                                        nextMultiplier,
                                        Integer.MAX_VALUE
                                )
                        );
                    }
            );
        }

        // Tell the player what happened
        player.sendSystemMessage(
                Component.literal(
                        "§6§lMULTIPLIER UP! §eAll items received a random enchantment at §b"
                                + nextMultiplier
                                + "x§e!"
                )
        );
    }
}
