package com.ok2603.glt.entity;

import com.ok2603.glt.journal.Journal;
import com.ok2603.glt.journal.JournalMoment;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.entity.animal.Cod;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * An old maintenance robot that still does its job. Most of them just tend the pipes and
 * ignore you. The one at a fish farm ("keeper") looks after the fish: it will hand you one fish
 * a day, but if you take more than a few yourself it gets upset, beeps at you and pushes you away.
 */
public class RobotEntity extends PathfinderMob {
    /** Fish you can take from a farm before its keeper gets upset. */
    public static final int FISH_ALLOWED = 3;
    private static final long UPSET_TICKS = 24000L * 3;
    private static final int TEND_RADIUS = 12;

    private boolean keeper;
    @Nullable
    private BlockPos home;
    private int fishTaken;
    private long upsetUntil;
    private long lastFishDay = -1;

    public RobotEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 40.0)
                .add(Attributes.ARMOR, 10.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.MOVEMENT_SPEED, 0.15);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(3, new MoveTowardsRestrictionGoal(this, 0.8));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.6));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0f));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    /** Gives the robot a place to look after. Keepers look after a fish farm. */
    public void setHome(BlockPos home, boolean keeper) {
        this.home = home.immutable();
        this.keeper = keeper;
        restrictTo(this.home, TEND_RADIUS);
    }

    public boolean isKeeper() {
        return keeper;
    }

    @Nullable
    public BlockPos home() {
        return home;
    }

    public boolean isUpset() {
        return level().getGameTime() < upsetUntil;
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
        if (level().isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!keeper) {
            beep(false);
            message(player, "message.glt.robot.busy." + random.nextInt(3));
            return InteractionResult.CONSUME;
        }
        if (isUpset()) {
            beep(true);
            message(player, "message.glt.robot.upset");
            return InteractionResult.CONSUME;
        }
        long day = level().getDayTime() / 24000L;
        if (day == lastFishDay) {
            beep(false);
            message(player, "message.glt.robot.tomorrow");
            return InteractionResult.CONSUME;
        }
        lastFishDay = day;
        beep(false);
        ItemStack fish = new ItemStack(Items.COD);
        if (!player.getInventory().add(fish)) {
            player.drop(fish, false);
        }
        message(player, "message.glt.robot.gift");
        if (player instanceof ServerPlayer serverPlayer) {
            Journal.unlock(serverPlayer, JournalMoment.ROBOT_FISH);
        }
        return InteractionResult.CONSUME;
    }

    /** Called when a player kills or catches a fish near this keeper's farm. */
    public void onFishTaken(Player player) {
        if (!keeper) {
            return;
        }
        fishTaken++;
        if (fishTaken == FISH_ALLOWED) {
            beep(false);
            message(player, "message.glt.robot.counting");
        } else if (fishTaken > FISH_ALLOWED) {
            fishTaken = 0;
            upsetUntil = level().getGameTime() + UPSET_TICKS;
            beep(true);
            message(player, "message.glt.robot.angry");
            push(player);
        }
    }

    private void push(Player player) {
        player.knockback(1.2, getX() - player.getX(), getZ() - player.getZ());
        player.hurtMarked = true; // send the push to the player's game
    }

    private void beep(boolean angry) {
        level().playSound(null, blockPosition(), angry ? SoundEvents.NOTE_BLOCK_BASS.value() : SoundEvents.NOTE_BLOCK_BIT.value(),
                SoundSource.NEUTRAL, 1.0f, angry ? 0.5f : 1.5f + random.nextFloat() * 0.3f);
    }

    private static void message(Player player, String key) {
        player.displayClientMessage(Component.translatable(key).withStyle(ChatFormatting.GRAY), true);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!(level() instanceof ServerLevel level) || home == null) {
            return;
        }
        if (!hasRestriction()) {
            restrictTo(home, TEND_RADIUS);
        }
        if (!keeper) {
            if (tickCount % 200 == 0 && random.nextInt(3) == 0) {
                beep(false);
            }
            return;
        }
        // Upset keepers push anyone who comes close to the pool.
        if (isUpset() && tickCount % 40 == 0) {
            for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, getBoundingBox().inflate(3))) {
                beep(true);
                push(player);
            }
        }
        // Tending the farm: now and then it puts a new fish in the pool, up to a few.
        if (tickCount % 1200 == 0 && level.getFluidState(home).isSource()) {
            int fish = level.getEntitiesOfClass(AbstractFish.class, new net.minecraft.world.phys.AABB(home).inflate(TEND_RADIUS)).size();
            if (fish < 6) {
                Cod cod = EntityType.COD.create(level);
                if (cod != null) {
                    cod.moveTo(home.getX() + 0.5, home.getY() + 0.2, home.getZ() + 0.5, random.nextFloat() * 360f, 0f);
                    cod.setFromBucket(true);
                    level.addFreshEntity(cod);
                }
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Keeper", keeper);
        if (home != null) {
            tag.put("Home", NbtUtils.writeBlockPos(home));
        }
        tag.putInt("FishTaken", fishTaken);
        tag.putLong("UpsetUntil", upsetUntil);
        tag.putLong("LastFishDay", lastFishDay);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        keeper = tag.getBoolean("Keeper");
        home = NbtUtils.readBlockPos(tag, "Home").orElse(null);
        fishTaken = tag.getInt("FishTaken");
        upsetUntil = tag.getLong("UpsetUntil");
        lastFishDay = tag.contains("LastFishDay") ? tag.getLong("LastFishDay") : -1;
        if (home != null) {
            restrictTo(home, TEND_RADIUS);
        }
    }
}
