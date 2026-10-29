package com.ok2603.glt.network;

import com.ok2603.glt.GLTMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Client → server: the player pressed Z to sit down or stand up. */
public record SitPayload() implements CustomPacketPayload {
    public static final SitPayload INSTANCE = new SitPayload();

    public static final Type<SitPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(GLTMod.MODID, "sit"));

    public static final StreamCodec<ByteBuf, SitPayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
