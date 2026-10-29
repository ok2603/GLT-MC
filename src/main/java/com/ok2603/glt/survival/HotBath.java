package com.ok2603.glt.survival;

import com.ok2603.glt.GLTMod;
import com.ok2603.glt.journal.Journal;
import com.ok2603.glt.journal.JournalMoment;
import com.ok2603.glt.registry.ModBlocks;
import com.ok2603.glt.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * The bath in the hot pipes.
 *
 * Water within a few blocks of a Leaking Hot Pipe is warm. Sitting in it warms you right up
 * and slowly heals you. Drop Chito's coat into the warm water to wash it: for a while
 * afterwards it keeps you even warmer ("dry and warm").
 */
@EventBusSubscriber(modid = GLTMod.MODID)
public class HotBath {
    private static final int PIPE_RANGE = 3;
    /** How long a washed coat stays extra warm (10 minutes). */
    private static final long WASHED_TICKS = 20 * 60 * 10;
    private static final int WASH_TIME = 20 * 5;

    /** True if this spot is water warmed by a leaking pipe. */
    public static boolean isWarmWater(Level level, BlockPos pos) {
        if (!level.getFluidState(pos).isSource() && level.getFluidState(pos).isEmpty()) {
            return false;
        }
        for (BlockPos near : BlockPos.betweenClosed(pos.offset(-PIPE_RANGE, -2, -PIPE_RANGE), pos.offset(PIPE_RANGE, 2, PIPE_RANGE))) {
            if (level.getBlockState(near).is(ModBlocks.LEAKING_HOT_PIPE.get())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isBathing(ServerPlayer player) {
        return player.isInWater() && isWarmWater(player.level(), player.blockPosition());
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % 20 != 0 || !isBathing(player)) {
            return;
        }
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 0, true, false, true));
        player.serverLevel().sendParticles(ParticleTypes.CLOUD, player.getX(), player.getY() + 1.0, player.getZ(),
                2, 0.4, 0.1, 0.4, 0.01);
        Journal.unlock(player, JournalMoment.BATH);
    }

    /** Washing the coat. */
    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof ItemEntity item) || !(item.level() instanceof ServerLevel level)
                || item.tickCount % 20 != 0 || !item.getItem().is(ModItems.CHITOS_COAT.get()) || !item.isInWater()) {
            return;
        }
        if (!isWarmWater(level, item.blockPosition())) {
            return;
        }
        CompoundTag soak = item.getPersistentData();
        int soaked = soak.getInt("glt_soaked") + 20;
        soak.putInt("glt_soaked", soaked);
        level.sendParticles(ParticleTypes.BUBBLE, item.getX(), item.getY() + 0.2, item.getZ(), 4, 0.2, 0.1, 0.2, 0.02);
        if (soaked >= WASH_TIME) {
            soak.remove("glt_soaked");
            ItemStack coat = item.getItem().copy();
            CompoundTag tag = coat.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            tag.putLong("WashedUntil", level.getGameTime() + WASHED_TICKS);
            coat.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            item.setItem(coat);
            ServerPlayer nearest = (ServerPlayer) level.getNearestPlayer(item, 8);
            if (nearest != null) {
                nearest.displayClientMessage(Component.translatable("message.glt.bath.coat_washed"), true);
            }
        }
    }

    /** True if this coat was washed recently enough to still be "dry and warm". */
    public static boolean isFreshlyWashed(ItemStack coat, Level level) {
        CustomData data = coat.get(DataComponents.CUSTOM_DATA);
        return data != null && data.copyTag().getLong("WashedUntil") > level.getGameTime();
    }
}
