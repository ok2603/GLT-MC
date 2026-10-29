package com.ok2603.glt.entity;

import com.ok2603.glt.registry.ModTags;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;

import java.util.EnumSet;
import java.util.List;

/** Nuko walks over to ammo or explosives lying on the ground and eats them. */
public class EatAmmoGoal extends Goal {
    private static final double SEARCH_RANGE = 8.0;

    private final NukoEntity nuko;
    private ItemEntity target;
    private int cooldown;

    public EatAmmoGoal(NukoEntity nuko) {
        this.nuko = nuko;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (cooldown > 0) {
            cooldown--;
            return false;
        }
        cooldown = 20;
        List<ItemEntity> items = nuko.level().getEntitiesOfClass(ItemEntity.class,
                nuko.getBoundingBox().inflate(SEARCH_RANGE), item -> item.isAlive() && item.getItem().is(ModTags.NUKO_FOOD));
        target = null;
        double best = Double.MAX_VALUE;
        for (ItemEntity item : items) {
            double distance = item.distanceToSqr(nuko);
            if (distance < best) {
                best = distance;
                target = item;
            }
        }
        return target != null;
    }

    @Override
    public boolean canContinueToUse() {
        return target != null && target.isAlive() && target.distanceToSqr(nuko) < SEARCH_RANGE * SEARCH_RANGE * 2;
    }

    @Override
    public void start() {
        nuko.getNavigation().moveTo(target, 1.1);
    }

    @Override
    public void tick() {
        nuko.getLookControl().setLookAt(target);
        if (nuko.distanceToSqr(target) < 1.5 * 1.5) {
            nuko.eat(target);
            target = null;
        } else if (nuko.getNavigation().isDone()) {
            nuko.getNavigation().moveTo(target, 1.1);
        }
    }

    @Override
    public void stop() {
        target = null;
        nuko.getNavigation().stop();
    }
}
