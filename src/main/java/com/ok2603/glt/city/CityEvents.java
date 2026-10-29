package com.ok2603.glt.city;

import com.ok2603.glt.GLTMod;
import com.ok2603.glt.journal.Journal;
import com.ok2603.glt.journal.JournalMoment;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Makes the Last City world playable from the first second:
 *  - the world spawn is set on the bottom floor (vanilla would pick the top of the highest roof),
 *  - new players and players without a bed are placed there,
 *  - reaching a new floor for the first time is announced and written in the journal.
 */
@EventBusSubscriber(modid = GLTMod.MODID)
public class CityEvents {
    private static final String ARRIVED = "glt_city_arrived";
    private static final String HIGHEST_FLOOR = "glt_city_highest_floor";

    @SubscribeEvent
    public static void onCreateSpawn(LevelEvent.CreateSpawnPosition event) {
        if (event.getLevel() instanceof ServerLevel level && LastCity.isCity(level)) {
            event.getSettings().setSpawn(LastCity.SPAWN, 0f);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !LastCity.isCity(player.level())) {
            return;
        }
        CompoundTag data = player.getPersistentData();
        if (!data.getBoolean(ARRIVED)) {
            data.putBoolean(ARRIVED, true);
            sendToStart(player);
        }
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.isEndConquered() || !(event.getEntity() instanceof ServerPlayer player)
                || !LastCity.isCity(player.level())) {
            return;
        }
        // Slept in a bed or bedroll? Vanilla already put you there. Otherwise, back to the start.
        BlockPos bed = player.getRespawnPosition();
        if (bed == null || !player.getRespawnDimension().equals(player.level().dimension())
                || bed.distSqr(player.blockPosition()) > 8 * 8) {
            sendToStart(player);
        }
    }

    private static void sendToStart(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        BlockPos spawn = level.getSharedSpawnPos();
        // Only trust the shared spawn if it is on a floor (someone may have moved it with /setworldspawn).
        if (spawn.getY() > level.getMaxBuildHeight() - 16) {
            spawn = LastCity.SPAWN;
        }
        player.teleportTo(level, spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5, player.getYRot(), player.getXRot());
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % 40 != 0
                || !LastCity.isCity(player.level()) || !player.onGround()) {
            return;
        }
        int floor = LastCity.floor(player.getY());
        CompoundTag data = player.getPersistentData();
        if (floor > data.getInt(HIGHEST_FLOOR)) {
            data.putInt(HIGHEST_FLOOR, floor);
            player.displayClientMessage(Component.translatable(
                    CityLayout.isTopFloor(floor) ? "message.glt.city.top_floor" : "message.glt.city.new_floor", floor + 1), true);
            Journal.unlock(player, JournalMoment.NEW_FLOOR);
        }
    }
}
