package com.ok2603.glt.journal;

import com.ok2603.glt.config.GLTConfig;
import com.ok2603.glt.network.JournalContentsPayload;
import com.ok2603.glt.role.Role;
import com.ok2603.glt.role.Roles;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

/** Writing to and reading the shared journal. */
public class Journal {
    /** How close the other girl has to be for a "together" moment. */
    private static final double TOGETHER_RANGE = 16.0;
    /** Longest page Chito can write herself. */
    public static final int MAX_PAGE_LENGTH = 200;

    /** The day of the trip, starting at day 1. */
    public static int today(MinecraftServer server) {
        return (int) (server.overworld().getDayTime() / 24000L) + 1;
    }

    /**
     * Writes a moment into the journal if it isn't there yet.
     * Returns true if it was new. Everyone gets a quiet "Chito wrote in the journal" note.
     */
    public static boolean unlock(ServerPlayer player, JournalMoment moment) {
        MinecraftServer server = player.server;
        JournalData data = JournalData.get(server);
        if (data.has(moment)) {
            return false;
        }
        if (moment.together() && !together(player)) {
            return false;
        }
        data.add(JournalEntry.ofMoment(moment, today(server)));
        for (ServerPlayer other : server.getPlayerList().getPlayers()) {
            other.displayClientMessage(Component.translatable("message.glt.journal.new_entry"), true);
            other.playNotifySound(SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 0.8f, 1.0f);
        }
        return true;
    }

    /** True if the other girl is close by, or nobody else is playing. */
    public static boolean together(ServerPlayer player) {
        if (!GLTConfig.get(GLTConfig.JOURNAL_NEEDS_BOTH)) {
            return true;
        }
        boolean anyoneElse = false;
        for (Player other : player.level().players()) {
            if (other == player || other.isSpectator()) {
                continue;
            }
            anyoneElse = true;
            if (other.distanceToSqr(player) < TOGETHER_RANGE * TOGETHER_RANGE) {
                return true;
            }
        }
        return !anyoneElse && player.server.getPlayerList().getPlayerCount() <= 1;
    }

    /** Only Chito writes in the journal (or anyone, if nobody is playing Chito). */
    public static boolean canWrite(Player player) {
        return Roles.isOrMissing(player, Role.CHITO);
    }

    /** Sends the journal to a player and opens it on their screen. */
    public static void open(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new JournalContentsPayload(JournalData.get(player.server).entries(), canWrite(player)));
    }

    /** A page Chito wrote herself. */
    public static void writePage(ServerPlayer player, String text) {
        String clean = text.strip();
        if (clean.isEmpty() || !canWrite(player)) {
            return;
        }
        if (clean.length() > MAX_PAGE_LENGTH) {
            clean = clean.substring(0, MAX_PAGE_LENGTH);
        }
        JournalData.get(player.server).add(JournalEntry.ofPage(clean, today(player.server)));
        player.playNotifySound(SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1.0f, 1.0f);
        open(player);
    }
}
