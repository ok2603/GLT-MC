package com.ok2603.glt.network;

import com.ok2603.glt.GLTMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Sent by the driver's game when the Kettenkrad crashes into a wall or drops from high up. */
public record KettenkradCrashPayload(int entityId, int damage) implements CustomPacketPayload {
    public static final Type<KettenkradCrashPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(GLTMod.MODID, "kettenkrad_crash"));

    public static final StreamCodec<ByteBuf, KettenkradCrashPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, KettenkradCrashPayload::entityId,
            ByteBufCodecs.VAR_INT, KettenkradCrashPayload::damage,
            KettenkradCrashPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
