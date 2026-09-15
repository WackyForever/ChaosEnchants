package com.example.chaos.state;

import com.example.chaos.ChaosMod;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;

public class GlobalMultiplierState {
    private static final String NBT_KEY = "VideoChaosMultiplier";

    public static long getMultiplier(ServerPlayer player) {
        CompoundTag persistentData = player.getPersistentData();
        if (!persistentData.contains(NBT_KEY)) {
            persistentData.putLong(NBT_KEY, 1L);
        }
        return persistentData.getLong(NBT_KEY);
    }

    public static void setMultiplier(ServerPlayer player, long value) {
        player.getPersistentData().putLong(NBT_KEY, value);
    }

    public static void syncToClient(ServerPlayer player, long value) {
        ServerPlayNetworking.send(player, new ChaosMod.MultiplierSyncPayload(value));
    }
}
