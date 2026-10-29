package com.ok2603.glt.block;

import com.mojang.serialization.MapCodec;
import com.ok2603.glt.registry.ModBlockEntities;
import com.ok2603.glt.role.Roles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * The old food factory: the only way to make food instead of finding it.
 * Put in crops (potatoes, wheat, carrots, beetroot) or cocoa beans, plus sugar, and give it
 * power (a Fuel Canister, or coal). It presses out ration bars (or chocolate rations from cocoa).
 * Only Chito understands the controls; Yuuri just presses buttons at random.
 */
public class FoodFactoryBlock extends BaseEntityBlock {
    public static final MapCodec<FoodFactoryBlock> CODEC = simpleCodec(FoodFactoryBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public FoodFactoryBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LIT, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FoodFactoryBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.FOOD_FACTORY.get(), FoodFactoryBlockEntity::serverTick);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!Roles.canRepair(player)) {
            player.displayClientMessage(Component.translatable("message.glt.factory.yuuri"), true);
            level.playSound(null, pos, SoundEvents.STONE_BUTTON_CLICK_ON, SoundSource.BLOCKS, 0.6f, 1.6f);
            return InteractionResult.CONSUME;
        }
        if (level.getBlockEntity(pos) instanceof FoodFactoryBlockEntity factory) {
            player.openMenu(factory);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        Containers.dropContentsOnDestroy(state, newState, level, pos);
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(LIT) && random.nextInt(3) == 0) {
            level.addParticle(ParticleTypes.SMOKE, pos.getX() + 0.3 + random.nextDouble() * 0.4, pos.getY() + 1.05,
                    pos.getZ() + 0.3 + random.nextDouble() * 0.4, 0, 0.03, 0);
            if (random.nextInt(10) == 0) {
                level.playLocalSound(pos, SoundEvents.PISTON_CONTRACT, SoundSource.BLOCKS, 0.15f, 1.8f, false);
            }
        }
    }
}
