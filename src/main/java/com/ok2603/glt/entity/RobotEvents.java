package com.ok2603.glt.entity;

import com.ok2603.glt.GLTMod;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.ItemFishedEvent;

/** Lets fish farm robots notice when someone takes their fish. */
@EventBusSubscriber(modid = GLTMod.MODID)
public class RobotEvents {
    private static final double NOTICE_RANGE = 24;

    @SubscribeEvent
    public static void onFishKilled(LivingDeathEvent event) {
        if (event.getEntity() instanceof AbstractFish fish && !fish.level().isClientSide
                && event.getSource().getEntity() instanceof Player player) {
            tellKeepers(fish, player);
        }
    }

    @SubscribeEvent
    public static void onFishCaught(ItemFishedEvent event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide) {
            tellKeepers(event.getHookEntity(), player);
        }
    }

    private static void tellKeepers(Entity where, Player player) {
        AABB area = where.getBoundingBox().inflate(NOTICE_RANGE);
        for (RobotEntity robot : where.level().getEntitiesOfClass(RobotEntity.class, area, RobotEntity::isKeeper)) {
            robot.onFishTaken(player);
        }
    }
}
