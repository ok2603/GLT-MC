package com.ok2603.glt.block;

import com.ok2603.glt.config.GLTConfig;
import com.ok2603.glt.journal.Journal;
import com.ok2603.glt.journal.JournalMoment;
import com.ok2603.glt.registry.ModBlockEntities;
import com.ok2603.glt.registry.ModBlocks;
import com.ok2603.glt.registry.ModItems;
import com.ok2603.glt.role.Roles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundMoveVehiclePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The brains of an elevator. The console on the lower floor is the "controller": it knows where
 * the platform is and moves it. The console on the upper floor only remembers where the
 * controller is. Both are set up when the city is generated (see CityFeature).
 *
 * Moving works by shifting the 5x5 platform one block every STEP_TICKS ticks and lifting
 * everything standing on it (players, Nuko, the Kettenkrad with its riders) by the same block.
 */
public class ElevatorConsoleBlockEntity extends BlockEntity {
    /** Ticks per block of travel. 56 blocks take about 8 seconds. */
    public static final int STEP_TICKS = 3;
    /** How much headroom above the platform counts as "standing on it". */
    private static final int RIDE_HEIGHT = 4;

    // --- Controller data ---
    private boolean controller;
    private int minX;
    private int minZ;
    private int size;
    private int bottomY;
    private int topY;
    private int platformY;
    /** +1 going up, -1 going down, 0 standing still. */
    private int direction;
    private int timer;

    // --- Upper console data ---
    @Nullable
    private BlockPos link;

    public ElevatorConsoleBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ELEVATOR_CONSOLE.get(), pos, state);
    }

    /** Makes this the controller of a platform resting at bottomY that rides up to topY. */
    public void setupController(int minX, int minZ, int size, int bottomY, int topY) {
        this.controller = true;
        this.minX = minX;
        this.minZ = minZ;
        this.size = size;
        this.bottomY = bottomY;
        this.topY = topY;
        this.platformY = bottomY;
        this.direction = 0;
        this.link = null;
        setChanged();
    }

    /** Makes this console a remote for the controller at that position. */
    public void linkTo(BlockPos controllerPos) {
        this.controller = false;
        this.link = controllerPos.immutable();
        setChanged();
    }

    @Nullable
    private ElevatorConsoleBlockEntity findController() {
        if (controller) {
            return this;
        }
        if (link != null && level != null && level.isLoaded(link)
                && level.getBlockEntity(link) instanceof ElevatorConsoleBlockEntity other && other.controller) {
            return other;
        }
        return null;
    }

    public boolean isMoving() {
        return direction != 0;
    }

    public int platformY() {
        return platformY;
    }

    // ---------- Using it ----------

    public void use(ServerPlayer player) {
        ElevatorConsoleBlockEntity c = findController();
        if (c == null) {
            player.displayClientMessage(Component.translatable("message.glt.elevator.dead"), true);
            return;
        }
        if (c.isMoving()) {
            player.displayClientMessage(Component.translatable("message.glt.elevator.moving"), true);
            return;
        }
        // Which floor is this console on? The platform goes to the other one,
        // or comes here if it is somewhere else.
        boolean onTop = Math.abs(worldPosition.getY() - (c.topY + 1)) < Math.abs(worldPosition.getY() - (c.bottomY + 1));
        int here = onTop ? c.topY : c.bottomY;
        int target = c.platformY == here ? (onTop ? c.bottomY : c.topY) : here;
        boolean goingUp = target > c.platformY;

        if (goingUp) {
            if (!Roles.canRepair(player)) {
                player.displayClientMessage(Component.translatable("message.glt.elevator.yuuri"), true);
                level.playSound(null, worldPosition, SoundEvents.STONE_BUTTON_CLICK_ON, SoundSource.BLOCKS, 0.6f, 1.6f);
                return;
            }
            if (!takeFuel(player)) {
                player.displayClientMessage(Component.translatable("message.glt.elevator.no_fuel"), true);
                level.playSound(null, worldPosition, SoundEvents.STONE_BUTTON_CLICK_ON, SoundSource.BLOCKS, 0.6f, 0.8f);
                return;
            }
            player.displayClientMessage(Component.translatable("message.glt.elevator.up"), true);
        } else {
            player.displayClientMessage(Component.translatable("message.glt.elevator.down"), true);
        }
        level.playSound(null, worldPosition, SoundEvents.PISTON_EXTEND, SoundSource.BLOCKS, 1.0f, 0.5f);
        c.start(goingUp ? 1 : -1);
    }

    /** Takes one fuel canister from the player (creative players ride for free). */
    private static boolean takeFuel(ServerPlayer player) {
        if (player.getAbilities().instabuild || !GLTConfig.get(GLTConfig.ELEVATOR_NEEDS_FUEL)) {
            return true;
        }
        var inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.is(ModItems.FUEL_CANISTER.get())) {
                stack.shrink(1);
                return true;
            }
        }
        return false;
    }

    /** Starts moving (only on the controller). Public so tests can drive it. */
    public void start(int direction) {
        if (!controller) {
            return;
        }
        this.direction = direction;
        this.timer = 0;
        setChanged();
    }

    // ---------- Moving ----------

    public static void serverTick(Level level, BlockPos pos, BlockState state, ElevatorConsoleBlockEntity be) {
        if (!be.controller || be.direction == 0 || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (++be.timer < STEP_TICKS) {
            return;
        }
        be.timer = 0;
        be.step(serverLevel);
    }

    private void step(ServerLevel level) {
        int newY = platformY + direction;
        BlockState platform = ModBlocks.ELEVATOR_PLATFORM.get().defaultBlockState();

        // Something in the way (a block someone built in the shaft)? Stop.
        for (int x = minX; x < minX + size; x++) {
            for (int z = minZ; z < minZ + size; z++) {
                BlockState there = level.getBlockState(new BlockPos(x, newY, z));
                if (!there.isAir() && !there.is(platform.getBlock())) {
                    direction = 0;
                    setChanged();
                    level.playSound(null, worldPosition, SoundEvents.ANVIL_LAND, SoundSource.BLOCKS, 0.6f, 0.6f);
                    return;
                }
            }
        }

        // Who is riding: everything whose feet are on the platform or just above it.
        AABB riders = new AABB(minX, platformY + 0.5, minZ, minX + size, platformY + 1 + RIDE_HEIGHT, minZ + size);
        List<Entity> entities = level.getEntities((Entity) null, riders, e -> !e.isPassenger() && !e.isSpectator());

        for (int x = minX; x < minX + size; x++) {
            for (int z = minZ; z < minZ + size; z++) {
                level.setBlock(new BlockPos(x, newY, z), platform, 2);
                level.setBlock(new BlockPos(x, platformY, z), Blocks.AIR.defaultBlockState(), 2);
            }
        }
        for (Entity entity : entities) {
            entity.teleportTo(entity.getX(), entity.getY() + direction, entity.getZ());
            entity.resetFallDistance();
            // A Kettenkrad driven by a player is moved by that player's game, so tell it too.
            LivingEntity driver = entity.getControllingPassenger();
            if (driver instanceof ServerPlayer player) {
                player.connection.send(new ClientboundMoveVehiclePacket(entity));
            }
            for (Entity passenger : entity.getIndirectPassengers()) {
                passenger.resetFallDistance();
            }
        }
        platformY = newY;
        if (platformY % 6 == 0) {
            level.playSound(null, minX + size / 2.0, platformY + 1, minZ + size / 2.0,
                    SoundEvents.CHAIN_PLACE, SoundSource.BLOCKS, 0.8f, 0.5f);
        }

        int target = direction > 0 ? topY : bottomY;
        if (platformY == target) {
            direction = 0;
            level.playSound(null, minX + size / 2.0, platformY + 1, minZ + size / 2.0,
                    SoundEvents.ANVIL_LAND, SoundSource.BLOCKS, 0.5f, 0.5f);
            for (Entity entity : level.getEntitiesOfClass(ServerPlayer.class, riders.move(0, 1, 0).inflate(1))) {
                if (entity instanceof ServerPlayer player) {
                    Journal.unlock(player, JournalMoment.ELEVATOR);
                }
            }
        }
        setChanged();
    }

    // ---------- Saving ----------

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("Controller", controller);
        if (controller) {
            tag.putInt("MinX", minX);
            tag.putInt("MinZ", minZ);
            tag.putInt("Size", size);
            tag.putInt("BottomY", bottomY);
            tag.putInt("TopY", topY);
            tag.putInt("PlatformY", platformY);
            tag.putInt("Direction", direction);
        } else if (link != null) {
            tag.put("Link", NbtUtils.writeBlockPos(link));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        controller = tag.getBoolean("Controller");
        minX = tag.getInt("MinX");
        minZ = tag.getInt("MinZ");
        size = tag.getInt("Size");
        bottomY = tag.getInt("BottomY");
        topY = tag.getInt("TopY");
        platformY = tag.getInt("PlatformY");
        direction = tag.getInt("Direction");
        link = NbtUtils.readBlockPos(tag, "Link").orElse(null);
    }
}
