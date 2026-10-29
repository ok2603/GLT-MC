package com.ok2603.glt.entity;

import com.ok2603.glt.registry.ModEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * An invisible seat on the ground. Pressing Z spawns one under the player and sits them on it.
 * It disappears as soon as nobody is sitting on it.
 */
public class SeatEntity extends Entity {
    /** Height of the seat point above the ground. Raise it if players sink into the floor. */
    private static final double SIT_HEIGHT = 0.0;

    public SeatEntity(EntityType<? extends SeatEntity> type, Level level) {
        super(type, level);
    }

    /** Z key: sit down where you stand, or stand up if you're already sitting. */
    public static void toggleSitting(ServerPlayer player) {
        if (player.getVehicle() instanceof SeatEntity) {
            player.stopRiding();
            return;
        }
        if (player.isPassenger() || player.isSpectator() || !player.onGround() || player.isInWater()) {
            return;
        }
        SeatEntity seat = ModEntities.SEAT.get().create(player.level());
        if (seat == null) {
            return;
        }
        seat.moveTo(player.getX(), player.getY(), player.getZ(), player.getYRot(), 0);
        player.level().addFreshEntity(seat);
        player.startRiding(seat, true);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide && !isVehicle()) {
            discard();
        }
    }

    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity passenger, EntityDimensions dimensions, float partialTick) {
        return new Vec3(0.0, SIT_HEIGHT, 0.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }
}
