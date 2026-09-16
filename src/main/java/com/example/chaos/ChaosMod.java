package com.example.chaos;

import com.example.chaos.state.GlobalMultiplierState;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

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

        PayloadTypeRegistry.playS2C().register(
                MultiplierSyncPayload.ID,
                MultiplierSyncPayload.CODEC
        );


        ServerPlayConnectionEvents.JOIN.register(
                (handler, sender, server) -> {
                    long current =
                            GlobalMultiplierState.getMultiplier(handler.getPlayer());

                    ServerPlayNetworking.send(
                            handler.getPlayer(),
                            new MultiplierSyncPayload(current)
                    );
                }
        );
    }
}
