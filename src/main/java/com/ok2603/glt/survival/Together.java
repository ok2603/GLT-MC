package com.ok2603.glt.survival;

import com.ok2603.glt.GLTMod;
import com.ok2603.glt.config.GLTConfig;
import com.ok2603.glt.entity.KettenkradEntity;
import com.ok2603.glt.journal.Journal;
import com.ok2603.glt.journal.JournalMoment;
import com.ok2603.glt.registry.ModEffects;
import com.ok2603.glt.registry.ModItems;
import com.ok2603.glt.role.Role;
import com.ok2603.glt.role.Roles;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * The little things that make it a trip for two:
 *  - Lonely: wander too far from each other for too long and fires warm you only half as well.
 *  - Sharing food: right-click the other girl with food to feed her. Shared food fills more.
 *  - Nibbling: a hungry Yuuri sometimes sneaks a ration from the Kettenkrad's storage.
 */
@EventBusSubscriber(modid = GLTMod.MODID)
public class Together {
    private static final String APART = "glt_apart_seconds";
    /** Shared food gives this much extra hunger on top of the food itself (0.5 = 50% more). */
    public static final float SHARE_BONUS = 0.5f;
    private static final int NIBBLE_INTERVAL = 20 * 60 * 5;
    private static final float NIBBLE_CHANCE = 0.3f;

    // ---------- Lonely ----------

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.isSpectator()) {
            return;
        }
        if (player.tickCount % 20 == 0) {
            updateLonely(player);
        }
        if (player.tickCount % NIBBLE_INTERVAL == 0 && player.tickCount > 0) {
            maybeNibble(player);
        }
    }

    private static void updateLonely(ServerPlayer player) {
        int distance = GLTConfig.get(GLTConfig.LONELY_DISTANCE);
        CompoundTag data = player.getPersistentData();
        ServerPlayer nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        boolean anyoneElse = false;
        for (ServerPlayer other : player.server.getPlayerList().getPlayers()) {
            if (other == player || other.isSpectator()) {
                continue;
            }
            anyoneElse = true;
            double d = other.level() == player.level() ? other.distanceToSqr(player) : Double.MAX_VALUE;
            if (nearest == null || d < nearestDistance) {
                nearest = other;
                nearestDistance = d;
            }
        }
        if (distance <= 0 || !anyoneElse || nearestDistance < (double) distance * distance) {
            data.putInt(APART, 0);
            if (player.hasEffect(ModEffects.LONELY)) {
                player.removeEffect(ModEffects.LONELY);
            }
            return;
        }
        int apart = data.getInt(APART) + 1;
        data.putInt(APART, apart);
        int delay = GLTConfig.get(GLTConfig.LONELY_DELAY);
        if (apart == delay) {
            player.displayClientMessage(Component.translatable("message.glt.lonely", nearest.getDisplayName()), true);
            Journal.unlock(player, JournalMoment.LONELY);
        }
        if (apart >= delay) {
            player.addEffect(new MobEffectInstance(ModEffects.LONELY, 20 * 30, 0, false, false, true));
        }
    }

    // ---------- Sharing food ----------

    @SubscribeEvent
    public static void onInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof Player target) || event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        Player feeder = event.getEntity();
        ItemStack stack = event.getItemStack();
        FoodProperties food = stack.get(DataComponents.FOOD);
        if (food == null) {
            return;
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        if (!(feeder instanceof ServerPlayer server) || !(target instanceof ServerPlayer hungry)) {
            return;
        }
        if (!hungry.getFoodData().needsFood() && !food.canAlwaysEat()) {
            server.displayClientMessage(Component.translatable("message.glt.share.full", hungry.getDisplayName()), true);
            return;
        }
        feed(server, hungry, stack, food);
    }

    /** Feeds one bite of the feeder's food to the other player. Public so tests can use it. */
    public static void feed(Player feeder, Player hungry, ItemStack stack, FoodProperties food) {
        ItemStack bite = feeder.getAbilities().instabuild ? stack.copyWithCount(1) : stack.split(1);
        ItemStack leftover = bite.finishUsingItem(hungry.level(), hungry);
        hungry.getFoodData().eat(Math.max(1, Math.round(food.nutrition() * SHARE_BONUS)), 0.3f);
        if (!leftover.isEmpty() && !feeder.getInventory().add(leftover)) {
            feeder.drop(leftover, false);
        }
        hungry.level().playSound(null, hungry.blockPosition(), SoundEvents.GENERIC_EAT, SoundSource.PLAYERS, 0.8f, 1.1f);
        if (hungry.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.HEART, hungry.getX(), hungry.getEyeY() + 0.4, hungry.getZ(), 2, 0.3, 0.1, 0.3, 0);
        }
        feeder.displayClientMessage(Component.translatable("message.glt.share.gave", hungry.getDisplayName()), true);
        hungry.displayClientMessage(Component.translatable("message.glt.share.got", feeder.getDisplayName()), true);
        if (feeder instanceof ServerPlayer serverFeeder) {
            Journal.unlock(serverFeeder, JournalMoment.SHARE_FOOD);
        }
    }

    // ---------- Yuuri's snacks ----------

    private static void maybeNibble(ServerPlayer player) {
        if (!GLTConfig.get(GLTConfig.YUURI_NIBBLES) || Roles.get(player) != Role.YUURI
                || !player.getFoodData().needsFood() || player.getRandom().nextFloat() > NIBBLE_CHANCE) {
            return;
        }
        for (KettenkradEntity kettenkrad : player.level().getEntitiesOfClass(KettenkradEntity.class,
                player.getBoundingBox().inflate(6))) {
            var storage = kettenkrad.getStorage();
            for (int slot = 0; slot < storage.getContainerSize(); slot++) {
                ItemStack stack = storage.getItem(slot);
                if (stack.is(ModItems.RATION_BAR.get())) {
                    stack.shrink(1);
                    storage.setChanged();
                    player.getFoodData().eat(3, 0.3f);
                    player.displayClientMessage(Component.translatable("message.glt.nibble.yuuri"), true);
                    for (ServerPlayer other : player.serverLevel().getEntitiesOfClass(ServerPlayer.class,
                            player.getBoundingBox().inflate(16), other -> other != player)) {
                        other.displayClientMessage(Component.translatable("message.glt.nibble.others", player.getDisplayName()), true);
                    }
                    Journal.unlock(player, JournalMoment.NIBBLE);
                    return;
                }
            }
        }
    }
}
