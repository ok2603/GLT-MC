package com.ok2603.glt.block;

import com.mojang.serialization.MapCodec;
import com.ok2603.glt.journal.Journal;
import com.ok2603.glt.journal.JournalMoment;
import com.ok2603.glt.role.Role;
import com.ok2603.glt.role.Roles;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * A rusty metal sign from the old city, built into walls. Right-click to read it.
 * Chito can read the old writing; Yuuri only sees scribbles.
 * The text is sign.glt.old_sign.N in en_us.json.
 */
public class OldSignBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<OldSignBlock> CODEC = simpleCodec(OldSignBlock::new);
    public static final int TEXT_COUNT = 12;
    public static final IntegerProperty TEXT = IntegerProperty.create("text", 0, TEXT_COUNT - 1);

    public OldSignBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(TEXT, 0));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, TEXT);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(TEXT, context.getLevel().getRandom().nextInt(TEXT_COUNT));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (Roles.get(player) == Role.YUURI) {
            player.displayClientMessage(Component.translatable("message.glt.sign.scribbles")
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), false);
        } else {
            player.displayClientMessage(Component.translatable("sign.glt.old_sign." + state.getValue(TEXT))
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), false);
            if (player instanceof ServerPlayer serverPlayer) {
                Journal.unlock(serverPlayer, JournalMoment.OLD_SIGN);
            }
        }
        return InteractionResult.CONSUME;
    }
}
