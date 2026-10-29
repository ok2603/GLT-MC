package com.ok2603.glt.client;

import com.ok2603.glt.menu.FoodFactoryMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

/** The food factory's screen, drawn with plain shapes in the colors of old machinery. */
public class FoodFactoryScreen extends AbstractContainerScreen<FoodFactoryMenu> {
    private static final int PANEL = 0xFF6E7468;
    private static final int PANEL_DARK = 0xFF3E423A;
    private static final int PANEL_LIGHT = 0xFF9AA093;
    private static final int SLOT = 0xFF2A2D27;
    private static final int ARROW = 0xFF2A2D27;
    private static final int ARROW_FILL = 0xFFE8C35A;

    public FoodFactoryScreen(FoodFactoryMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
        inventoryLabelY = imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        graphics.fill(x, y, x + imageWidth, y + imageHeight, PANEL_DARK);
        graphics.fill(x + 1, y + 1, x + imageWidth - 1, y + imageHeight - 1, PANEL_LIGHT);
        graphics.fill(x + 2, y + 2, x + imageWidth - 1, y + imageHeight - 1, PANEL);

        for (Slot slot : menu.slots) {
            graphics.fill(x + slot.x - 1, y + slot.y - 1, x + slot.x + 17, y + slot.y + 17, SLOT);
            graphics.fill(x + slot.x, y + slot.y, x + slot.x + 16, y + slot.y + 16, 0xFF8B8B8B);
        }

        // Progress arrow from the inputs to the output.
        int arrowX = x + 84;
        int arrowY = y + 38;
        graphics.fill(arrowX, arrowY, arrowX + 24, arrowY + 10, ARROW);
        int filled = Math.round(menu.progress() * 22);
        graphics.fill(arrowX + 1, arrowY + 1, arrowX + 1 + filled, arrowY + 9, ARROW_FILL);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        graphics.drawString(font, Component.translatable("screen.glt.factory.power", menu.power()), 76, 58, 0x202020, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
