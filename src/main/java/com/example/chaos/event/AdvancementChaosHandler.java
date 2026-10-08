
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
import java.util.Optional;

public class AdvancementChaosHandler {

    public static void onPlayerEarnAdvancement(ServerPlayer player) {

        // Get and double the current multiplier.
        long currentMultiplier =
                GlobalMultiplierState.getMultiplier(player);

        long nextMultiplier = currentMultiplier * 2L;

        // Prevent overflow.
        if (nextMultiplier < currentMultiplier || nextMultiplier < 0) {
            nextMultiplier = Long.MAX_VALUE;
        }

        // Save and sync the multiplier.
        GlobalMultiplierState.setMultiplier(player, nextMultiplier);
        GlobalMultiplierState.syncToClient(player, nextMultiplier);

        Registry<Enchantment> registry =
                player.registryAccess()
                        .registryOrThrow(Registries.ENCHANTMENT);

        int enchantmentLevel =
                (int) Math.min(nextMultiplier, Integer.MAX_VALUE);

        // Read the compatibility toggle.
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

            if (itemStack.isEmpty()) {
                continue;
            }

            /*
             * ON:
             * Any enchantment can be selected, as before.
             *
             * OFF:
             * Normally enchantable items only receive enchantments
             * compatible with their item type and existing enchants.
             *
             * Normally non-enchantable items, such as dirt, retain
             * the original unrestricted random-enchantment behavior.
             */

            boolean normallyEnchantable =
                    EnchantmentHelper.canStoreEnchantments(itemStack);

            List<Holder.Reference<Enchantment>> candidates =
                    new ArrayList<>();

            for (Holder.Reference<Enchantment> candidate :
                    registry.holders().toList()) {

                if (allowIncompatible || !normallyEnchantable) {
                    candidates.add(candidate);
                    continue;
                }

                // Check whether this enchantment supports the item.
                boolean supportsItem =
                        candidate.value().canEnchant(itemStack);

                if (!supportsItem) {
                    continue;
                }

                // Reject conflicts with existing enchantments.
                boolean conflicts = false;

                for (Holder<Enchantment> existing :
                        EnchantmentHelper.getEnchantmentsForCrafting(itemStack)
                                .keySet()) {

                    if (!existing.equals(candidate)
                            && Enchantment.areIncompatible(
                                    candidate,
                                    existing)) {
                        conflicts = true;
                        break;
                    }
                }

                if (!conflicts) {
                    candidates.add(candidate);
                }
            }

            if (candidates.isEmpty()) {
                continue;
            }

            // Choose a random eligible enchantment.
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
