package com.ok2603.glt.client;

import com.ok2603.glt.entity.KettenkradEntity;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.EntityHitResult;

/**
 * When you look at a stopped Kettenkrad, shows what the mouse buttons do under the crosshair.
 * While riding one, shows its fuel and condition in the top-left corner.
 */
public class KettenkradPromptHud implements LayeredDraw.Layer {
    @Override
    public void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui || mc.screen != null) {
            return;
        }
        if (mc.player.getVehicle() instanceof KettenkradEntity riding) {
            graphics.drawString(mc.font, status(riding), 6, 6, statusColor(riding));
            if (riding.getFuel() <= 0 && riding.getControllingPassenger() == mc.player) {
                graphics.drawString(mc.font, Component.translatable("hud.glt.kettenkrad.out_of_fuel"), 6, 17, 0xC8C8C8);
            }
            return;
        }
        if (mc.player.isPassenger()) {
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
        graphics.drawCenteredString(mc.font, Component.translatable("hud.glt.kettenkrad.storage"), x, y, 0xFFFFFF);
        y += 11;
        graphics.drawCenteredString(mc.font, Component.translatable("hud.glt.kettenkrad.pick_up"), x, y, 0x9A9A9A);
        y += 11;
        graphics.drawCenteredString(mc.font, status(kettenkrad), x, y, statusColor(kettenkrad));
    }

    private static Component status(KettenkradEntity kettenkrad) {
        int fuel = (int) Math.ceil(kettenkrad.getFuel() * 100.0 / KettenkradEntity.MAX_FUEL);
        return Component.translatable("hud.glt.kettenkrad.status", fuel, kettenkrad.getCondition());
    }

    /** White normally, orange when worn or low on fuel, red when broken or empty. */
    private static int statusColor(KettenkradEntity kettenkrad) {
        if (kettenkrad.isBroken() || kettenkrad.getFuel() <= 0) {
            return 0xFF6B5A;
        }
        if (kettenkrad.getCondition() < KettenkradEntity.POOR_CONDITION
                || kettenkrad.getFuel() < KettenkradEntity.FUEL_PER_CANISTER / 2) {
            return 0xFFB347;
        }
        return 0xE8E8E8;
    }
}
