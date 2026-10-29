package com.ok2603.glt.survival;

import com.ok2603.glt.GLTMod;
import com.ok2603.glt.cold.BodyHeat;
import com.ok2603.glt.entity.KettenkradEntity;
import com.ok2603.glt.journal.Journal;
import com.ok2603.glt.journal.JournalMoment;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;

/** The Kettenkrad is home: a night's sleep next to it leaves you warm. */
@EventBusSubscriber(modid = GLTMod.MODID)
public class SleepEvents {
    private static final double HOME_RANGE = 8.0;

    @SubscribeEvent
    public static void onWakeUp(PlayerWakeUpEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        // Only after a full night, not when getting up early.
        long timeOfDay = player.level().getDayTime() % 24000;
        if (timeOfDay > 1000) {
            return;
        }
        boolean nearHome = !player.level().getEntitiesOfClass(KettenkradEntity.class,
                player.getBoundingBox().inflate(HOME_RANGE)).isEmpty();
        if (nearHome) {
            BodyHeat.set(player, BodyHeat.MAX);
            player.displayClientMessage(Component.translatable("message.glt.bedroll.slept_home"), true);
            Journal.unlock(player, JournalMoment.SLEEP_HOME);
        }
    }
}
