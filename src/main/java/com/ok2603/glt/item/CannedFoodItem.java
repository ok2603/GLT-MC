package com.ok2603.glt.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.function.Supplier;

/** Food in a can: eating it leaves the empty can behind (handy for rain music). */
public class CannedFoodItem extends Item {
    private final Supplier<? extends Item> leftover;

    public CannedFoodItem(Properties properties, Supplier<? extends Item> leftover) {
        super(properties);
        this.leftover = leftover;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (entity instanceof Player player && !player.getAbilities().instabuild && !level.isClientSide) {
            ItemStack can = new ItemStack(leftover.get());
            if (result.isEmpty()) {
                return can;
            }
            if (!player.getInventory().add(can)) {
                player.drop(can, false);
            }
        }
        return result;
    }
}
