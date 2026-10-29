package com.ok2603.glt.item;

import com.ok2603.glt.cold.BodyHeat;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Food that also warms you up when you eat it, like hot soup. */
public class WarmFoodItem extends Item {
    private final float warmth;

    public WarmFoodItem(Properties properties, float warmth) {
        super(properties);
        this.warmth = warmth;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (entity instanceof ServerPlayer player) {
            BodyHeat.add(player, warmth);
        }
        return super.finishUsingItem(stack, level, entity);
    }
}
