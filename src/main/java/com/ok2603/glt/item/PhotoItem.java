package com.ok2603.glt.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;

import java.util.List;

/** A photo taken with the camera. Hang it in an item frame to remember the trip. */
public class PhotoItem extends Item {
    public PhotoItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) {
            return;
        }
        CompoundTag tag = data.copyTag();
        tooltip.add(Component.translatable("item.glt.photo.place", Component.translatable(tag.getString("Place")))
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.glt.photo.day", tag.getInt("Day")).withStyle(ChatFormatting.GRAY));
        ListTag people = tag.getList("People", 8);
        if (people.isEmpty()) {
            tooltip.add(Component.translatable("item.glt.photo.nobody").withStyle(ChatFormatting.DARK_GRAY));
        } else {
            StringBuilder names = new StringBuilder();
            for (int i = 0; i < people.size(); i++) {
                if (i > 0) {
                    names.append(", ");
                }
                names.append(people.getString(i));
            }
            tooltip.add(Component.translatable("item.glt.photo.people", names.toString()).withStyle(ChatFormatting.GRAY));
        }
    }
}
