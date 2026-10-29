package com.ok2603.glt.block;

import com.mojang.serialization.MapCodec;
import com.ok2603.glt.journal.Journal;
import com.ok2603.glt.journal.JournalMoment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * A little stone statue from an old temple. Leave a piece of food in front of it as an offering
 * and you get a bit of luck for a while, which makes rare loot (chocolate, beer) a little more likely.
 */
public class StatueBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<StatueBlock> CODEC = simpleCodec(StatueBlock::new);
    private static final VoxelShape SHAPE = Block.box(3, 0, 3, 13, 15, 13);
    /** How long the luck lasts (10 minutes). */
    private static final int LUCK_TICKS = 20 * 60 * 10;

    public StatueBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                              InteractionHand hand, BlockHitResult hit) {
        if (!stack.has(DataComponents.FOOD)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (level instanceof ServerLevel serverLevel) {
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            player.addEffect(new MobEffectInstance(MobEffects.LUCK, LUCK_TICKS, 0));
            serverLevel.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5, 6, 0.2, 0.2, 0.2, 0.01);
            level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0f, 0.8f);
            player.displayClientMessage(Component.translatable("message.glt.statue.offering"), true);
            if (player instanceof ServerPlayer serverPlayer) {
                Journal.unlock(serverPlayer, JournalMoment.OFFERING);
            }
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }
}
