package com.ok2603.glt.block;

import com.mojang.serialization.MapCodec;
import com.ok2603.glt.journal.Journal;
import com.ok2603.glt.journal.JournalMoment;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A little pile of stones, like the ones near the temple. Right-click it with cobblestone
 * to stack one more stone on top. When the pile is complete, a bit of luck comes your way.
 */
public class CairnBlock extends Block {
    public static final MapCodec<CairnBlock> CODEC = simpleCodec(CairnBlock::new);
    public static final int MAX_STONES = 5;
    public static final IntegerProperty STONES = IntegerProperty.create("stones", 1, MAX_STONES);
    private static final VoxelShape[] SHAPES = {
            Block.box(4, 0, 4, 12, 3, 12),
            Block.box(4, 0, 4, 12, 6, 12),
            Block.box(4, 0, 4, 12, 8, 12),
            Block.box(4, 0, 4, 12, 10, 12),
            Block.box(4, 0, 4, 12, 12, 12)};

    public CairnBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(STONES, 1));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STONES);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(STONES) - 1];
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                              InteractionHand hand, BlockHitResult hit) {
        int stones = state.getValue(STONES);
        if (!stack.is(Items.COBBLESTONE) || stones >= MAX_STONES) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (level instanceof ServerLevel serverLevel) {
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            level.setBlock(pos, state.setValue(STONES, stones + 1), 3);
            level.playSound(null, pos, SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 1.0f, 1.2f);
            if (stones + 1 == MAX_STONES) {
                player.addEffect(new MobEffectInstance(MobEffects.LUCK, 20 * 60 * 5, 0));
                serverLevel.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 0.9, pos.getZ() + 0.5, 6, 0.2, 0.2, 0.2, 0.01);
                player.displayClientMessage(Component.translatable("message.glt.cairn.done"), true);
                if (player instanceof ServerPlayer serverPlayer) {
                    Journal.unlock(serverPlayer, JournalMoment.CAIRN);
                }
            }
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }
}
