package com.ok2603.glt.entity;

import com.ok2603.glt.journal.Journal;
import com.ok2603.glt.journal.JournalMoment;
import com.ok2603.glt.registry.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Ishii, who is building a plane to fly to the next city. Bring her scrap parts and one
 * Fuel Canister and she takes off... and the plane falls apart in the air. She floats down
 * on her parachute and shares her food and fuel with you, since she won't need it anymore.
 */
public class IshiiEntity extends SurvivorEntity {
    public static final int PARTS_NEEDED = 8;
    private static final EntityDataAccessor<Boolean> FLYING =
            SynchedEntityData.defineId(IshiiEntity.class, EntityDataSerializers.BOOLEAN);

    private static final int STATE_BUILDING = 0;
    private static final int STATE_FLYING = 1;
    private static final int STATE_LANDED = 2;
    /** When the engine starts failing, and when the plane breaks apart. */
    private static final int SPUTTER_TICK = 140;
    private static final int BREAK_TICK = 190;

    private int parts;
    private int state = STATE_BUILDING;
    private int flightTicks;
    private float flightYaw;

    public IshiiEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(FLYING, false);
    }

    /** True while the plane is in the air (the renderer draws it then). */
    public boolean isFlyingPlane() {
        return entityData.get(FLYING);
    }

    public int parts() {
        return parts;
    }

    @Override
    protected boolean isBusy() {
        return state == STATE_FLYING;
    }

    @Override
    protected void talk(Player player) {
        if (state == STATE_FLYING) {
            return;
        }
        if (state == STATE_LANDED) {
            say(player, "chat.glt.ishii.after");
            return;
        }
        ItemStack held = player.getMainHandItem();
        if (parts < PARTS_NEEDED && held.is(ModItems.SCRAP_PARTS.get())) {
            int taken = Math.min(held.getCount(), PARTS_NEEDED - parts);
            if (!player.getAbilities().instabuild) {
                held.shrink(taken);
            }
            parts += taken;
            playSound(SoundEvents.ANVIL_USE, 0.5f, 1.4f);
            say(player, parts >= PARTS_NEEDED ? "chat.glt.ishii.parts_done" : "chat.glt.ishii.parts", parts, PARTS_NEEDED);
            return;
        }
        if (parts >= PARTS_NEEDED && held.is(ModItems.FUEL_CANISTER.get())) {
            if (!player.getAbilities().instabuild) {
                held.shrink(1);
            }
            say(player, "chat.glt.ishii.takeoff");
            takeOff(player);
            return;
        }
        say(player, parts >= PARTS_NEEDED ? "chat.glt.ishii.need_fuel" : "chat.glt.ishii.hello", parts, PARTS_NEEDED);
    }

    /** Starts the flight, heading away from the player who helped. Public so tests can start it. */
    public void takeOff(Player player) {
        state = STATE_FLYING;
        flightTicks = 0;
        flightYaw = player != null ? player.getYRot() : getYRot();
        entityData.set(FLYING, true);
        getNavigation().stop();
        level().playSound(null, blockPosition(), SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.NEUTRAL, 1.0f, 0.6f);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide || state != STATE_FLYING) {
            return;
        }
        ServerLevel level = (ServerLevel) level();
        flightTicks++;
        setYRot(flightYaw);
        setYBodyRot(flightYaw);
        setYHeadRot(flightYaw);
        Vec3 forward = Vec3.directionFromRotation(0, flightYaw);

        if (flightTicks < BREAK_TICK && !horizontalCollision) {
            setNoGravity(true);
            double speed = Math.min(0.6, 0.15 + flightTicks * 0.01);
            double climb = flightTicks < 20 ? 0.02 : flightTicks < SPUTTER_TICK ? 0.22 : -0.05;
            setDeltaMovement(forward.x * speed, climb, forward.z * speed);
            hasImpulse = true;
            if (flightTicks % 4 == 0) {
                level.playSound(null, blockPosition(), SoundEvents.MINECART_RIDING, SoundSource.NEUTRAL, 0.8f, 1.6f);
            }
            if (flightTicks > SPUTTER_TICK) {
                level.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY() + 0.5, getZ(), 3, 0.3, 0.2, 0.3, 0.01);
                if (flightTicks % 10 == 0) {
                    level.playSound(null, blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.NEUTRAL, 1.0f, 0.5f);
                }
            } else {
                level.sendParticles(ParticleTypes.CLOUD, getX() - forward.x, getY() + 0.4, getZ() - forward.z, 1, 0, 0, 0, 0);
            }
            return;
        }

        if (entityData.get(FLYING)) {
            // The plane breaks apart. Out comes the parachute.
            entityData.set(FLYING, false);
            setNoGravity(false);
            level.sendParticles(ParticleTypes.EXPLOSION, getX(), getY() + 0.5, getZ(), 3, 0.6, 0.4, 0.6, 0);
            level.playSound(null, blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.NEUTRAL, 0.8f, 1.2f);
            addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 20 * 60, 0, false, false));
        }
        if (onGround() || isInWater()) {
            land(level);
        }
    }

    private void land(ServerLevel level) {
        state = STATE_LANDED;
        removeEffect(MobEffects.SLOW_FALLING);
        level.playSound(null, blockPosition(), SoundEvents.WOOL_PLACE, SoundSource.NEUTRAL, 1.0f, 0.8f);
        // She won't need her supplies for the trip anymore.
        for (ItemStack stack : new ItemStack[]{new ItemStack(ModItems.RATION_BAR.get(), 6),
                new ItemStack(ModItems.FUEL_CANISTER.get(), 2), new ItemStack(ModItems.CHOCOLATE_RATION.get(), 1)}) {
            ItemEntity item = new ItemEntity(level, getX(), getY() + 0.5, getZ(), stack);
            item.setDefaultPickUpDelay();
            level.addFreshEntity(item);
        }
        for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, getBoundingBox().inflate(96))) {
            say(player, "chat.glt.ishii.landed");
            Journal.unlock(player, JournalMoment.ISHII_FLIGHT);
        }
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Parts", parts);
        tag.putInt("State", state);
        tag.putInt("FlightTicks", flightTicks);
        tag.putFloat("FlightYaw", flightYaw);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        parts = tag.getInt("Parts");
        state = Mth.clamp(tag.getInt("State"), STATE_BUILDING, STATE_LANDED);
        flightTicks = tag.getInt("FlightTicks");
        flightYaw = tag.getFloat("FlightYaw");
        entityData.set(FLYING, state == STATE_FLYING && flightTicks < BREAK_TICK);
    }
}
