package com.ok2603.glt.item;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

/**
 * A bottle of beer from the old world. It warms you a little, makes the screen sway,
 * and the girls can't stop giggling. Leaves an empty bottle.
 */
public class BeerItem extends WarmFoodItem {
    public BeerItem(Properties properties) {
        super(properties, 10f);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public SoundEvent getEatingSound() {
        return SoundEvents.GENERIC_DRINK;
    }

    @Override
    public SoundEvent getDrinkingSound() {
        return SoundEvents.GENERIC_DRINK;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (entity instanceof ServerPlayer player) {
            player.displayClientMessage(Component.translatable("message.glt.beer.giggle"), true);
        }
        return super.finishUsingItem(stack, level, entity);
    }
}
