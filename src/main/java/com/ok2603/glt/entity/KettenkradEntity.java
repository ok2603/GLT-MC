package com.ok2603.glt.entity;

import com.ok2603.glt.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * The Kettenkrad: Chito and Yuuri's half-track.
 *
 * - Right-click to get on. The first person drives, the second sits in the back.
 * - W/S to drive forward and back, A/D to turn. Shift to get off.
 * - It only drives while it has fuel. Right-click it with a Fuel Canister to refuel.
 * - Sneak + right-click shows how much fuel is left.
 * - Hit it a few times to pick it back up as an item.
 */
public class KettenkradEntity extends Entity {
    /** Fuel is counted in ticks of driving (20 ticks = 1 second). */
    public static final int FUEL_PER_CANISTER = 20 * 60 * 2; // 2 minutes of driving
    public static final int MAX_FUEL = FUEL_PER_CANISTER * 5;

    private static final float MAX_SPEED = 0.35f;
    private static final float MAX_REVERSE_SPEED = 0.12f;
    private static final float ACCELERATION = 0.02f;
    private static final float TURN_SPEED = 4.0f;

    private static final EntityDataAccessor<Integer> FUEL =
            SynchedEntityData.defineId(KettenkradEntity.class, EntityDataSerializers.INT);

    private float speed;
    private float yawDelta;
    private float damage;

    // Smooth movement on other players' screens.
    private int lerpSteps;
    private double lerpX, lerpY, lerpZ, lerpYRot, lerpXRot;

    public KettenkradEntity(EntityType<? extends KettenkradEntity> type, Level level) {
        super(type, level);
    }

    // ---------- Fuel ----------

    public int getFuel() {
        return this.entityData.get(FUEL);
    }

    public void setFuel(int fuel) {
        this.entityData.set(FUEL, Mth.clamp(fuel, 0, MAX_FUEL));
    }

    /** The engine runs (and gives off heat) while someone is driving and there is fuel. */
    public boolean isEngineRunning() {
        return getFuel() > 0 && getControllingPassenger() != null;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(FUEL, 0);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        setFuel(tag.getInt("Fuel"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Fuel", getFuel());
    }

    // ---------- Riding ----------

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // Refuel with a Fuel Canister.
        if (stack.is(ModItems.FUEL_CANISTER.get())) {
            if (getFuel() >= MAX_FUEL) {
                return InteractionResult.PASS;
            }
            if (!level().isClientSide) {
                setFuel(getFuel() + FUEL_PER_CANISTER);
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                playSound(SoundEvents.BUCKET_EMPTY, 1.0f, 0.8f);
            }
            return InteractionResult.sidedSuccess(level().isClientSide);
        }

        // Sneak + right-click: check the fuel gauge.
        if (player.isSecondaryUseActive()) {
            if (!level().isClientSide) {
                int percent = getFuel() * 100 / MAX_FUEL;
                player.displayClientMessage(Component.translatable("message.glt.kettenkrad.fuel", percent), true);
            }
            return InteractionResult.sidedSuccess(level().isClientSide);
        }

        // Get on.
        if (!player.isPassenger() && canAddPassenger(player)) {
            if (!level().isClientSide) {
                return player.startRiding(this) ? InteractionResult.CONSUME : InteractionResult.PASS;
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return getPassengers().size() < 2;
    }

    /** The first passenger is the driver. */
    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        return getFirstPassenger() instanceof Player player ? player : null;
    }

    /** Where each rider sits: driver in front, passenger in the back. */
    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity passenger, EntityDimensions dimensions, float partialTick) {
        int seat = Math.max(getPassengers().indexOf(passenger), 0);
        double forward = seat == 0 ? 0.15 : -0.55;
        return new Vec3(0.0, 0.8, forward).yRot(-getYRot() * Mth.DEG_TO_RAD);
    }

    /** Turn the riders together with the vehicle. */
    @Override
    protected void positionRider(Entity passenger, Entity.MoveFunction callback) {
        super.positionRider(passenger, callback);
        if (hasPassenger(passenger)) {
            passenger.setYRot(passenger.getYRot() + yawDelta);
            passenger.setYHeadRot(passenger.getYHeadRot() + yawDelta);
        }
    }

    // ---------- Driving ----------

    @Override
    public void tick() {
        super.tick();
        tickLerp();

        if (damage > 0) {
            damage = Math.max(0, damage - 1);
        }

        if (isControlledByLocalInstance()) {
            drive();
        } else {
            yawDelta = 0;
        }

        // The server burns fuel while the Kettenkrad is being driven and moving.
        if (!level().isClientSide && getControllingPassenger() != null) {
            double movedX = getX() - xo;
            double movedZ = getZ() - zo;
            if (movedX * movedX + movedZ * movedZ > 1.0e-4) {
                setFuel(getFuel() - 1);
            }
        }
    }

    private void drive() {
        float forwardInput = 0;
        float turnInput = 0;
        if (getControllingPassenger() instanceof Player driver && getFuel() > 0) {
            forwardInput = driver.zza; // W = positive, S = negative
            turnInput = driver.xxa;    // A = positive, D = negative
        }

        // Speed up, slow down, or roll to a stop.
        if (forwardInput > 0) {
            speed = Math.min(speed + ACCELERATION, MAX_SPEED);
        } else if (forwardInput < 0) {
            speed = Math.max(speed - ACCELERATION, -MAX_REVERSE_SPEED);
        } else {
            speed *= 0.85f;
            if (Math.abs(speed) < 0.005f) {
                speed = 0;
            }
        }

        // Steering. Turning in reverse feels natural when it's flipped.
        yawDelta = -turnInput * TURN_SPEED * (speed < 0 ? -1 : 1);
        if (speed == 0) {
            yawDelta = -turnInput * TURN_SPEED * 0.5f; // the tracks can turn on the spot, slowly
        }
        setYRot(getYRot() + yawDelta);

        // Move in the direction the Kettenkrad is facing, with gravity.
        float yawRad = getYRot() * Mth.DEG_TO_RAD;
        double motionX = -Mth.sin(yawRad) * speed;
        double motionZ = Mth.cos(yawRad) * speed;
        double motionY = onGround() ? -0.04 : getDeltaMovement().y - 0.08;
        setDeltaMovement(motionX, motionY, motionZ);
        move(MoverType.SELF, getDeltaMovement());

        // Bumping into a wall stops it.
        if (horizontalCollision) {
            speed *= 0.5f;
        }
    }

    /** Tracks can climb up one full block. */
    @Override
    public float maxUpStep() {
        return 1.0f;
    }

    @Override
    public void lerpTo(double x, double y, double z, float yRot, float xRot, int steps) {
        this.lerpX = x;
        this.lerpY = y;
        this.lerpZ = z;
        this.lerpYRot = yRot;
        this.lerpXRot = xRot;
        this.lerpSteps = 10;
    }

    private void tickLerp() {
        if (isControlledByLocalInstance()) {
            lerpSteps = 0;
            syncPacketPositionCodec(getX(), getY(), getZ());
        }
        if (lerpSteps > 0) {
            lerpPositionAndRotationStep(lerpSteps, lerpX, lerpY, lerpZ, lerpYRot, lerpXRot);
            lerpSteps--;
        }
    }

    // ---------- Breaking ----------

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isInvulnerableTo(source)) {
            return false;
        }
        if (level().isClientSide || isRemoved()) {
            return true;
        }
        damage += amount * 10;
        markHurt();
        gameEvent(GameEvent.ENTITY_DAMAGE, source.getEntity());

        boolean creative = source.getEntity() instanceof Player player && player.getAbilities().instabuild;
        if (creative || damage > 40) {
            if (!creative && level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                spawnAtLocation(ModItems.KETTENKRAD.get());
            }
            discard();
        }
        return true;
    }

    @Override
    public boolean isPickable() {
        return !isRemoved();
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(ModItems.KETTENKRAD.get());
    }
}
