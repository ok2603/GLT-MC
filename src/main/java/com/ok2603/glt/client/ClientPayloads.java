package com.ok2603.glt.client;

import com.ok2603.glt.network.JournalContentsPayload;
import net.minecraft.client.Minecraft;

/**
 * Handling for messages that only make sense on the player's own computer (opening screens).
 * Kept in its own class so a dedicated server never loads the screen code.
 */
public class ClientPayloads {
    public static void openJournal(JournalContentsPayload payload) {
        Minecraft.getInstance().setScreen(new JournalScreen(payload.entries(), payload.canWrite()));
    }

    public static void showEnding() {
        Minecraft.getInstance().setScreen(new EndingScreen());
    }
}
