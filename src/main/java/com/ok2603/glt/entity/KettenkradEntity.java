package com.ok2603.glt.entity;

import com.ok2603.glt.registry.ModItems;
import com.ok2603.glt.role.Role;
import com.ok2603.glt.role.Roles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * The Kettenkrad: Chito and Yuuri's half-track.
 *
 * - Right-click to get on. The first rider drives, the second sits in the back.
 *   Chito drives faster; Yuuri is slow to speed up and to steer (see Roles).
 * - W/S to drive forward and back, A/D to turn. Shift to get off.
 * - It only drives while it has fuel. Right-click it with a Fuel Canister to refuel.
 * - Sneak + right-click shows how much fuel is left.
 * - When it's stopped, left-click it to set up camp: a campfire appears on the side you're standing on.
 * - Sneak + left-click a few times to pick it back up as an item.
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

    /** Whoever got on first drives. */
    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        return getFirstPassenger() instanceof Player player ? player : null;
    }

    /** Where each rider sits: driver in front, passenger in the back. */
    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity passenger, EntityDimensions dimensions, float partialTick) {
        boolean frontSeat = getPassengers().indexOf(passenger) <= 0;
        // Driver on the seat behind the handlebars, passenger in the rear bed.
        double forward = frontSeat ? 0.22 : -0.62;
        double height = frontSeat ? 0.6 : 0.55;
        return new Vec3(0.0, height, forward).yRot(-getYRot() * Mth.DEG_TO_RAD);
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
            // Chito knows how to drive gently, so she uses less fuel.
            boolean chitoSaves = getControllingPassenger() instanceof Player driver
                    && Roles.get(driver) == Role.CHITO
                    && tickCount % Roles.CHITO_FUEL_SAVING == 0;
            if (movedX * movedX + movedZ * movedZ > 1.0e-4 && !chitoSaves) {
                setFuel(getFuel() - 1);
            }
        }
    }

    private void drive() {
        float forwardInput = 0;
        float turnInput = 0;
        float maxSpeed = MAX_SPEED;
        float acceleration = ACCELERATION;
        float turnSpeed = TURN_SPEED;
        if (getControllingPassenger() instanceof Player driver) {
            if (getFuel() > 0) {
                forwardInput = driver.zza; // W = positive, S = negative
                turnInput = driver.xxa;    // A = positive, D = negative
            }
            // Chito drives faster; Yuuri is slow to speed up and to steer.
            maxSpeed += Roles.extraDrivingSpeed(driver);
            acceleration *= Roles.accelerationMultiplier(driver);
            turnSpeed *= Roles.steeringMultiplier(driver);
        }

        // Speed up, slow down, or roll to a stop.
        if (forwardInput > 0) {
            speed = Math.min(speed + acceleration, maxSpeed);
        } else if (forwardInput < 0) {
            speed = Math.max(speed - acceleration, -MAX_REVERSE_SPEED);
        } else {
            speed *= 0.85f;
            if (Math.abs(speed) < 0.005f) {
                speed = 0;
            }
        }

        // Steering. Turning in reverse feels natural when it's flipped.
        yawDelta = -turnInput * turnSpeed * (speed < 0 ? -1 : 1);
        if (speed == 0) {
            yawDelta = -turnInput * turnSpeed * 0.5f; // the tracks can turn on the spot, slowly
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

    // ---------- Camping ----------

    /** How far from the middle of the Kettenkrad the campfire goes, in blocks. */
    private static final double CAMP_DISTANCE = 2.0;

    /** Nobody is driving it and it isn't rolling. */
    public boolean isStopped() {
        return getControllingPassenger() == null && getDeltaMovement().horizontalDistanceSqr() < 1.0e-4;
    }

    /**
     * Left-click (without sneaking) sets up camp instead of damaging the Kettenkrad.
     * Sneak + left-click still hits it, so it can be picked up.
     */
    @Override
    public boolean skipAttackInteraction(Entity attacker) {
        if (attacker instanceof Player player && !player.isShiftKeyDown()) {
            if (!level().isClientSide && isStopped()) {
                setUpCamp(player);
            }
            return true;
        }
        return false;
    }

    private void setUpCamp(Player player) {
        if (campfireNearby()) {
            player.displayClientMessage(Component.translatable("message.glt.camp.already"), true);
            return;
        }

        // Which side of the Kettenkrad is the player on? "right" is the driver's right.
        float yawRad = getYRot() * Mth.DEG_TO_RAD;
        Vec3 right = new Vec3(-Mth.cos(yawRad), 0, -Mth.sin(yawRad));
        double side = player.position().subtract(position()).dot(right) >= 0 ? 1 : -1;
        BlockPos spot = findCampSpot(BlockPos.containing(position().add(right.scale(side * CAMP_DISTANCE))));
        if (spot == null) {
            player.displayClientMessage(Component.translatable("message.glt.camp.no_room"), true);
            return;
        }

        level().setBlock(spot, Blocks.CAMPFIRE.defaultBlockState()
                .setValue(CampfireBlock.FACING, Direction.fromYRot(getYRot())), 3);
        level().playSound(null, spot, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1.0f, 1.0f);
        level().gameEvent(player, GameEvent.BLOCK_PLACE, spot);
        player.displayClientMessage(Component.translatable("message.glt.camp.set_up"), true);
    }

    /** Finds a free spot with solid ground near the given position (a little up or down is fine). */
    @Nullable
    private BlockPos findCampSpot(BlockPos start) {
        for (int dy : new int[]{0, 1, -1, 2, -2}) {
            BlockPos pos = start.above(dy);
            BlockPos below = pos.below();
            if (level().getBlockState(pos).canBeReplaced()
                    && level().getFluidState(pos).isEmpty()
                    && level().getBlockState(below).isFaceSturdy(level(), below, Direction.UP)) {
                return pos;
            }
        }
        return null;
    }

    private boolean campfireNearby() {
        BlockPos center = blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-4, -2, -4), center.offset(4, 2, 4))) {
            if (level().getBlockState(pos).is(BlockTags.CAMPFIRES)) {
                return true;
            }
        }
        return false;
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
