package com.ok2603.glt.item;

import com.ok2603.glt.entity.KettenkradEntity;
import com.ok2603.glt.journal.Journal;
import com.ok2603.glt.journal.JournalMoment;
import com.ok2603.glt.registry.ModItems;
import com.ok2603.glt.registry.ModSounds;
import com.ok2603.glt.role.Roles;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;

import java.util.List;

/**
 * Yuuri's rifle. Right-click to fire one Rifle Cartridge. Ammo is very rare, and there's
 * almost nothing to shoot, which is faithful to the anime.
 *
 * Yuuri is a good shot (also from the Kettenkrad's back seat). Chito can barely aim:
 * her shots go wide. Sneaking steadies your aim a little. The driver can't shoot.
 */
public class RifleItem extends Item {
    private static final double RANGE = 64.0;
    private static final float DAMAGE = 10.0f;
    /** Ticks between shots: working the bolt takes time. */
    private static final int COOLDOWN = 30;

    public RifleItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack rifle = player.getItemInHand(hand);
        if (player.getVehicle() instanceof KettenkradEntity kettenkrad && kettenkrad.getControllingPassenger() == player) {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.translatable("message.glt.rifle.driving"), true);
            }
            return InteractionResultHolder.fail(rifle);
        }

        ItemStack ammo = findAmmo(player);
        if (ammo.isEmpty() && !player.getAbilities().instabuild) {
            if (!level.isClientSide) {
                level.playSound(null, player.blockPosition(), ModSounds.RIFLE_EMPTY.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
                player.displayClientMessage(Component.translatable("message.glt.rifle.no_ammo"), true);
            }
            player.getCooldowns().addCooldown(this, 10);
            return InteractionResultHolder.fail(rifle);
        }

        if (level instanceof ServerLevel serverLevel) {
            shoot(serverLevel, player);
            if (!player.getAbilities().instabuild) {
                ammo.shrink(1);
            }
            rifle.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
            if (player instanceof ServerPlayer serverPlayer) {
                Journal.unlock(serverPlayer, JournalMoment.RIFLE);
            }
        }
        player.getCooldowns().addCooldown(this, COOLDOWN);
        return InteractionResultHolder.sidedSuccess(rifle, level.isClientSide);
    }

    private static ItemStack findAmmo(Player player) {
        if (player.getOffhandItem().is(ModItems.RIFLE_CARTRIDGE.get())) {
            return player.getOffhandItem();
        }
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(ModItems.RIFLE_CARTRIDGE.get())) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private static void shoot(ServerLevel level, Player player) {
        // Aim: a random wobble that depends on who is holding the rifle.
        float spread = Roles.rifleSpread(player) * (player.isShiftKeyDown() ? 0.5f : 1.0f);
        float yaw = player.getYRot() + (level.random.nextFloat() * 2 - 1) * spread;
        float pitch = Mth.clamp(player.getXRot() + (level.random.nextFloat() * 2 - 1) * spread, -90, 90);
        Vec3 direction = Vec3.directionFromRotation(pitch, yaw);
        Vec3 start = player.getEyePosition();
        Vec3 end = start.add(direction.scale(RANGE));

        BlockHitResult blockHit = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        if (blockHit.getType() != HitResult.Type.MISS) {
            end = blockHit.getLocation();
        }
        Entity vehicle = player.getVehicle();
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(level, player, start, end,
                new AABB(start, end).inflate(1.0),
                entity -> !entity.isSpectator() && entity.isPickable() && entity != vehicle && entity.getVehicle() != vehicle);

        if (entityHit != null) {
            end = entityHit.getLocation();
            entityHit.getEntity().hurt(level.damageSources().playerAttack(player), DAMAGE);
        } else if (blockHit.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = blockHit.getBlockPos();
            BlockState state = level.getBlockState(pos);
            if (state.is(Tags.Blocks.GLASS_BLOCKS) || state.is(Tags.Blocks.GLASS_PANES)) {
                level.destroyBlock(pos, false, player);
            } else {
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state),
                        end.x, end.y, end.z, 8, 0.05, 0.05, 0.05, 0.1);
            }
        }

        // Muzzle smoke, and a faint trail along the shot.
        Vec3 muzzle = start.add(direction.scale(0.8)).add(0, -0.15, 0);
        level.sendParticles(ParticleTypes.SMOKE, muzzle.x, muzzle.y, muzzle.z, 4, 0.03, 0.03, 0.03, 0.01);
        double length = end.distanceTo(start);
        for (double d = 2; d < length; d += 2.5) {
            Vec3 point = start.add(direction.scale(d));
            level.sendParticles(ParticleTypes.WHITE_SMOKE, point.x, point.y, point.z, 1, 0, 0, 0, 0);
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.RIFLE_SHOT.get(), SoundSource.PLAYERS, 3.0f,
                0.95f + level.random.nextFloat() * 0.1f);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.RIFLE_BOLT.get(), SoundSource.PLAYERS, 0.8f, 1.0f);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.glt.yuuris_rifle.tooltip").withStyle(ChatFormatting.GRAY));
    }
}
