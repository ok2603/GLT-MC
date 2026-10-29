package com.ok2603.glt.worldgen;

import com.mojang.serialization.Codec;
import com.ok2603.glt.block.LootContainerBlockEntity;
import com.ok2603.glt.registry.ModBlocks;
import com.ok2603.glt.registry.ModLootTables;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * A small ruined concrete shelter from the old world, half collapsed, with one or two
 * supply crates inside. The main place to scavenge rations and fuel outside the city.
 */
public class SupplyCacheFeature extends Feature<NoneFeatureConfiguration> {
    private static final int SIZE = 3; // the room goes from -SIZE to +SIZE (7 x 7 blocks)

    public SupplyCacheFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        Direction facing = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        Builder b = new Builder(level, context.origin(), facing);

        if (!b.solidGround(-SIZE, SIZE, -SIZE, SIZE)) {
            return false;
        }

        BlockState floor = Blocks.STONE_BRICKS.defaultBlockState();
        BlockState crackedFloor = Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
        BlockState wall = Blocks.LIGHT_GRAY_CONCRETE.defaultBlockState();
        BlockState crackedWall = Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
        BlockState roof = Blocks.SMOOTH_STONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
        BlockState rubble = Blocks.COBBLESTONE.defaultBlockState();

        // How much of the roof is still standing (the rest has collapsed into rubble).
        float roofLeft = 0.3f + random.nextFloat() * 0.6f;

        for (int f = -SIZE; f <= SIZE; f++) {
            for (int r = -SIZE; r <= SIZE; r++) {
                b.foundation(f, r, floor, 4);
                b.set(f, r, -1, random.nextFloat() < 0.3f ? crackedFloor : floor);
                boolean edge = Math.abs(f) == SIZE || Math.abs(r) == SIZE;
                boolean door = f == SIZE && r == 0;

                for (int up = 0; up <= 3; up++) {
                    b.set(f, r, up, Blocks.AIR.defaultBlockState());
                }
                if (edge && !door) {
                    // Walls crumble from the top: each column keeps 1 to 3 blocks.
                    int height = 1 + random.nextInt(3);
                    for (int up = 0; up < height; up++) {
                        b.set(f, r, up, random.nextFloat() < 0.25f ? crackedWall : wall);
                    }
                    if (height == 3 && random.nextFloat() < roofLeft) {
                        b.set(f, r, 3, roof);
                    }
                } else if (!edge) {
                    if (random.nextFloat() < roofLeft) {
                        b.set(f, r, 3, roof);
                    } else if (random.nextFloat() < 0.25f) {
                        b.set(f, r, 0, rubble); // fallen roof
                    }
                }
            }
        }

        // One crate in a back corner, sometimes a second one.
        placeCrate(level, random, b.pos(-SIZE + 1, -SIZE + 1, 0));
        if (random.nextFloat() < 0.35f) {
            placeCrate(level, random, b.pos(-SIZE + 1, SIZE - 1, 0));
        }
        // Sometimes a street lamp still stands outside the door.
        if (random.nextFloat() < 0.3f) {
            b.foundation(SIZE + 1, 1, floor, 4);
            for (int up = 0; up < 3; up++) {
                b.set(SIZE + 1, 1, up, ModBlocks.LAMP_POST.get().defaultBlockState());
            }
            b.set(SIZE + 1, 1, 3, ModBlocks.STREET_LAMP.get().defaultBlockState());
            level.scheduleTick(b.pos(SIZE + 1, 1, 3), ModBlocks.STREET_LAMP.get(), 20);
        }
        // An old candle left behind.
        if (random.nextFloat() < 0.5f) {
            b.set(-SIZE + 1, 0, 0, Blocks.CANDLE.defaultBlockState());
        }
        return true;
    }

    private static void placeCrate(WorldGenLevel level, RandomSource random, BlockPos pos) {
        level.setBlock(pos, ModBlocks.SUPPLY_CRATE.get().defaultBlockState(), 2);
        if (level.getBlockEntity(pos) instanceof LootContainerBlockEntity crate) {
            crate.setLootTable(ModLootTables.SUPPLY_CACHE, random.nextLong());
        }
    }
}
