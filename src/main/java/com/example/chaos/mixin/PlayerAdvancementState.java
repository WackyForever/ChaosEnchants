package com.example.chaos.mixin;

import net.minecraft.advancements.AdvancementHolder;

import java.util.HashMap;
import java.util.Map;

public final class PlayerAdvancementState {

    private static final Map<AdvancementHolder, Boolean> STATES =
            new HashMap<>();

    private PlayerAdvancementState() {
    }

    public static void setWasComplete(
            AdvancementHolder advancement,
            boolean complete
    ) {
        STATES.put(advancement, complete);
    }

    public static boolean wasComplete(
            AdvancementHolder advancement
    ) {
        return STATES.getOrDefault(advancement, false);
    }

    public static void remove(
            AdvancementHolder advancement
    ) {
        STATES.remove(advancement);
    }
}
