package com.ok2603.glt.client;

import com.ok2603.glt.journal.Journal;
import com.ok2603.glt.journal.JournalEntry;
import com.ok2603.glt.journal.JournalMoment;
import com.ok2603.glt.network.JournalWritePayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

/**
 * Chito's journal: an open book with two pages. Each entry shows the day it happened.
 * Chito can write her own page with the "Write" button.
 */
public class JournalScreen extends Screen {
    private static final int BOOK_WIDTH = 300;
    private static final int BOOK_HEIGHT = 190;
    private static final int PAGE_MARGIN = 14;
    private static final int LINE_HEIGHT = 10;
    private static final int LINES_PER_PAGE = 15;

    private static final int PAPER = 0xFFE9DFC4;
    private static final int PAPER_SHADE = 0xFFD8CBA8;
    private static final int COVER = 0xFF5B3E26;
    private static final int INK = 0x3B2A1A;
    private static final int INK_DAY = 0x8A5A2E;
    private static final int INK_FAINT = 0x8C7B62;

    private record Line(FormattedCharSequence text, int color) {
    }

    private final List<JournalEntry> entries;
    private final boolean canWrite;
    private final List<List<Line>> pages = new ArrayList<>();
    /** Index of the left page of the spread being shown (always even). */
    private int spread = -1;

    private boolean writing;
    private MultiLineEditBox editor;

    public JournalScreen(List<JournalEntry> entries, boolean canWrite) {
        super(Component.translatable("screen.glt.journal.title"));
        this.entries = entries;
        this.canWrite = canWrite;
    }

    private int left() {
        return (width - BOOK_WIDTH) / 2;
    }

    private int top() {
        return (height - BOOK_HEIGHT) / 2 - 10;
    }

    private int pageTextWidth() {
        return BOOK_WIDTH / 2 - PAGE_MARGIN * 2;
    }

    @Override
    protected void init() {
        buildPages();
        if (spread < 0 || spread >= pages.size()) {
            // Open on the newest entries.
            spread = Math.max(0, (pages.size() - 1) / 2 * 2);
        }

        int buttonY = top() + BOOK_HEIGHT + 6;
        if (writing) {
            editor = new MultiLineEditBox(font, left() + 20, top() + 30, BOOK_WIDTH - 40, BOOK_HEIGHT - 50,
                    Component.translatable("screen.glt.journal.placeholder"), Component.empty());
            editor.setCharacterLimit(Journal.MAX_PAGE_LENGTH);
            addRenderableWidget(editor);
            setInitialFocus(editor);
            addRenderableWidget(Button.builder(Component.translatable("screen.glt.journal.save"), b -> save())
                    .bounds(width / 2 - 104, buttonY, 100, 20).build());
            addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), b -> {
                writing = false;
                rebuildWidgets();
            }).bounds(width / 2 + 4, buttonY, 100, 20).build());
            return;
        }

        addRenderableWidget(Button.builder(Component.literal("<"), b -> turn(-2))
                .bounds(left(), buttonY, 20, 20).build()).active = spread > 0;
        addRenderableWidget(Button.builder(Component.literal(">"), b -> turn(2))
                .bounds(left() + BOOK_WIDTH - 20, buttonY, 20, 20).build()).active = spread + 2 < pages.size();
        if (canWrite) {
            addRenderableWidget(Button.builder(Component.translatable("screen.glt.journal.write"), b -> {
                writing = true;
                rebuildWidgets();
            }).bounds(width / 2 - 104, buttonY, 100, 20).build());
        }
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> onClose())
                .bounds(canWrite ? width / 2 + 4 : width / 2 - 50, buttonY, 100, 20).build());
    }

    private void turn(int pagesToTurn) {
        spread = Math.max(0, Math.min(spread + pagesToTurn, Math.max(0, pages.size() - 1) / 2 * 2));
        rebuildWidgets();
    }

    private void save() {
        String text = editor.getValue().strip();
        if (!text.isEmpty()) {
            PacketDistributor.sendToServer(new JournalWritePayload(text));
        }
        writing = false;
        rebuildWidgets();
    }

    /** Splits all entries into lines and the lines into pages. */
    private void buildPages() {
        pages.clear();
        List<Line> page = new ArrayList<>();
        for (JournalEntry entry : entries) {
            Component text;
            if (entry.isMoment()) {
                JournalMoment moment = JournalMoment.byId(entry.moment());
                text = moment == null ? Component.literal("...") : Component.translatable(moment.translationKey());
            } else {
                text = Component.literal(entry.text());
            }
            List<Line> block = new ArrayList<>();
            block.add(new Line(Component.translatable("screen.glt.journal.day", entry.day()).getVisualOrderText(), INK_DAY));
            for (FormattedCharSequence line : font.split(text, pageTextWidth())) {
                block.add(new Line(line, INK));
            }
            // Start a new page rather than leave a "Day" heading alone at the bottom.
            if (!page.isEmpty() && page.size() + Math.min(block.size(), 2) > LINES_PER_PAGE) {
                pages.add(page);
                page = new ArrayList<>();
            }
            for (Line line : block) {
                if (page.size() >= LINES_PER_PAGE) {
                    pages.add(page);
                    page = new ArrayList<>();
                }
                page.add(line);
            }
            if (page.size() < LINES_PER_PAGE) {
                page.add(new Line(FormattedCharSequence.EMPTY, INK));
            }
        }
        if (!page.isEmpty()) {
            pages.add(page);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        int x = left();
        int y = top();

        // The open book: a leather cover with two paper pages.
        graphics.fill(x - 4, y - 4, x + BOOK_WIDTH + 4, y + BOOK_HEIGHT + 4, COVER);
        graphics.fill(x, y, x + BOOK_WIDTH, y + BOOK_HEIGHT, PAPER);
        graphics.fill(x + BOOK_WIDTH / 2 - 1, y, x + BOOK_WIDTH / 2 + 1, y + BOOK_HEIGHT, PAPER_SHADE);

        if (writing) {
            graphics.drawCenteredString(font, Component.translatable("screen.glt.journal.writing"), width / 2, y + 12, INK_DAY);
            editor.render(graphics, mouseX, mouseY, partialTick);
            return;
        }

        graphics.drawString(font, title, x + PAGE_MARGIN, y - 14, 0xFFFFFF, true);
        if (pages.isEmpty()) {
            for (FormattedCharSequence line : font.split(Component.translatable("screen.glt.journal.empty"), pageTextWidth())) {
                graphics.drawString(font, line, x + PAGE_MARGIN, y + PAGE_MARGIN, INK_FAINT, false);
                y += LINE_HEIGHT;
            }
            return;
        }
        drawPage(graphics, spread, x + PAGE_MARGIN);
        drawPage(graphics, spread + 1, x + BOOK_WIDTH / 2 + PAGE_MARGIN);
    }

    private void drawPage(GuiGraphics graphics, int index, int x) {
        if (index >= pages.size()) {
            return;
        }
        int y = top() + PAGE_MARGIN;
        for (Line line : pages.get(index)) {
            graphics.drawString(font, line.text(), x, y, line.color(), false);
            y += LINE_HEIGHT;
        }
        String number = String.valueOf(index + 1);
        graphics.drawString(font, number, x + pageTextWidth() / 2 - font.width(number) / 2,
                top() + BOOK_HEIGHT - 12, INK_FAINT, false);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
