package com.ok2603.glt.item;

import com.ok2603.glt.entity.KettenkradEntity;
import com.ok2603.glt.registry.ModEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import java.util.List;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

/** Right-click on the ground to place a Kettenkrad. */
public class KettenkradItem extends Item {
    public KettenkradItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
        KettenkradEntity kettenkrad = ModEntities.KETTENKRAD.get().create(level);
        if (kettenkrad == null) {
            return InteractionResult.FAIL;
        }
        // Restore the fuel and condition it had when it was picked up.
        CustomData saved = context.getItemInHand().get(DataComponents.CUSTOM_DATA);
        if (saved != null) {
            CompoundTag tag = saved.copyTag();
            kettenkrad.setFuel(tag.getInt("Fuel"));
            if (tag.contains("Condition")) {
                kettenkrad.setCondition(tag.getInt("Condition"));
            }
        }
        // Face the same way as the player.
        kettenkrad.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, context.getRotation(), 0);
        if (!level.noCollision(kettenkrad, kettenkrad.getBoundingBox())) {
            return InteractionResult.FAIL;
        }

        level.addFreshEntity(kettenkrad);
        Player player = context.getPlayer();
        level.gameEvent(player, GameEvent.ENTITY_PLACE, pos);
        if (player == null || !player.getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        CustomData saved = stack.get(DataComponents.CUSTOM_DATA);
        if (saved != null) {
            CompoundTag tag = saved.copyTag();
            int fuel = tag.getInt("Fuel") * 100 / KettenkradEntity.MAX_FUEL;
            int condition = tag.contains("Condition") ? tag.getInt("Condition") : KettenkradEntity.MAX_CONDITION;
            tooltip.add(Component.translatable("hud.glt.kettenkrad.status", fuel, condition).withStyle(ChatFormatting.GRAY));
        }
    }
}
