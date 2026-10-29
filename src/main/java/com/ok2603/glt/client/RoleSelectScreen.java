package com.ok2603.glt.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.ok2603.glt.network.ChooseRolePayload;
import com.ok2603.glt.role.Role;
import com.ok2603.glt.role.Roles;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

/** The menu opened with H: pick Chito or Yuuri and see their perks and cons. */
public class RoleSelectScreen extends Screen {
    private static final int COLUMN_WIDTH = 170;
    private static final int GAP = 20;
    private static final int TOP = 36;

    private static final int GREEN = 0x7FD46B;
    private static final int RED = 0xE87070;
    private static final int GRAY = 0xA0A0A0;

    private static final String[] CHITO_PERKS = {"perk1", "perk2"};
    private static final String[] CHITO_CONS = {"con1", "con2", "con3"};
    private static final String[] YUURI_PERKS = {"perk1", "perk2", "perk3"};
    private static final String[] YUURI_CONS = {"con1", "con2"};

    public RoleSelectScreen() {
        super(Component.translatable("screen.glt.role.title"));
    }

    private int leftX() {
        return width / 2 - COLUMN_WIDTH - GAP / 2;
    }

    private int rightX() {
        return width / 2 + GAP / 2;
    }

    @Override
    protected void init() {
        int buttonY = height - 32;
        addRenderableWidget(Button.builder(Component.translatable("screen.glt.role.play_as_chito"), b -> choose(Role.CHITO))
                .bounds(leftX(), buttonY, COLUMN_WIDTH, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("screen.glt.role.play_as_yuuri"), b -> choose(Role.YUURI))
                .bounds(rightX(), buttonY, COLUMN_WIDTH, 20).build());
    }

    private void choose(Role role) {
        PacketDistributor.sendToServer(new ChooseRolePayload(role));
        onClose();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        graphics.drawCenteredString(font, title, width / 2, 10, 0xFFFFFF);
        if (minecraft != null && minecraft.player != null) {
            Role current = Roles.get(minecraft.player);
            Component currentName = current == Role.NONE
                    ? Component.translatable("screen.glt.role.none")
                    : Component.translatable("role.glt." + current.getSerializedName());
            graphics.drawCenteredString(font, Component.translatable("screen.glt.role.current", currentName),
                    width / 2, 22, GRAY);
        }

        drawColumn(graphics, Role.CHITO, leftX(), CHITO_PERKS, CHITO_CONS);
        drawColumn(graphics, Role.YUURI, rightX(), YUURI_PERKS, YUURI_CONS);

        graphics.drawCenteredString(font, Component.translatable("screen.glt.role.together"),
                width / 2, height - 46, 0xF0C060);
    }

    private void drawColumn(GuiGraphics graphics, Role role, int x, String[] perks, String[] cons) {
        String name = role.getSerializedName();
        ResourceLocation skin = RolePlayerRenderer.skinFor(role);

        // The character's face, taken from their skin (face + hat layer).
        RenderSystem.enableBlend();
        graphics.blit(skin, x, TOP, 24, 24, 8f, 8f, 8, 8, 64, 64);
        graphics.blit(skin, x, TOP, 24, 24, 40f, 8f, 8, 8, 64, 64);
        RenderSystem.disableBlend();

        graphics.drawString(font, Component.translatable("role.glt." + name), x + 30, TOP + 3, 0xFFFFFF);
        graphics.drawString(font, Component.translatable("role.glt." + name + ".tagline"), x + 30, TOP + 14, GRAY);

        int y = TOP + 32;
        for (String perk : perks) {
            y = drawWrapped(graphics, Component.literal("+ ").append(
                    Component.translatable("role.glt." + name + "." + perk)), x, y, GREEN);
        }
        y += 4;
        for (String con : cons) {
            y = drawWrapped(graphics, Component.literal("- ").append(
                    Component.translatable("role.glt." + name + "." + con)), x, y, RED);
        }
    }

    private int drawWrapped(GuiGraphics graphics, Component text, int x, int y, int color) {
        List<FormattedCharSequence> lines = font.split(text, COLUMN_WIDTH);
        for (FormattedCharSequence line : lines) {
            graphics.drawString(font, line, x, y, color);
            y += 10;
        }
        return y + 1;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
