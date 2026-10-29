package com.ok2603.glt.item;

import com.ok2603.glt.entity.NukoEntity;
import com.ok2603.glt.registry.ModEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Nuko, held in your arms. While you hold it, it plays music like an old radio.
 * Right-click the ground to put it down again.
 */
public class NukoItem extends Item {
    public NukoItem(Properties properties) {
        super(properties);
    }

    /** Turns a Nuko in the world into an item that remembers it (owner, health, name...). */
    public static ItemStack capture(NukoEntity nuko) {
        ItemStack stack = new ItemStack(com.ok2603.glt.registry.ModItems.NUKO.get());
        CompoundTag tag = new CompoundTag();
        nuko.saveWithoutId(tag);
        tag.remove("Pos");
        tag.remove("Motion");
        tag.remove("UUID");
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        if (nuko.hasCustomName()) {
            stack.set(DataComponents.CUSTOM_NAME, nuko.getCustomName());
        }
        return stack;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }
        BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
        NukoEntity nuko = ModEntities.NUKO.get().create(serverLevel);
        if (nuko == null) {
            return InteractionResult.FAIL;
        }
        ItemStack stack = context.getItemInHand();
        CustomData saved = stack.get(DataComponents.CUSTOM_DATA);
        if (saved != null) {
            nuko.load(saved.copyTag());
        }
        float yaw = context.getPlayer() != null ? context.getPlayer().getYRot() + 180 : 0;
        nuko.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, yaw, 0);
        nuko.setYHeadRot(yaw);
        serverLevel.addFreshEntity(nuko);
        stack.shrink(1);
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.glt.nuko.tooltip").withStyle(ChatFormatting.GRAY));
    }
}
