package com.ok2603.glt.role;

import com.ok2603.glt.GLTMod;
import com.ok2603.glt.network.RoleSyncPayload;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/** Keeps everyone's role in sync and runs the perks that need to tick. */
@EventBusSubscriber(modid = GLTMod.MODID)
public class RoleEvents {
    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            Roles.sync(player);
            Roles.applyAttributes(player);
            if (Roles.get(player) == Role.NONE) {
                player.sendSystemMessage(Component.translatable("message.glt.role.hint"));
            }
        }
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            Roles.sync(player);
            Roles.applyAttributes(player);
        }
    }

    @SubscribeEvent
    public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            Roles.sync(player);
        }
    }

    /** When a player comes into view of another player, tell the viewer who they are. */
    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof Player target && event.getEntity() instanceof ServerPlayer viewer) {
            PacketDistributor.sendToPlayer(viewer, new RoleSyncPayload(target.getId(), Roles.get(target)));
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.isCreative() || player.isSpectator()) {
            return;
        }
        // Yuuri gets hungry faster.
        if (Roles.get(player) == Role.YUURI) {
            player.causeFoodExhaustion(Roles.YUURI_EXTRA_HUNGER);
        }
    }
}
