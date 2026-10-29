package com.ok2603.glt.block;

import com.mojang.serialization.MapCodec;
import com.ok2603.glt.registry.ModItems;
import com.ok2603.glt.role.Role;
import com.ok2603.glt.role.Roles;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * The hatch of a wrecked tank. Opens like a crate. Some tanks have a chocolate ration hidden
 * in a corner: Yuuri always sniffs it out, Chito only hears something rattle.
 * If nobody is playing Yuuri (for example in single player as Chito), anyone can find it.
 */
public class TankHatchBlock extends LootContainerBlock {
    public static final MapCodec<TankHatchBlock> CODEC = simpleCodec(TankHatchBlock::new);

    public TankHatchBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<TankHatchBlock> codec() {
        return CODEC;
    }

    @Override
    protected void beforeOpening(LootContainerBlockEntity container, Player player) {
        if (!container.hasHiddenChocolate()) {
            return;
        }
        if (canSniffOutChocolate(player)) {
            container.setHiddenChocolate(false);
            ItemStack chocolate = new ItemStack(ModItems.CHOCOLATE_RATION.get());
            if (!player.getInventory().add(chocolate)) {
                player.drop(chocolate, false);
            }
            player.level().playSound(null, player.blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.8f, 1.2f);
            player.displayClientMessage(Component.translatable("message.glt.tank.chocolate_found"), false);
        } else {
            player.displayClientMessage(Component.translatable("message.glt.tank.rattle"), true);
        }
    }

    private static boolean canSniffOutChocolate(Player player) {
        if (Roles.get(player) == Role.YUURI) {
            return true;
        }
        // Nobody is playing Yuuri right now: don't lock the chocolate away forever.
        return player.level().players().stream().noneMatch(other -> Roles.get(other) == Role.YUURI);
    }
}
