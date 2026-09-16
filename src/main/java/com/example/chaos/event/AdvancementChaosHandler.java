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

        // Get the player's current multiplier.
        long currentMultiplier =
                GlobalMultiplierState.getMultiplier(player);

        // Double the multiplier.
        long nextMultiplier = currentMultiplier * 2L;

        // Prevent overflow from turning the multiplier negative.
        if (nextMultiplier < currentMultiplier) {
            nextMultiplier = Long.MAX_VALUE;
        }

        // Save the new multiplier.
        GlobalMultiplierState.setMultiplier(
                player,
                nextMultiplier
        );

        // Update the client's HUD.
        GlobalMultiplierState.syncToClient(
                player,
                nextMultiplier
        );

        // Get Minecraft's enchantment registry.
        Registry<Enchantment> registry =
                player.registryAccess()
                        .registryOrThrow(Registries.ENCHANTMENT);

        // Convert the multiplier to the maximum level
        // Minecraft's enchantment component can store.
        int enchantmentLevel =
                (int) Math.min(
                        nextMultiplier,
                        Integer.MAX_VALUE
                );

        // Process every inventory slot.
        for (int i = 0;
             i < player.getInventory().getContainerSize();
             i++) {

            ItemStack itemStack =
                    player.getInventory().getItem(i);

            // Ignore empty slots.
            if (itemStack.isEmpty()) {
                continue;
            }

            // Pick a completely random enchantment.
            Optional<Holder.Reference<Enchantment>> randomEnchant =
                    registry.getRandom(player.getRandom());

            if (randomEnchant.isEmpty()) {
                continue;
            }

            Holder<Enchantment> enchantment =
                    randomEnchant.get();

            /*
             * Directly modify the enchantment component.
             *
             * This intentionally bypasses normal:
             *
             * - item compatibility
             * - enchantment compatibility
             * - normal enchanting-table restrictions
             * - normal anvil restrictions
             *
             * Therefore things such as:
             *
             * Sharpness + Smite
             * Fortune + Silk Touch
             * Protection + Fire Protection
             * Mending + Infinity
             *
             * can coexist.
             *
             * It also works on normally unenchantable items
             * such as dirt, food, sticks, blocks, etc.
             */
            EnchantmentHelper.updateEnchantments(
                    itemStack,
                    mutableEnchantments -> {
                        mutableEnchantments.set(
                                enchantment,
                                enchantmentLevel
                        );
                    }
            );
        }

        // Tell the player what happened.
        player.sendSystemMessage(
                Component.literal(
                        "§6§lMULTIPLIER UP! §eAll items received a random enchantment at §b"
                                + nextMultiplier
                                + "x§e!"
                )
        );
    }
}
