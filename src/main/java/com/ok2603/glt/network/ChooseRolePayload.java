package com.ok2603.glt.network;

import com.ok2603.glt.GLTMod;
import com.ok2603.glt.role.Role;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Client → server: "I want to play as this role" (sent from the H menu). */
public record ChooseRolePayload(Role role) implements CustomPacketPayload {
    public static final Type<ChooseRolePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(GLTMod.MODID, "choose_role"));

    public static final StreamCodec<ByteBuf, ChooseRolePayload> STREAM_CODEC =
            RoleSyncPayload.ROLE_CODEC.map(ChooseRolePayload::new, ChooseRolePayload::role);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
