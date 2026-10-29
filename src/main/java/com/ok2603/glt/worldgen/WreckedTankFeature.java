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
import net.minecraft.world.level.block.LightningRodBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * A rusted, abandoned tank half sunk into the ground. Its hatch holds scrap and sometimes fuel,
 * and in about half of them a chocolate ration is hidden (see TankHatchBlock).
 */
public class WreckedTankFeature extends Feature<NoneFeatureConfiguration> {
    /** Chance that a tank has hidden chocolate inside. */
    private static final float CHOCOLATE_CHANCE = 0.5f;

    public WreckedTankFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        Direction facing = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        Builder b = new Builder(level, context.origin(), facing);

        if (!b.solidGround(-3, 3, -2, 2)) {
            return false;
        }

        BlockState plate = ModBlocks.RUSTED_PLATE.get().defaultBlockState();
        BlockState track = Blocks.POLISHED_BLACKSTONE.defaultBlockState();
        BlockState trackWheel = Blocks.CHAIN.defaultBlockState();
        BlockState ground = level.getBlockState(b.pos(0, 0, -1));

        // Tracks on both sides, half sunk into the ground (layer 0), with the hull between them.
        for (int f = -3; f <= 3; f++) {
            for (int r = -2; r <= 2; r++) {
                b.foundation(f, r, ground, 3);
                boolean side = Math.abs(r) == 2;
                if (side) {
                    b.set(f, r, 0, (f + r) % 2 == 0 ? track : trackWheel);
                } else if (random.nextFloat() > 0.1f) {
                    b.set(f, r, 0, plate);
                }
                // Hull deck, wider than the body so it covers the tracks. Rust has eaten some holes.
                if (Math.abs(f) <= 3 && random.nextFloat() > 0.12f) {
                    b.set(f, r, 1, plate);
                }
            }
        }

        // Turret, slightly toward the back.
        for (int f = -2; f <= 0; f++) {
            for (int r = -1; r <= 1; r++) {
                b.set(f, r, 2, plate);
            }
        }

        // The gun barrel points forward (sometimes broken off short).
        int barrelLength = 2 + random.nextInt(3);
        for (int f = 1; f <= barrelLength; f++) {
            b.set(f, 0, 2, Blocks.LIGHTNING_ROD.defaultBlockState().setValue(LightningRodBlock.FACING, facing));
        }

        // The hatch on top of the turret, with the loot inside.
        BlockPos hatchPos = b.pos(-1, 0, 3);
        level.setBlock(hatchPos, ModBlocks.TANK_HATCH.get().defaultBlockState(), 2);
        if (level.getBlockEntity(hatchPos) instanceof LootContainerBlockEntity hatch) {
            hatch.setLootTable(ModLootTables.WRECKED_TANK, random.nextLong());
            hatch.setHiddenChocolate(random.nextFloat() < CHOCOLATE_CHANCE);
        }

        // Snow settles on the cold metal.
        for (int f = -3; f <= 3; f++) {
            for (int r = -2; r <= 2; r++) {
                for (int up = 4; up >= 1; up--) {
                    BlockPos pos = b.pos(f, r, up);
                    if (level.getBlockState(pos).isAir() && level.getBlockState(pos.below()).is(ModBlocks.RUSTED_PLATE.get())) {
                        if (level.getBiome(pos).value().coldEnoughToSnow(pos) && random.nextFloat() < 0.6f) {
                            level.setBlock(pos, Blocks.SNOW.defaultBlockState(), 2);
                        }
                        break;
                    }
                }
            }
        }
        return true;
    }
}
