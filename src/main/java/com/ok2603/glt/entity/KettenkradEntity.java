package com.ok2603.glt.entity;

import com.ok2603.glt.registry.ModBlocks;
import com.ok2603.glt.registry.ModItems;
import com.ok2603.glt.role.Role;
import com.ok2603.glt.role.Roles;
import com.ok2603.glt.network.KettenkradCrashPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.PacketDistributor;
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
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
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
 *   Out of fuel, Yuuri can still push it along slowly.
 * - The headlight shines while the engine runs.
 * - Sneak + right-click opens its storage (27 slots). It's your whole home on the road.
 * - Crashes, big drops and long drives wear it down. Chito repairs it with Scrap Parts.
 *   Below 25% it smokes and slows down; at 0% it won't drive at all.
 * - Right-click it with a Bedroll to lay the bedroll out next to it.
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

    /** Condition from 0 (broken) to MAX_CONDITION (like new). */
    public static final int MAX_CONDITION = 100;
    private static final int REPAIR_PER_SCRAP = 20;
    /** Driving this many ticks wears the Kettenkrad down by 1%. */
    private static final int WEAR_TICKS = 20 * 90;
    /** Below this condition it smokes and loses speed. */
    public static final int POOR_CONDITION = 25;
    /** How fast Yuuri can push it with no fuel, in blocks per tick. */
    private static final float PUSH_SPEED = 0.06f;
    /** How far ahead the headlight shines, in blocks. */
    private static final double HEADLIGHT_DISTANCE = 5.0;

    private static final EntityDataAccessor<Integer> FUEL =
            SynchedEntityData.defineId(KettenkradEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> CONDITION =
            SynchedEntityData.defineId(KettenkradEntity.class, EntityDataSerializers.INT);

    private final KettenkradStorage storage = new KettenkradStorage(this);
    /** Where the invisible light block of the headlight currently is. */
    @Nullable
    private BlockPos headlightPos;
    private int wearTicks;
    private int crashCooldown;

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
        return getFuel() > 0 && !isBroken() && getControllingPassenger() != null;
    }

    // ---------- Condition ----------

    public int getCondition() {
        return this.entityData.get(CONDITION);
    }

    public void setCondition(int condition) {
        this.entityData.set(CONDITION, Mth.clamp(condition, 0, MAX_CONDITION));
    }

    public boolean isBroken() {
        return getCondition() <= 0;
    }

    /** Wears the Kettenkrad down (server only). */
    public void damageCondition(int amount) {
        if (amount <= 0 || level().isClientSide) {
            return;
        }
        boolean wasBroken = isBroken();
        setCondition(getCondition() - amount);
        playSound(SoundEvents.ANVIL_LAND, 0.4f, 1.6f);
        if (!wasBroken && isBroken() && getControllingPassenger() instanceof Player driver) {
            driver.displayClientMessage(Component.translatable("message.glt.kettenkrad.broken"), true);
        }
    }

    public KettenkradStorage getStorage() {
        return storage;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(FUEL, 0);
        builder.define(CONDITION, MAX_CONDITION);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        setFuel(tag.getInt("Fuel"));
        setCondition(tag.contains("Condition") ? tag.getInt("Condition") : MAX_CONDITION);
        storage.removeAllItems();
        storage.fromTag(tag.getList("Storage", Tag.TAG_COMPOUND), registryAccess());
        headlightPos = NbtUtils.readBlockPos(tag, "Headlight").orElse(null);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Fuel", getFuel());
        tag.putInt("Condition", getCondition());
        tag.put("Storage", storage.createTag(registryAccess()));
        if (headlightPos != null) {
            tag.put("Headlight", NbtUtils.writeBlockPos(headlightPos));
        }
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

        // Repair with Scrap Parts. That's Chito's job.
        if (stack.is(ModItems.SCRAP_PARTS.get())) {
            if (getCondition() >= MAX_CONDITION) {
                return InteractionResult.PASS;
            }
            if (!level().isClientSide) {
                if (!Roles.canRepair(player)) {
                    player.displayClientMessage(Component.translatable("message.glt.kettenkrad.cant_repair"), true);
                    return InteractionResult.CONSUME;
                }
                setCondition(getCondition() + REPAIR_PER_SCRAP);
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                playSound(SoundEvents.ANVIL_USE, 0.5f, 1.4f);
                player.displayClientMessage(Component.translatable("message.glt.kettenkrad.repaired", getCondition()), true);
            }
            return InteractionResult.sidedSuccess(level().isClientSide);
        }

        // Lay a bedroll out next to it.
        if (stack.is(ModItems.BEDROLL.get())) {
            if (!level().isClientSide) {
                layOutBedroll(player, stack);
            }
            return InteractionResult.sidedSuccess(level().isClientSide);
        }

        // Sneak + right-click: open the storage.
        if (player.isSecondaryUseActive()) {
            if (!level().isClientSide) {
                player.openMenu(new SimpleMenuProvider(
                        (id, inventory, p) -> ChestMenu.threeRows(id, inventory, storage),
                        Component.translatable("container.glt.kettenkrad")));
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
        if (crashCooldown > 0) {
            crashCooldown--;
        }

        // A worn-out engine smokes.
        if (level().isClientSide && getCondition() < POOR_CONDITION && random.nextInt(isBroken() ? 3 : 6) == 0) {
            float yawRad = getYRot() * Mth.DEG_TO_RAD;
            level().addParticle(isBroken() ? ParticleTypes.LARGE_SMOKE : ParticleTypes.SMOKE,
                    getX() - Mth.sin(yawRad) * 0.6, getY() + 0.9, getZ() + Mth.cos(yawRad) * 0.6, 0, 0.03, 0);
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
            boolean moving = movedX * movedX + movedZ * movedZ > 1.0e-4;
            if (moving && isEngineRunning() && !chitoSaves) {
                setFuel(getFuel() - 1);
            }
            // Long drives slowly wear it down.
            if (moving && isEngineRunning() && ++wearTicks >= WEAR_TICKS) {
                wearTicks = 0;
                setCondition(getCondition() - 1);
            }
        }

        if (!level().isClientSide && tickCount % 4 == 0) {
            updateHeadlight();
        }
    }

    private void drive() {
        float forwardInput = 0;
        float turnInput = 0;
        float maxSpeed = MAX_SPEED;
        float acceleration = ACCELERATION;
        float turnSpeed = TURN_SPEED;
        if (getControllingPassenger() instanceof Player driver) {
            if (getFuel() > 0 && !isBroken()) {
                forwardInput = driver.zza; // W = positive, S = negative
                turnInput = driver.xxa;    // A = positive, D = negative
                // Chito drives faster; Yuuri is slow to speed up and to steer.
                maxSpeed += Roles.extraDrivingSpeed(driver);
                acceleration *= Roles.accelerationMultiplier(driver);
                turnSpeed *= Roles.steeringMultiplier(driver);
                // A worn-out engine can't reach full speed.
                if (getCondition() < POOR_CONDITION) {
                    maxSpeed *= 0.6f;
                }
            } else if (Roles.canPush(driver)) {
                // No fuel (or broken): Yuuri gets off the seat and pushes. Slowly.
                forwardInput = Math.max(0, driver.zza);
                turnInput = driver.xxa;
                maxSpeed = PUSH_SPEED;
                acceleration = 0.01f;
                turnSpeed *= 0.4f;
            }
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

        // Bumping into a wall stops it. A hard crash damages it.
        if (horizontalCollision) {
            if (Math.abs(speed) > 0.22f && crashCooldown == 0 && level().isClientSide) {
                crashCooldown = 20;
                PacketDistributor.sendToServer(new KettenkradCrashPayload(getId(), Math.round(Math.abs(speed) * 20)));
            }
            speed *= 0.5f;
        }
    }

    /** Big drops hurt the Kettenkrad (the client that drives it reports this to the server). */
    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        if (fallDistance > 4 && level().isClientSide && isControlledByLocalInstance()) {
            PacketDistributor.sendToServer(new KettenkradCrashPayload(getId(), Math.round((fallDistance - 4) * 4)));
        }
        return false;
    }

    // ---------- Headlight ----------

    /** Moves the invisible light block in front of the Kettenkrad while the engine runs. */
    private void updateHeadlight() {
        BlockPos target = null;
        if (isEngineRunning()) {
            float yawRad = getYRot() * Mth.DEG_TO_RAD;
            BlockPos ahead = BlockPos.containing(getX() - Mth.sin(yawRad) * HEADLIGHT_DISTANCE, getY() + 1,
                    getZ() + Mth.cos(yawRad) * HEADLIGHT_DISTANCE);
            for (BlockPos candidate : new BlockPos[]{ahead, ahead.above(), ahead.below()}) {
                if (candidate.equals(headlightPos) || level().getBlockState(candidate).isAir()) {
                    target = candidate;
                    break;
                }
            }
        }
        if (target != null && target.equals(headlightPos)) {
            return;
        }
        removeHeadlight();
        if (target != null) {
            level().setBlock(target, Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, 14), 3);
            headlightPos = target;
        }
    }

    private void removeHeadlight() {
        if (headlightPos != null) {
            BlockState state = level().getBlockState(headlightPos);
            if (state.is(Blocks.LIGHT)) {
                level().setBlock(headlightPos, Blocks.AIR.defaultBlockState(), 3);
            }
            headlightPos = null;
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        if (!level().isClientSide) {
            removeHeadlight();
        }
        super.remove(reason);
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

    /** Puts a bedroll on the ground on the player's side, a bit behind the campfire spot. */
    private void layOutBedroll(Player player, ItemStack stack) {
        float yawRad = getYRot() * Mth.DEG_TO_RAD;
        Vec3 right = new Vec3(-Mth.cos(yawRad), 0, -Mth.sin(yawRad));
        Vec3 forward = new Vec3(-Mth.sin(yawRad), 0, Mth.cos(yawRad));
        double side = player.position().subtract(position()).dot(right) >= 0 ? 1 : -1;
        Vec3 spotVec = position().add(right.scale(side * 1.6)).add(forward.scale(-1.6));
        BlockPos spot = findCampSpot(BlockPos.containing(spotVec));
        if (spot == null) {
            player.displayClientMessage(Component.translatable("message.glt.bedroll.no_room"), true);
            return;
        }
        // The pillow end points toward the Kettenkrad's front.
        level().setBlock(spot, ModBlocks.BEDROLL.get().defaultBlockState()
                .setValue(HorizontalDirectionalBlock.FACING, Direction.fromYRot(getYRot())), 3);
        level().playSound(null, spot, SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, 1.0f, 1.0f);
        level().gameEvent(player, GameEvent.BLOCK_PLACE, spot);
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
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
            if (level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                if (!creative) {
                    // The item remembers the fuel and condition.
                    ItemStack item = new ItemStack(ModItems.KETTENKRAD.get());
                    CompoundTag saved = new CompoundTag();
                    saved.putInt("Fuel", getFuel());
                    saved.putInt("Condition", getCondition());
                    item.set(DataComponents.CUSTOM_DATA, CustomData.of(saved));
                    spawnAtLocation(item);
                }
                Containers.dropContents(level(), this, storage);
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
