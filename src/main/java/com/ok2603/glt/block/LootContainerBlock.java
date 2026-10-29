package com.ok2603.glt.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * A container found in ruins (like the supply crate). It holds 27 items, like a chest.
 * When it was placed by world generation, its loot is rolled the first time someone opens it,
 * so Chito and Yuuri each get their own role bonus.
 */
public class LootContainerBlock extends BaseEntityBlock {
    public static final MapCodec<LootContainerBlock> CODEC = simpleCodec(LootContainerBlock::new);

    public LootContainerBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends LootContainerBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LootContainerBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (level.getBlockEntity(pos) instanceof LootContainerBlockEntity container) {
            beforeOpening(container, player);
            player.openMenu(container);
        }
        return InteractionResult.CONSUME;
    }

    /** Runs on the server just before the container opens. */
    protected void beforeOpening(LootContainerBlockEntity container, Player player) {
    }

    /** Breaking it spills what's inside. */
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        Containers.dropContentsOnDestroy(state, newState, level, pos);
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
