
package com.example.chaos.state;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.HolderLookup;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

public class EnchantmentCompatibilityState extends SavedData {

    private static final String DATA_NAME = "chaosenchants_compatibility";
    private static final String COMPATIBILITY_KEY = "AllowIncompatibleEnchants";

    private boolean allowIncompatibleEnchants = true;

    private static final SavedData.Factory<EnchantmentCompatibilityState> FACTORY =
            new SavedData.Factory<>(
                    EnchantmentCompatibilityState::new,
                    EnchantmentCompatibilityState::load,
                    DataFixTypes.LEVEL
            );

    public static EnchantmentCompatibilityState get(
            net.minecraft.server.MinecraftServer server) {
        return server.overworld()
                .getDataStorage()
                .computeIfAbsent(FACTORY, DATA_NAME);
    }

    public boolean allowsIncompatibleEnchants() {
        return allowIncompatibleEnchants;
    }

    public void setAllowIncompatibleEnchants(boolean enabled) {
        if (this.allowIncompatibleEnchants != enabled) {
            this.allowIncompatibleEnchants = enabled;
            setDirty();
        }
    }

    private static EnchantmentCompatibilityState load(
            CompoundTag tag,
            HolderLookup.Provider registries) {

        EnchantmentCompatibilityState state =
                new EnchantmentCompatibilityState();

        state.allowIncompatibleEnchants =
                tag.getBoolean(COMPATIBILITY_KEY);

        return state;
    }

    @Override
    public CompoundTag save(
            CompoundTag tag,
            HolderLookup.Provider registries) {

        tag.putBoolean(COMPATIBILITY_KEY, allowIncompatibleEnchants);
        return tag;
    }
}
