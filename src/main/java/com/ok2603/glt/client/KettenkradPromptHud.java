package com.ok2603.glt.client;

import com.ok2603.glt.entity.KettenkradEntity;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.EntityHitResult;

/** When you look at a stopped Kettenkrad, shows what the mouse buttons do under the crosshair. */
public class KettenkradPromptHud implements LayeredDraw.Layer {
    @Override
    public void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui || mc.screen != null || mc.player.isPassenger()) {
            return;
        }
        if (!(mc.hitResult instanceof EntityHitResult hit) || !(hit.getEntity() instanceof KettenkradEntity kettenkrad)
                || !kettenkrad.isStopped()) {
            return;
        }

        int x = graphics.guiWidth() / 2;
        int y = graphics.guiHeight() / 2 + 12;
        if (kettenkrad.getPassengers().size() < 2) {
            graphics.drawCenteredString(mc.font, Component.translatable("hud.glt.kettenkrad.enter"), x, y, 0xFFFFFF);
            y += 11;
        }
        graphics.drawCenteredString(mc.font, Component.translatable("hud.glt.kettenkrad.camp"), x, y, 0xFFFFFF);
        y += 11;
        graphics.drawCenteredString(mc.font, Component.translatable("hud.glt.kettenkrad.pick_up"), x, y, 0x9A9A9A);
    }
}
