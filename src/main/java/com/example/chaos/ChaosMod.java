package com.example.chaos;

import com.example.chaos.event.AdvancementChaosHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public class ChaosMod implements ModInitializer {
    public static final String MOD_ID = "chaosenchants";

    // Define Network Packet payload for syncing the client HUD screen seamlessly
    public record MultiplierSyncPayload(long multiplier) implements CustomPacketPayload {
        public static final Id<MultiplierSyncPayload> ID = new Id<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "sync"));
        public static final PacketCodec<RegistryByteBuf, MultiplierSyncPayload> CODEC = CustomPacketPayload.codec(
                (payload, buf) -> buf.writeLong(payload.multiplier()),
                buf -> new MultiplierSyncPayload(buf.readLong())
        );
        @Override public Id<? extends CustomPacketPayload> type() { return ID; }
    }

    @Override
    public void onInitialize() {
        // Register the networking packet channel
        PayloadTypeRegistry.playS2C().register(MultiplierSyncPayload.ID, MultiplierSyncPayload.CODEC);

        // Hook into when advancements complete
        ServerPlayerEvents.AFTER_ADVANCEMENT_EARNED.register((player, advancement) -> {
            AdvancementChaosHandler.onPlayerEarnAdvancement(player);
        });

        // Make sure returning or connecting players sync their saved multiplier values instantly
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            long current = com.example.chaos.state.GlobalMultiplierState.getMultiplier(handler.getPlayer());
            ServerPlayNetworking.send(handler.getPlayer(), new MultiplierSyncPayload(current));
        });
    }
}
