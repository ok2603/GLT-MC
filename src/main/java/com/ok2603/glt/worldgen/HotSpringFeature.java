package com.ok2603.glt.worldgen;

import com.mojang.serialization.Codec;
import com.ok2603.glt.registry.ModBlocks;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * A broken hot pipe sticking out of the ground in a cold place, leaking into a small pool.
 * The pool is a warm bath (see HotBath).
 */
public class HotSpringFeature extends Feature<NoneFeatureConfiguration> {
    public HotSpringFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        Direction facing = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        Builder b = new Builder(level, context.origin(), facing);
        if (!b.solidGround(-2, 2, -2, 2)) {
            return false;
        }

        BlockState rim = Blocks.STONE_BRICKS.defaultBlockState();
        BlockState water = Blocks.WATER.defaultBlockState();
        // A 3x3 pool sunk one block into the ground, with a stone rim.
        for (int f = -2; f <= 2; f++) {
            for (int r = -2; r <= 2; r++) {
                boolean edge = Math.abs(f) == 2 || Math.abs(r) == 2;
                b.set(f, r, -2, rim);
                b.set(f, r, -1, edge ? rim : water);
                b.set(f, r, 0, Blocks.AIR.defaultBlockState());
                b.set(f, r, 1, Blocks.AIR.defaultBlockState());
            }
        }
        // The pipe comes out of the ground behind the pool, bends over it, and leaks.
        Direction.Axis along = facing.getAxis();
        BlockState up = ModBlocks.HOT_PIPE.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
        BlockState across = ModBlocks.HOT_PIPE.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, along);
        BlockState leak = ModBlocks.LEAKING_HOT_PIPE.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, along);
        b.foundation(-3, 0, rim, 3);
        b.set(-3, 0, -1, up);
        b.set(-3, 0, 0, up);
        b.set(-3, 0, 1, up);
        b.set(-3, 0, 2, across);
        b.set(-2, 0, 2, across);
        b.set(-1, 0, 2, leak);
        if (random.nextBoolean()) {
            b.set(0, 0, 2, across);
        }
        return true;
    }
}
