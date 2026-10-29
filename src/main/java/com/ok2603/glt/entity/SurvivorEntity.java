package com.ok2603.glt.entity;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * One of the few people still living in the city (Kanazawa, Ishii). They wander a little,
 * look at you, talk when you right-click them, and never despawn. Players can't hurt them:
 * this is a peaceful story.
 */
public abstract class SurvivorEntity extends PathfinderMob {
    protected SurvivorEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.22);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.6) {
            @Override
            public boolean canUse() {
                return !isBusy() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return !isBusy() && super.canContinueToUse();
            }
        });
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0f));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    /** True while doing something special (like flying), so the wandering goal leaves them alone. */
    protected boolean isBusy() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return source.getEntity() instanceof Player || super.isInvulnerableTo(source);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        if (!level().isClientSide) {
            talk(player);
        }
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    /** Called on the server when a player right-clicks them. */
    protected abstract void talk(Player player);

    /** Shows "Name: line" to one player. */
    protected void say(Player player, String key, Object... args) {
        player.displayClientMessage(Component.translatable("chat.glt.says", getDisplayName(),
                Component.translatable(key, args).withStyle(ChatFormatting.ITALIC)), false);
    }
}
