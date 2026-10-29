package com.ok2603.glt.journal;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * One entry in the journal: either a moment (moment = its id, text empty)
 * or a page Chito wrote herself (moment empty, text = what she wrote).
 */
public record JournalEntry(String moment, String text, int day) {
    public static final StreamCodec<ByteBuf, JournalEntry> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, JournalEntry::moment,
            ByteBufCodecs.STRING_UTF8, JournalEntry::text,
            ByteBufCodecs.VAR_INT, JournalEntry::day,
            JournalEntry::new);

    public static JournalEntry ofMoment(JournalMoment moment, int day) {
        return new JournalEntry(moment.id(), "", day);
    }

    public static JournalEntry ofPage(String text, int day) {
        return new JournalEntry("", text, day);
    }

    public boolean isMoment() {
        return !moment.isEmpty();
    }
}
