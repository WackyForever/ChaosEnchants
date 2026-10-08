
package com.example.chaos.event;

import com.example.chaos.state.GlobalMultiplierState;
import com.example.chaos.state.EnchantmentCompatibilityState;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.ArrayList;
import java.util.List;

public class AdvancementChaosHandler {

    public static void onPlayerEarnAdvancement(ServerPlayer player) {

        // Get the player's current multiplier.
        long currentMultiplier =
                GlobalMultiplierState.getMultiplier(player);

        // Double the multiplier.
        long nextMultiplier = currentMultiplier * 2L;

        // Prevent overflow.
        if (nextMultiplier < currentMultiplier || nextMultiplier < 0) {
            nextMultiplier = Long.MAX_VALUE;
        }

        // Save and sync the multiplier.
        GlobalMultiplierState.setMultiplier(player, nextMultiplier);
        GlobalMultiplierState.syncToClient(player, nextMultiplier);

        // Get Minecraft's enchantment registry.
        Registry<Enchantment> registry =
                player.registryAccess()
                        .registryOrThrow(Registries.ENCHANTMENT);

        // Convert the multiplier to an enchantment level.
        int enchantmentLevel =
                (int) Math.min(nextMultiplier, Integer.MAX_VALUE);

        // Read the compatibility setting.
        boolean allowIncompatible =
                EnchantmentCompatibilityState
                        .get(player.getServer())
                        .allowsIncompatibleEnchants();

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

            // Determine whether this item can normally store enchantments.
            boolean normallyEnchantable =
                    EnchantmentHelper.canStoreEnchantments(itemStack);

            List<Holder.Reference<Enchantment>> candidates =
                    new ArrayList<>();

            // Find eligible enchantments.
            for (Holder.Reference<Enchantment> candidate :
                    registry.holders().toList()) {

                /*
                 * If compatibility is ON, allow any enchantment.
                 *
                 * If the item is normally non-enchantable (such as dirt),
                 * preserve the original unrestricted behavior in either mode.
                 */
                if (allowIncompatible || !normallyEnchantable) {
                    candidates.add(candidate);
                    continue;
                }

                // When OFF, check whether the enchantment supports this item.
                if (!candidate.value().canEnchant(itemStack)) {
                    continue;
                }

                // Check for conflicts with existing enchantments.
                boolean conflicts = false;

                for (Holder<Enchantment> existing :
                        EnchantmentHelper.getEnchantmentsForCrafting(itemStack)
                                .keySet()) {

                    if (!existing.equals(candidate)
                            && !Enchantment.areCompatible(
                                    existing,
                                    candidate)) {
                        conflicts = true;
                        break;
                    }
                }

                if (!conflicts) {
                    candidates.add(candidate);
                }
            }

            // No compatible enchantments available.
            if (candidates.isEmpty()) {
                continue;
            }

            // Select a random eligible enchantment.
            Holder<Enchantment> enchantment =
                    candidates.get(
                            player.getRandom().nextInt(candidates.size())
                    );

            // Apply the enchantment at the current multiplier level.
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

        // Notify the player.
        player.sendSystemMessage(
                Component.literal(
                        "§6§lMULTIPLIER UP! §eAll eligible items received a random enchantment at §b"
                                + nextMultiplier
                                + "x§e! Compatibility: "
                                + (allowIncompatible ? "ON" : "OFF")
                )
        );
    }
}
