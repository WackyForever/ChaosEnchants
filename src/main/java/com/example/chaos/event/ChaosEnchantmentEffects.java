
package com.example.chaos.event;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import net.minecraft.world.item.enchantment.Enchantments;

public final class ChaosEnchantmentEffects {

    private static final ThreadLocal<Boolean> AREA_MINING =
            ThreadLocal.withInitial(() -> false);

    private ChaosEnchantmentEffects() {
    }

    public static void register() {
        PlayerBlockBreakEvents.AFTER.register(
                (world, player, pos, state, blockEntity) -> {

                    if (!(player instanceof ServerPlayer serverPlayer)) {
                        return;
                    }

                    // Prevent extra block breaks from recursively triggering
                    // another area-mining operation.
                    if (AREA_MINING.get()) {
                        return;
                    }

                    int level = EnchantmentHelper.getItemEnchantmentLevel(
                            Enchantments.EFFICIENCY,
                            player.getMainHandItem()
                    );

                    if (level < 100) {
                        return;
                    }

                    int size;
                    if (level >= 1000) {
                        size = 7;
                    } else if (level >= 500) {
                        size = 5;
                    } else {
                        size = 3;
                    }

                    int radius = size / 2;
                    Direction facing = player.getDirection();

                    AREA_MINING.set(true);

                    try {
                        for (int a = -radius; a <= radius; a++) {
                            for (int b = -radius; b <= radius; b++) {

                                if (a == 0 && b == 0) {
                                    continue;
                                }

                                BlockPos target;

                                // Mine a plane perpendicular to the
                                // player's horizontal facing direction.
                                if (facing.getAxis() == Direction.Axis.Z) {
                                    target = pos.offset(a, b, 0);
                                } else {
                                    target = pos.offset(0, b, a);
                                }

                                var targetState =
                                        world.getBlockState(target);

                                if (targetState.isAir()) {
                                    continue;
                                }

                                // Do not automatically destroy containers,
                                // machines, or other block entities.
                                if (targetState.hasBlockEntity()) {
                                    continue;
                                }

                                // Skip unbreakable blocks.
                                if (targetState.getDestroySpeed(
                                        world, target
                                ) < 0.0F) {
                                    continue;
                                }

                                serverPlayer.gameMode.destroyBlock(target);
                            }
                        }
                    } finally {
                        AREA_MINING.remove();
                    }
                }
        );
    }
}
