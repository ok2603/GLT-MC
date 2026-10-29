package com.ok2603.glt.entity;

import com.ok2603.glt.journal.Journal;
import com.ok2603.glt.journal.JournalMoment;
import com.ok2603.glt.registry.ModItems;
import com.ok2603.glt.registry.ModSounds;
import com.ok2603.glt.registry.ModTags;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * Nuko: a small white creature that eats ammunition and explosives.
 * Completely harmless. Feed it a cartridge, gunpowder or TNT and it follows you.
 * It also gobbles up any ammo lying on the ground nearby.
 * Sneak + right-click with an empty hand to pick it up; while you hold it, it plays the radio.
 */
public class NukoEntity extends TamableAnimal {
    public NukoEntity(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 8.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.FOLLOW_RANGE, 16.0);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new EatAmmoGoal(this));
        goalSelector.addGoal(3, new TemptGoal(this, 1.1, Ingredient.of(ModTags.NUKO_FOOD), false));
        goalSelector.addGoal(4, new FollowOwnerGoal(this, 1.0, 6.0f, 2.0f));
        goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.8));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0f));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(ModTags.NUKO_FOOD);
    }

    /** Nuko doesn't have babies. */
    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        return null;
    }

    @Override
    public boolean canFallInLove() {
        return false;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // Feed it.
        if (isFood(stack)) {
            if (!level().isClientSide) {
                eat();
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                if (!isTame()) {
                    tame(player);
                    level().broadcastEntityEvent(this, (byte) 7); // hearts
                }
                if (player instanceof ServerPlayer serverPlayer) {
                    Journal.unlock(serverPlayer, JournalMoment.NUKO);
                }
            }
            return InteractionResult.sidedSuccess(level().isClientSide);
        }

        // Pick it up.
        if (stack.isEmpty() && player.isSecondaryUseActive() && (!isTame() || isOwnedBy(player))) {
            if (!level().isClientSide) {
                ItemStack held = com.ok2603.glt.item.NukoItem.capture(this);
                if (!player.getInventory().add(held)) {
                    player.drop(held, false);
                }
                playSound(ModSounds.NUKO_AMBIENT.get(), 1.0f, 1.2f);
                discard();
            }
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    /** Munch: heals a little and shows it's happy. */
    public void eat() {
        heal(2.0f);
        level().playSound(null, getX(), getY(), getZ(), ModSounds.NUKO_EAT.get(), SoundSource.NEUTRAL, 1.0f, 1.0f);
        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, getX(), getY() + 0.5, getZ(), 5, 0.2, 0.2, 0.2, 0);
        }
    }

    /** Eats an ammo item lying on the ground. */
    public void eat(ItemEntity item) {
        ItemStack stack = item.getItem();
        stack.shrink(1);
        if (stack.isEmpty()) {
            item.discard();
        } else {
            item.setItem(stack);
        }
        eat();
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.NUKO_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.RABBIT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.RABBIT_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 0.6f;
    }

    /** It's harmless and nothing wants to hurt it... but it would be sad to lose it to a fall. */
    @Override
    public int getMaxFallDistance() {
        return 4;
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(ModItems.NUKO_SPAWN_EGG.get());
    }
}
