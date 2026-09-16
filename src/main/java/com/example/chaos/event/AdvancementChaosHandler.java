package com.example.chaos.event;

import com.example.chaos.state.GlobalMultiplierState;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import java.util.Optional;

public class AdvancementChaosHandler {
    public static void onPlayerEarnAdvancement(ServerPlayer player) {
        long currentMultiplier = GlobalMultiplierState.getMultiplier(player);
        long nextMultiplier = currentMultiplier * 2;
        GlobalMultiplierState.setMultiplier(player, nextMultiplier);
        GlobalMultiplierState.syncToClient(player, nextMultiplier);

        Registry<Enchantment> registry =
                player.registryAccess().registryOrThrow(Registries.ENCHANTMENT);

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack itemStack = player.getInventory().getItem(i);
            if (itemStack.isEmpty()) continue;

            var enchantmentMap =
                    EnchantmentHelper.getEnchantmentsForCrafting(itemStack);

            if (enchantmentMap.isEmpty()) {
                Optional<Holder.Reference<Enchantment>> randomEnchant =
                        registry.getRandom(player.getRandom());

                if (randomEnchant.isPresent()) {
                    itemStack.enchant(randomEnchant.get(), (int) nextMultiplier);
                }
            } else {
                EnchantmentHelper.updateEnchantments(itemStack, mutableComponents -> {
                    for (var entry : enchantmentMap.entrySet()) {
                        long scaledLevel = (long) entry.getValue() * 2;

                        mutableComponents.set(
                                entry.getKey(),
                                (int) Math.min(scaledLevel, Integer.MAX_VALUE)
                        );
                    }
                });
            }
        }

        player.sendSystemMessage(
                Component.literal(
                        "§6§lMULTIPLIER UP! §eAll enchants multiplied to §b"
                                + nextMultiplier + "x!"
                )
        );
    }
}
