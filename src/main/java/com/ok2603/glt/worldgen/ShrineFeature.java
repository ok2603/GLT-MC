package com.ok2603.glt.worldgen;

import com.mojang.serialization.Codec;
import com.ok2603.glt.block.CairnBlock;
import com.ok2603.glt.block.StatueBlock;
import com.ok2603.glt.registry.ModBlocks;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/** A tiny roadside shrine: a row of stone statues on a stone base, with little piles of stones. */
public class ShrineFeature extends Feature<NoneFeatureConfiguration> {
    public ShrineFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        Direction facing = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        Builder b = new Builder(level, context.origin(), facing);
        if (!b.solidGround(-1, 1, -2, 2)) {
            return false;
        }
        int statues = 2 + random.nextInt(2);
        for (int r = -2; r <= 2; r++) {
            for (int f = -1; f <= 1; f++) {
                b.foundation(f, r, Blocks.STONE.defaultBlockState(), 3);
                b.set(f, r, -1, Blocks.STONE_BRICKS.defaultBlockState());
                b.set(f, r, 0, Blocks.AIR.defaultBlockState());
                b.set(f, r, 1, Blocks.AIR.defaultBlockState());
            }
            b.set(-1, r, 0, Blocks.STONE_BRICK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM));
        }
        // Statues in a row along the back, facing out over the step.
        for (int i = 0; i < statues; i++) {
            int r = i - statues / 2;
            b.set(0, r, 0, ModBlocks.STATUE.get().defaultBlockState().setValue(StatueBlock.FACING, facing));
        }
        // Stone piles on either side.
        b.set(1, -2, 0, ModBlocks.CAIRN.get().defaultBlockState().setValue(CairnBlock.STONES, 1 + random.nextInt(4)));
        b.set(1, 2, 0, ModBlocks.CAIRN.get().defaultBlockState().setValue(CairnBlock.STONES, 1 + random.nextInt(4)));
        return true;
    }
}
