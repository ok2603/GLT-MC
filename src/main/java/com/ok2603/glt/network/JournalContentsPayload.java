package com.ok2603.glt.network;

import com.ok2603.glt.GLTMod;
import com.ok2603.glt.journal.JournalEntry;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/** Server → client: here is the journal, open it. */
public record JournalContentsPayload(List<JournalEntry> entries, boolean canWrite) implements CustomPacketPayload {
    public static final Type<JournalContentsPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(GLTMod.MODID, "journal_contents"));

    public static final StreamCodec<ByteBuf, JournalContentsPayload> STREAM_CODEC = StreamCodec.composite(
            JournalEntry.STREAM_CODEC.apply(ByteBufCodecs.list()), JournalContentsPayload::entries,
            ByteBufCodecs.BOOL, JournalContentsPayload::canWrite,
            JournalContentsPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
