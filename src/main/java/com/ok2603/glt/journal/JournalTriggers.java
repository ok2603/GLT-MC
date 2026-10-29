package com.ok2603.glt.journal;

import com.ok2603.glt.GLTMod;
import com.ok2603.glt.cold.BodyHeat;
import com.ok2603.glt.entity.KettenkradEntity;
import com.ok2603.glt.entity.SeatEntity;
import com.ok2603.glt.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Watches for the moments that write themselves into the journal.
 * Moments caused by a specific action (finding chocolate, camping, repairs...) call
 * Journal.unlock directly where they happen; this class handles eating and looking around.
 */
@EventBusSubscriber(modid = GLTMod.MODID)
public class JournalTriggers {
    /** How high you have to be to see the real stars (or "a high place"). */
    public static final int STARS_HEIGHT = 100;
    public static final int HIGH_PLACE_HEIGHT = 200;

    @SubscribeEvent
    public static void onFinishEating(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        ItemStack food = event.getItem();
        if (food.is(ModItems.RATION_BAR.get())) {
            Journal.unlock(player, JournalMoment.RATION);
        } else if (food.is(ModItems.CHOCOLATE_RATION.get())) {
            Journal.unlock(player, JournalMoment.CHOCOLATE);
        } else if (food.is(ModItems.CANNED_FISH.get())) {
            Journal.unlock(player, JournalMoment.FISH);
        } else if (food.is(ModItems.SOUP.get())) {
            Journal.unlock(player, JournalMoment.SOUP);
        } else if (food.is(ModItems.BEER.get())) {
            Journal.unlock(player, JournalMoment.BEER);
        }
    }

    /** Every few seconds, look around for quiet moments: snow, stars, the cold, high places. */
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % 100 != 0
                || player.isSpectator() || player.isCreative()) {
            return;
        }
        ServerLevel level = player.serverLevel();
        BlockPos pos = player.blockPosition();
        boolean underSky = level.canSeeSky(BlockPos.containing(player.getEyePosition()));
        Biome biome = level.getBiome(pos).value();

        if (underSky && level.isRaining() && biome.getPrecipitationAt(pos) == Biome.Precipitation.SNOW) {
            Journal.unlock(player, JournalMoment.SNOW);
        }

        if (BodyHeat.get(player) <= 0) {
            Journal.unlock(player, JournalMoment.FREEZING);
        } else if (BodyHeat.get(player) < BodyHeat.CHILLY && BodyHeat.isHuddling(player)) {
            Journal.unlock(player, JournalMoment.HUDDLE);
        }

        // Sitting under a clear night sky, high up.
        boolean sitting = player.getVehicle() instanceof SeatEntity || player.getVehicle() instanceof KettenkradEntity;
        if (sitting && underSky && level.dimension() == Level.OVERWORLD && !level.isRaining()
                && isNight(level) && pos.getY() >= STARS_HEIGHT) {
            Journal.unlock(player, JournalMoment.STARS);
        }

        if (player.getMainHandItem().is(ModItems.NUKO.get()) || player.getOffhandItem().is(ModItems.NUKO.get())) {
            Journal.unlock(player, JournalMoment.NUKO_RADIO);
        }

        if (pos.getY() >= HIGH_PLACE_HEIGHT && level.dimension() == Level.OVERWORLD) {
            Journal.unlock(player, JournalMoment.HIGH_PLACE);
        }
    }

    public static boolean isNight(Level level) {
        long time = level.getDayTime() % 24000L;
        return time > 13000 && time < 23000;
    }
}
