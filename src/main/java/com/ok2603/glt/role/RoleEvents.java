package com.ok2603.glt.role;

import com.ok2603.glt.GLTMod;
import com.ok2603.glt.config.GLTConfig;
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
            if (Roles.get(player) == Role.NONE && GLTConfig.get(GLTConfig.AUTO_ROLES)) {
                Role role = Roles.pickAutomaticRole(player);
                if (role != Role.NONE) {
                    Roles.set(player, role);
                    player.sendSystemMessage(Component.translatable("message.glt.role.assigned",
                            Component.translatable("role.glt." + role.getSerializedName())));
                }
            }
            Roles.sync(player);
            Roles.applyAttributes(player);
            player.refreshDisplayName();
            player.refreshTabListName();
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

    /** Show players as "Chito" or "Yuuri" in chat and above their heads. */
    @SubscribeEvent
    public static void onNameFormat(PlayerEvent.NameFormat event) {
        Component name = characterName(event.getEntity());
        if (name != null) {
            event.setDisplayname(name);
        }
    }

    /** ...and in the player list (Tab). */
    @SubscribeEvent
    public static void onTabListName(PlayerEvent.TabListNameFormat event) {
        Component name = characterName(event.getEntity());
        if (name != null) {
            event.setDisplayName(name);
        }
    }

    private static Component characterName(Player player) {
        Role role = Roles.get(player);
        if (role == Role.NONE || !GLTConfig.get(GLTConfig.CHARACTER_NAMES)) {
            return null;
        }
        return Component.translatable("role.glt." + role.getSerializedName());
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
