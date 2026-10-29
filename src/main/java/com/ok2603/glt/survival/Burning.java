package com.ok2603.glt.survival;

import com.ok2603.glt.GLTMod;
import com.ok2603.glt.cold.BodyHeat;
import com.ok2603.glt.journal.Journal;
import com.ok2603.glt.journal.JournalData;
import com.ok2603.glt.journal.JournalEntry;
import com.ok2603.glt.journal.JournalMoment;
import com.ok2603.glt.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.Map;

/**
 * Throw paper onto a lit campfire for a burst of warmth, like the girls burning books and maps.
 * It's a real choice: warmth now, or keep the books and maps.
 *
 * Chito's journal can be burned too, but the first time it only singes and jumps back out.
 * Throw it in again to really burn it: the whole journal is lost, and you get a lot of warmth.
 */
@EventBusSubscriber(modid = GLTMod.MODID)
public class Burning {
    /** Warmth each item gives (per item in the stack). */
    private static final Map<Item, Float> WARMTH = Map.of(
            Items.PAPER, 4f,
            Items.BOOK, 20f,
            Items.WRITABLE_BOOK, 20f,
            Items.WRITTEN_BOOK, 20f,
            Items.MAP, 12f,
            Items.FILLED_MAP, 12f);
    private static final float NOTE_WARMTH = 6f;
    private static final float JOURNAL_WARMTH = 60f;
    private static final float MAX_BURST = 60f;
    private static final double WARM_RANGE = 5.0;

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof ItemEntity item) || item.tickCount % 5 != 0
                || !(item.level() instanceof ServerLevel level) || item.isRemoved()) {
            return;
        }
        ItemStack stack = item.getItem();
        float warmthEach = warmthOf(stack);
        if (warmthEach <= 0 || !onLitFire(level, item.blockPosition())) {
            return;
        }

        if (stack.is(ModItems.CHITOS_JOURNAL.get()) && !warned(stack)) {
            // First time: it only singes. Pop it back out with a warning.
            CompoundTag tag = new CompoundTag();
            tag.putBoolean("BurnWarned", true);
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            item.setDeltaMovement((level.random.nextDouble() - 0.5) * 0.3, 0.45, (level.random.nextDouble() - 0.5) * 0.3);
            level.playSound(null, item.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.6f, 1.4f);
            for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, item.getBoundingBox().inflate(8))) {
                player.displayClientMessage(Component.translatable("message.glt.burn.journal_warning"), true);
            }
            return;
        }

        float warmth = Math.min(MAX_BURST, warmthEach * stack.getCount());
        boolean journal = stack.is(ModItems.CHITOS_JOURNAL.get());
        item.discard();
        level.sendParticles(ParticleTypes.FLAME, item.getX(), item.getY() + 0.3, item.getZ(), 20, 0.25, 0.3, 0.25, 0.03);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, item.getX(), item.getY() + 0.6, item.getZ(), 6, 0.2, 0.3, 0.2, 0.01);
        level.playSound(null, item.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 0.7f, 0.8f);

        for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, item.getBoundingBox().inflate(WARM_RANGE))) {
            BodyHeat.add(player, warmth);
            player.displayClientMessage(Component.translatable("message.glt.burn.warm"), true);
        }

        ServerPlayer nearest = (ServerPlayer) level.getNearestPlayer(item, 8);
        if (journal) {
            // The trip's story is gone. Only this one line remains.
            JournalData data = JournalData.get(level.getServer());
            data.clear();
            data.add(JournalEntry.ofMoment(JournalMoment.JOURNAL_BURNED, Journal.today(level.getServer())));
            for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
                player.displayClientMessage(Component.translatable("message.glt.burn.journal_gone"), false);
            }
        } else if (nearest != null) {
            Journal.unlock(nearest, JournalMoment.BURN_BOOK);
        }
    }

    private static float warmthOf(ItemStack stack) {
        if (stack.is(ModItems.CHITOS_JOURNAL.get())) {
            return JOURNAL_WARMTH;
        }
        if (stack.is(ModItems.OLD_NOTE.get())) {
            return NOTE_WARMTH;
        }
        return WARMTH.getOrDefault(stack.getItem(), 0f);
    }

    private static boolean warned(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null && data.copyTag().getBoolean("BurnWarned");
    }

    private static boolean onLitFire(ServerLevel level, BlockPos pos) {
        return isLitFire(level.getBlockState(pos)) || isLitFire(level.getBlockState(pos.below()));
    }

    private static boolean isLitFire(BlockState state) {
        if (state.is(BlockTags.CAMPFIRES)) {
            return state.getValue(CampfireBlock.LIT);
        }
        return state.is(BlockTags.FIRE);
    }
}
