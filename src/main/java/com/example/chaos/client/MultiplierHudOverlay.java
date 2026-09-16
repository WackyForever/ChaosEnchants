package com.example.chaos.client;

import com.example.chaos.ChaosMod;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.DeltaTracker;

public class MultiplierHudOverlay implements ClientModInitializer, HudRenderCallback {
    private static long clientMultiplier = 1;

    @Override
    public void onInitializeClient() {
        // Register HUD graphics drawer
        HudRenderCallback.EVENT.register(this);

        // Receive the update from the server network and write to client instantly
        ClientPlayNetworking.registerGlobalReceiver(ChaosMod.MultiplierSyncPayload.ID, (payload, context) -> {
            context.client().execute(() -> clientMultiplier = payload.multiplier());
        });
    }

    @Override
    public void onHudRender(GuiGraphics drawContext, DeltaTracker tickCounter) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.options.hideGui) return;

        String text = "🔥 Multiplier: x" + String.format("%,d", clientMultiplier);
        drawContext.drawString(client.font, text, 12, 12, 0xFFAA00, true);
    }
}
