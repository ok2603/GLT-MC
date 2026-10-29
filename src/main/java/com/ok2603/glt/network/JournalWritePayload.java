package com.ok2603.glt.network;

import com.ok2603.glt.GLTMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Client → server: Chito wrote a page in the journal. */
public record JournalWritePayload(String text) implements CustomPacketPayload {
    public static final Type<JournalWritePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(GLTMod.MODID, "journal_write"));

    public static final StreamCodec<ByteBuf, JournalWritePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(512), JournalWritePayload::text,
            JournalWritePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
