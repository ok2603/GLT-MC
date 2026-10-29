package com.ok2603.glt.block;

import com.mojang.serialization.MapCodec;
import com.ok2603.glt.journal.Journal;
import com.ok2603.glt.journal.JournalMoment;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * An old street lamp that still works: it switches itself on at dusk and off at dawn.
 * Put it on top of a Lamp Post (or a fence). A rare bit of comfort in the dark city.
 */
public class StreetLampBlock extends Block {
    public static final MapCodec<StreetLampBlock> CODEC = simpleCodec(StreetLampBlock::new);
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    private static final VoxelShape SHAPE = Block.box(3, 0, 3, 13, 10, 13);
    private static final int CHECK_TICKS = 40;

    public StreetLampBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(LIT, false));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (!oldState.is(this)) {
            level.scheduleTick(pos, this, 1);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        boolean shouldBeLit = isDark(level, pos);
        if (state.getValue(LIT) != shouldBeLit) {
            level.setBlock(pos, state.setValue(LIT, shouldBeLit), 3);
            if (shouldBeLit) {
                level.playSound(null, pos, SoundEvents.COPPER_BULB_TURN_ON, SoundSource.BLOCKS, 0.4f, 1.0f);
                for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, new AABB(pos).inflate(16))) {
                    Journal.unlock(player, JournalMoment.STREET_LAMPS);
                }
            }
        }
        // Each lamp checks at a slightly different moment, so a street flickers on one by one.
        level.scheduleTick(pos, this, CHECK_TICKS + random.nextInt(20));
    }

    /**
     * Dark means: night time, or a place the sun never reaches (like the lower floors of the city,
     * where the street lamps never go out).
     */
    private static boolean isDark(Level level, BlockPos pos) {
        int daylight = level.getBrightness(LightLayer.SKY, pos) - level.getSkyDarken();
        return daylight < 8;
    }
}
