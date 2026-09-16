package com.example.chaos.state;

import com.example.chaos.ChaosMod;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GlobalMultiplierState {

    private static final Map<UUID, Long> MULTIPLIERS = new HashMap<>();

    public static long getMultiplier(ServerPlayer player) {
        return MULTIPLIERS.getOrDefault(player.getUUID(), 1L);
    }

    public static void setMultiplier(ServerPlayer player, long value) {
        MULTIPLIERS.put(player.getUUID(), value);
    }

    public static void syncToClient(ServerPlayer player, long value) {
        ServerPlayNetworking.send(
                player,
                new ChaosMod.MultiplierSyncPayload(value)
        );
    }
}
