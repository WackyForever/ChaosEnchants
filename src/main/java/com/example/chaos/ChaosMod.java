
package com.example.chaos;

import com.example.chaos.state.GlobalMultiplierState;
import com.example.chaos.state.EnchantmentCompatibilityState;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.commands.Commands;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;

public class ChaosMod implements ModInitializer {

    public static final String MOD_ID = "chaosenchants";

    public record MultiplierSyncPayload(long multiplier)
            implements CustomPacketPayload {

        public static final CustomPacketPayload.Type<MultiplierSyncPayload> ID =
                new CustomPacketPayload.Type<>(
                        ResourceLocation.fromNamespaceAndPath(MOD_ID, "sync")
                );

        public static final StreamCodec<RegistryFriendlyByteBuf, MultiplierSyncPayload> CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.VAR_LONG,
                        MultiplierSyncPayload::multiplier,
                        MultiplierSyncPayload::new
                );

        @Override
        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }

    @Override
    public void onInitialize() {

        // Register multiplier synchronization
        PayloadTypeRegistry.playS2C().register(
                MultiplierSyncPayload.ID,
                MultiplierSyncPayload.CODEC
        );

        // Sync the current multiplier when a player joins
        ServerPlayConnectionEvents.JOIN.register(
                (handler, sender, server) -> {
                    long current =
                            GlobalMultiplierState.getMultiplier(
                                    handler.getPlayer()
                            );

                    ServerPlayNetworking.send(
                            handler.getPlayer(),
                            new MultiplierSyncPayload(current)
                    );
                }
        );

        // Register compatibility commands
        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) -> {

                    dispatcher.register(
                            Commands.literal("chaosenchants")
                                    .then(
                                            Commands.literal("compatibility")

                                                    // Show current setting
                                                    .executes(context -> {
                                                        boolean enabled =
                                                                EnchantmentCompatibilityState
                                                                        .get(context.getSource().getServer())
                                                                        .allowsIncompatibleEnchants();

                                                        context.getSource().sendSuccess(
                                                                () -> Component.literal(
                                                                        "Incompatible enchantments: "
                                                                                + (enabled ? "ON" : "OFF")
                                                                ),
                                                                false
                                                        );

                                                        return 1;
                                                    })

                                                    // Enable incompatible enchantments
                                                    .then(
                                                            Commands.literal("on")
                                                                    .requires(source ->
                                                                            source.hasPermission(2))
                                                                    .executes(context -> {
                                                                        EnchantmentCompatibilityState
                                                                                .get(context.getSource().getServer())
                                                                                .setAllowIncompatibleEnchants(true);

                                                                        context.getSource().sendSuccess(
                                                                                () -> Component.literal(
                                                                                        "Incompatible enchantments are now ON."
                                                                                ),
                                                                                true
                                                                        );

                                                                        return 1;
                                                                    })
                                                    )

                                                    // Disable incompatible enchantments
                                                    .then(
                                                            Commands.literal("off")
                                                                    .requires(source ->
                                                                            source.hasPermission(2))
                                                                    .executes(context -> {
                                                                        EnchantmentCompatibilityState
                                                                                .get(context.getSource().getServer())
                                                                                .setAllowIncompatibleEnchants(false);

                                                                        context.getSource().sendSuccess(
                                                                                () -> Component.literal(
                                                                                        "Incompatible enchantments are now OFF."
                                                                                ),
                                                                                true
                                                                        );

                                                                        return 1;
                                                                    })
                                                    )
                                    )
                    );
                }
        );
    }
}
