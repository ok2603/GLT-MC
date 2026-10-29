package com.ok2603.glt.client;

import com.ok2603.glt.cold.BodyHeat;
import com.ok2603.glt.network.ModNetwork;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.util.Mth;

/**
 * A small thermometer to the left of the hotbar.
 * Full and orange = warm. Low and blue = freezing. It blinks when you are almost frozen.
 */
public class BodyHeatHud implements LayeredDraw.Layer {
    private static final int WIDTH = 6;
    private static final int HEIGHT = 20;

    @Override
    public void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui || mc.player.isCreative() || mc.player.isSpectator()) {
            return;
        }

        float heat = Mth.clamp(ModNetwork.clientBodyHeat / BodyHeat.MAX, 0, 1);
        // Left of the hotbar and the off-hand slot.
        int x = graphics.guiWidth() / 2 - 91 - 29 - WIDTH - 6;
        int y = graphics.guiHeight() - HEIGHT - 2;

        // Blink when nearly frozen.
        if (heat < 0.1f && (mc.player.tickCount / 5) % 2 == 0) {
            return;
        }

        // Frame and empty background.
        graphics.fill(x - 1, y - 1, x + WIDTH + 1, y + HEIGHT + 1, 0xFF1A1A1A);
        graphics.fill(x, y, x + WIDTH, y + HEIGHT, 0xFF3A4652);

        // Fill from the bottom, blending from icy blue to warm orange.
        int filled = Math.round(heat * HEIGHT);
        int red = (int) Mth.lerp(heat, 0x6C, 0xF0);
        int green = (int) Mth.lerp(heat, 0xB8, 0x8C);
        int blue = (int) Mth.lerp(heat, 0xF0, 0x3C);
        int color = 0xFF000000 | (red << 16) | (green << 8) | blue;
        graphics.fill(x, y + HEIGHT - filled, x + WIDTH, y + HEIGHT, color);

        // A bulb at the bottom, like a thermometer.
        graphics.fill(x - 1, y + HEIGHT - 2, x + WIDTH + 1, y + HEIGHT + 2, color);
    }
}
