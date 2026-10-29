package com.ok2603.glt.city;

import com.ok2603.glt.GLTMod;
import com.ok2603.glt.journal.Journal;
import com.ok2603.glt.journal.JournalData;
import com.ok2603.glt.journal.JournalMoment;
import com.ok2603.glt.network.EndingPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

/**
 * The end of the trip. When everyone playing has climbed to the top floor of the Last City
 * and stands together by the black stone at the summit, the journal gets its last entry and
 * the ending plays. It happens once per world; after that you can keep playing.
 */
@EventBusSubscriber(modid = GLTMod.MODID)
public class Ending {
    /** The summit stone, in the middle of cell (0, 0) on the top floor. */
    public static final Vec3 SUMMIT = new Vec3(18.5, CityLayout.floorY(CityLayout.FLOORS - 1), 18.5);
    public static final double RANGE = 8.0;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % 20 != 0
                || !LastCity.isCity(player.level()) || !isAtSummit(player)) {
            return;
        }
        if (JournalData.get(player.server).has(JournalMoment.ENDING)) {
            return;
        }
        List<ServerPlayer> everyone = player.server.getPlayerList().getPlayers().stream()
                .filter(p -> !p.isSpectator()).toList();
        if (!everyone.stream().allMatch(Ending::isAtSummit)) {
            return;
        }
        if (Journal.unlock(player, JournalMoment.ENDING)) {
            for (ServerPlayer p : everyone) {
                PacketDistributor.sendToPlayer(p, EndingPayload.INSTANCE);
            }
        }
    }

    public static boolean isAtSummit(ServerPlayer player) {
        if (!LastCity.isCity(player.level())) {
            return false;
        }
        Vec3 pos = player.position();
        double dx = pos.x - SUMMIT.x;
        double dz = pos.z - SUMMIT.z;
        return dx * dx + dz * dz < RANGE * RANGE && Math.abs(pos.y - SUMMIT.y) < 6;
    }
}
