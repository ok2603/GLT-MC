package com.ok2603.glt.item;

import com.ok2603.glt.registry.ModDataComponents;
import com.ok2603.glt.role.Role;
import com.ok2603.glt.role.Roles;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * A scrap of paper from the old world. Right-click to read it.
 * Chito can read the old writing; Yuuri only sees scribbles.
 * Each note gets one of NOTE_COUNT texts (note.glt.old_note.N in en_us.json) the first time it is held.
 */
public class OldNoteItem extends Item {
    public static final int NOTE_COUNT = 16;

    public OldNoteItem(Properties properties) {
        super(properties);
    }

    /** Which note this is, picking one at random if it doesn't have one yet. */
    public static int noteId(ItemStack stack, Level level) {
        Integer id = stack.get(ModDataComponents.NOTE.get());
        if (id == null) {
            id = level.getRandom().nextInt(NOTE_COUNT);
            stack.set(ModDataComponents.NOTE.get(), id);
        }
        return id;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!level.isClientSide) {
            noteId(stack, level);
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            int id = noteId(stack, level);
            if (Roles.get(player) == Role.YUURI) {
                player.displayClientMessage(Component.translatable("message.glt.note.scribbles")
                        .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), false);
            } else {
                player.displayClientMessage(Component.translatable("note.glt.old_note." + id)
                        .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), false);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.glt.old_note.tooltip").withStyle(ChatFormatting.GRAY));
    }
}
