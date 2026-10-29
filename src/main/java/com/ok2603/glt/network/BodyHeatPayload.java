package com.ok2603.glt.network;

import com.ok2603.glt.GLTMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Sent from the server to a player so their screen can show their body heat. */
public record BodyHeatPayload(float heat) implements CustomPacketPayload {
    public static final Type<BodyHeatPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(GLTMod.MODID, "body_heat"));

    public static final StreamCodec<ByteBuf, BodyHeatPayload> STREAM_CODEC =
            ByteBufCodecs.FLOAT.map(BodyHeatPayload::new, BodyHeatPayload::heat);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
