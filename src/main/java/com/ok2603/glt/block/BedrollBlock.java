package com.ok2603.glt.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * A rolled-out blanket to sleep on, one block big. Works like a bed: sleep through the night
 * and wake up here. Sleeping next to the Kettenkrad warms you up fully (see SleepEvents).
 */
public class BedrollBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<BedrollBlock> CODEC = simpleCodec(BedrollBlock::new);
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 3, 16);

    public BedrollBlock(Properties properties) {
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
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    /** Tells Minecraft this counts as a bed (for sleeping and respawning). */
    @Override
    public boolean isBed(BlockState state, BlockGetter level, BlockPos pos, @Nullable LivingEntity sleeper) {
        return true;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!BedBlock.canSetSpawn(level)) {
            player.displayClientMessage(Component.translatable("message.glt.bedroll.cant_sleep_here"), true);
            return InteractionResult.CONSUME;
        }
        player.startSleepInBed(pos).ifLeft(problem -> {
            if (problem.getMessage() != null) {
                player.displayClientMessage(problem.getMessage(), true);
            }
        });
        return InteractionResult.CONSUME;
    }
}
