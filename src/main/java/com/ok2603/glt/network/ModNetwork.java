package com.ok2603.glt.network;

import com.ok2603.glt.GLTMod;
import com.ok2603.glt.entity.SeatEntity;
import com.ok2603.glt.registry.ModAttachments;
import com.ok2603.glt.role.Role;
import com.ok2603.glt.role.Roles;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/** Registers the messages the server and client send each other. */
@EventBusSubscriber(modid = GLTMod.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ModNetwork {
    /** The latest body heat the server told this client about (only used on the client). */
    public static float clientBodyHeat = 100f;

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("1");

        registrar.playToClient(BodyHeatPayload.TYPE, BodyHeatPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> clientBodyHeat = payload.heat()));

        registrar.playToClient(RoleSyncPayload.TYPE, RoleSyncPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    Entity entity = context.player().level().getEntity(payload.entityId());
                    if (entity instanceof Player player) {
                        player.setData(ModAttachments.ROLE, payload.role());
                    }
                }));

        registrar.playToServer(SitPayload.TYPE, SitPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        SeatEntity.toggleSitting(player);
                    }
                }));

        registrar.playToServer(ChooseRolePayload.TYPE, ChooseRolePayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player && payload.role() != Role.NONE) {
                        Roles.set(player, payload.role());
                    }
                }));
    }
}
