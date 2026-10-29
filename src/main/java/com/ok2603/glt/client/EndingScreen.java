package com.ok2603.glt.client;

import com.ok2603.glt.registry.ModSounds;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

/**
 * The ending: a black screen where the last lines of the trip fade in one by one,
 * then a few credits. Nuko's little tune plays. Press Esc to go back to the game.
 */
public class EndingScreen extends Screen {
    private static final int STORY_LINES = 7;
    private static final int CREDIT_LINES = 4;
    private static final int TICKS_PER_LINE = 50;
    private static final int FADE_TICKS = 30;
    private int ticks;

    public EndingScreen() {
        super(Component.translatable("screen.glt.ending.title"));
    }

    @Override
    protected void init() {
        if (ticks == 0 && minecraft != null) {
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(ModSounds.NUKO_RADIO.get(), 1.0f, 0.6f));
        }
    }

    @Override
    public void tick() {
        ticks++;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, width, height, 0xFF000000);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        float time = ticks + partialTick;
        int lineHeight = 14;
        int totalLines = STORY_LINES + 1 + CREDIT_LINES;
        int top = height / 2 - totalLines * lineHeight / 2;
        for (int i = 0; i < totalLines; i++) {
            boolean gap = i == STORY_LINES;
            if (gap) {
                continue;
            }
            float alpha = Mth.clamp((time - i * TICKS_PER_LINE) / FADE_TICKS, 0f, 1f);
            if (alpha <= 0.02f) {
                break;
            }
            int a = Math.max(5, (int) (alpha * 255));
            boolean credit = i > STORY_LINES;
            String key = credit ? "screen.glt.ending.credit." + (i - STORY_LINES - 1) : "screen.glt.ending.line." + i;
            int color = (a << 24) | (credit ? 0x9A9A9A : 0xE8E4DA);
            graphics.drawCenteredString(font, Component.translatable(key), width / 2, top + i * lineHeight, color);
        }
        if (time > totalLines * TICKS_PER_LINE + FADE_TICKS) {
            graphics.drawCenteredString(font, Component.translatable("screen.glt.ending.close"), width / 2, height - 20, 0xFF6A6A6A);
        }
    }
}
