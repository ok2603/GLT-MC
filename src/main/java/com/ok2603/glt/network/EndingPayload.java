package com.ok2603.glt.network;

import com.ok2603.glt.GLTMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Server → client: the girls reached the top. Show the ending. */
public record EndingPayload() implements CustomPacketPayload {
    public static final EndingPayload INSTANCE = new EndingPayload();

    public static final Type<EndingPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(GLTMod.MODID, "ending"));

    public static final StreamCodec<ByteBuf, EndingPayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
