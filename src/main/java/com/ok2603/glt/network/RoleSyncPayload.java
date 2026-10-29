package com.ok2603.glt.network;

import com.ok2603.glt.GLTMod;
import com.ok2603.glt.role.Role;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Server → client: "the player with this id is playing as this role". */
public record RoleSyncPayload(int entityId, Role role) implements CustomPacketPayload {
    public static final Type<RoleSyncPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(GLTMod.MODID, "role_sync"));

    public static final StreamCodec<ByteBuf, Role> ROLE_CODEC = ByteBufCodecs.VAR_INT.map(Role::byId, Role::ordinal);

    public static final StreamCodec<ByteBuf, RoleSyncPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, RoleSyncPayload::entityId,
            ROLE_CODEC, RoleSyncPayload::role,
            RoleSyncPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
