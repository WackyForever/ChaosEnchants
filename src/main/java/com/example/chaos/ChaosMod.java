package com.example.chaos;

import com.example.chaos.event.AdvancementChaosHandler;
import com.example.chaos.state.GlobalMultiplierState;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public class ChaosMod implements ModInitializer {

    public static final String MOD_ID = "chaosenchants";

    /**
     * Network payload used to synchronize the multiplier with the client HUD.
     */
    public record MultiplierSyncPayload(long multiplier) implements CustomPayload {

        public static final CustomPayload.Id<MultiplierSyncPayload> ID =
                new CustomPayload.Id<>(
                        Identifier.of(MOD_ID, "sync")
                );

        public static final PacketCodec<RegistryByteBuf, MultiplierSyncPayload> CODEC =
                PacketCodec.tuple(
                        PacketCodecs.LONG,
                        MultiplierSyncPayload::multiplier,
                        MultiplierSyncPayload::new
                );

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    @Override
    public void onInitialize() {

        // Register the S2C payload.
        PayloadTypeRegistry.playS2C().register(
                MultiplierSyncPayload.ID,
                MultiplierSyncPayload.CODEC
        );

        // Handle completed advancements.
        ServerPlayerEvents.AFTER_ADVANCEMENT_EARNED.register(
                (player, advancement) -> {
                    AdvancementChaosHandler.onPlayerEarnAdvancement(player);
                }
        );

        // Synchronize the saved multiplier whenever a player joins.
        ServerPlayConnectionEvents.JOIN.register(
                (handler, sender, server) -> {
                    long current =
                            GlobalMultiplierState.getMultiplier(handler.player);

                    ServerPlayNetworking.send(
                            handler.player,
                            new MultiplierSyncPayload(current)
                    );
                }
        );
    }
}
