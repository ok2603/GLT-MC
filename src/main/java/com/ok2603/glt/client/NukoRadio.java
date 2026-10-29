package com.ok2603.glt.client;

import com.ok2603.glt.registry.ModItems;
import com.ok2603.glt.registry.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * While anyone holds Nuko, a little radio tune plays from them (and follows them around).
 * It stops as soon as they put Nuko down or switch items.
 */
public class NukoRadio {
    private static final Map<UUID, Song> PLAYING = new HashMap<>();

    public static boolean isHolding(Player player) {
        return player.getMainHandItem().is(ModItems.NUKO.get()) || player.getOffhandItem().is(ModItems.NUKO.get());
    }

    /** Called every client tick. */
    public static void tick(Minecraft mc) {
        if (mc.level == null) {
            PLAYING.clear();
            return;
        }
        PLAYING.values().removeIf(Song::isStopped);
        for (Player player : mc.level.players()) {
            if (isHolding(player) && !PLAYING.containsKey(player.getUUID())) {
                Song song = new Song(player);
                PLAYING.put(player.getUUID(), song);
                mc.getSoundManager().play(song);
            }
        }
    }

    private static class Song extends AbstractTickableSoundInstance {
        private final Player player;

        Song(Player player) {
            super(ModSounds.NUKO_RADIO.get(), SoundSource.NEUTRAL, SoundInstance.createUnseededRandom());
            this.player = player;
            this.looping = true;
            this.delay = 20;
            this.volume = 0.8f;
            this.x = player.getX();
            this.y = player.getY();
            this.z = player.getZ();
        }

        @Override
        public void tick() {
            if (player.isRemoved() || !isHolding(player)) {
                stop();
                return;
            }
            x = player.getX();
            y = player.getY() + 1.0;
            z = player.getZ();
        }
    }
}
