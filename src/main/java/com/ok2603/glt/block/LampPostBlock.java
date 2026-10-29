package com.ok2603.glt.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** A thin iron post for street lamps. Stack a few and put a Street Lamp on top. */
public class LampPostBlock extends Block {
    public static final MapCodec<LampPostBlock> CODEC = simpleCodec(LampPostBlock::new);
    private static final VoxelShape SHAPE = Block.box(6, 0, 6, 10, 16, 10);

    public LampPostBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}
