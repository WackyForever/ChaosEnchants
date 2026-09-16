package com.example.chaos.state;

import com.example.chaos.ChaosMod;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GlobalMultiplierState extends SavedData {

    private static final String DATA_NAME = "chaosenchants_multiplier";
    private static final String MULTIPLIERS_KEY = "Multipliers";

    private final Map<UUID, Long> multipliers = new HashMap<>();

    public GlobalMultiplierState() {
    }

    private static GlobalMultiplierState load(
            CompoundTag tag,
            HolderLookup.Provider registries
    ) {
        GlobalMultiplierState state = new GlobalMultiplierState();

        CompoundTag multipliersTag = tag.getCompound(MULTIPLIERS_KEY);

        for (String uuidString : multipliersTag.getAllKeys()) {
            try {
                UUID uuid = UUID.fromString(uuidString);
                long multiplier = multipliersTag.getLong(uuidString);
                state.multipliers.put(uuid, multiplier);
            } catch (IllegalArgumentException ignored) {
                // Ignore invalid UUIDs in the saved data.
            }
        }

        return state;
    }

    @Override
    public CompoundTag save(
            CompoundTag tag,
            HolderLookup.Provider registries
    ) {
        CompoundTag multipliersTag = new CompoundTag();

        for (Map.Entry<UUID, Long> entry : multipliers.entrySet()) {
            multipliersTag.putLong(
                    entry.getKey().toString(),
                    entry.getValue()
            );
        }

        tag.put(MULTIPLIERS_KEY, multipliersTag);

        return tag;
    }

    private static GlobalMultiplierState getState(ServerPlayer player) {
        MinecraftServer server = player.getServer();

        if (server == null) {
            throw new IllegalStateException("Player is not connected to a server.");
        }

        ServerLevel overworld = server.overworld();

        return overworld.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(
                        GlobalMultiplierState::new,
                        GlobalMultiplierState::load,
                        null
                ),
                DATA_NAME
        );
    }

    public static long getMultiplier(ServerPlayer player) {
        GlobalMultiplierState state = getState(player);

        return state.multipliers.getOrDefault(
                player.getUUID(),
                1L
        );
    }

    public static void setMultiplier(ServerPlayer player, long value) {
        GlobalMultiplierState state = getState(player);

        state.multipliers.put(
                player.getUUID(),
                value
        );

        state.setDirty();
    }

    public static void syncToClient(ServerPlayer player, long value) {
        ServerPlayNetworking.send(
                player,
                new ChaosMod.MultiplierSyncPayload(value)
        );
    }
}
