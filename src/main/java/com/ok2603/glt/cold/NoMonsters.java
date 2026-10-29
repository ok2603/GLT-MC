package com.ok2603.glt.cold;

import com.ok2603.glt.GLTMod;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;

import java.util.EnumSet;
import java.util.Set;

/**
 * The world of Girls' Last Tour has no monsters. The danger is the cold.
 *
 * Hostile mobs never appear on their own in the Overworld: no zombies at night,
 * no creepers, phantoms, patrols, raids or structure mobs.
 * Spawn eggs, commands and monster spawners still work, so you can add them on purpose.
 * The Nether and the End are left alone, so you can still beat the game.
 */
@EventBusSubscriber(modid = GLTMod.MODID)
public class NoMonsters {
    /** The ways a mob can appear "by itself". These are blocked for monsters. */
    private static final Set<MobSpawnType> NATURAL_SPAWNS = EnumSet.of(
            MobSpawnType.NATURAL,
            MobSpawnType.CHUNK_GENERATION,
            MobSpawnType.STRUCTURE,
            MobSpawnType.PATROL,
            MobSpawnType.EVENT,
            MobSpawnType.REINFORCEMENT,
            MobSpawnType.JOCKEY,
            MobSpawnType.TRIGGERED);

    @SubscribeEvent
    public static void onMobSpawn(FinalizeSpawnEvent event) {
        if (event.getEntity() instanceof Enemy
                && NATURAL_SPAWNS.contains(event.getSpawnType())
                && event.getEntity().level().dimension() == Level.OVERWORLD) {
            event.setSpawnCancelled(true);
        }
    }
}
