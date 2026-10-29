package com.ok2603.glt.city;

import com.mojang.serialization.Codec;
import com.ok2603.glt.block.ElevatorConsoleBlock;
import com.ok2603.glt.block.ElevatorConsoleBlockEntity;
import com.ok2603.glt.block.FoodFactoryBlock;
import com.ok2603.glt.block.LootContainerBlockEntity;
import com.ok2603.glt.block.OldSignBlock;
import com.ok2603.glt.block.StatueBlock;
import com.ok2603.glt.city.CityLayout.Lot;
import com.ok2603.glt.registry.ModBlocks;
import com.ok2603.glt.registry.ModEntities;
import com.ok2603.glt.registry.ModFeatures;
import com.ok2603.glt.registry.ModLootTables;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Cod;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.function.Consumer;

/**
 * Builds the Last City, one chunk at a time, following the plan in CityLayout.
 *
 * It runs once per chunk (as the only feature of the glt:dead_city biome) and only ever
 * touches blocks inside its own chunk, so chunks can be built in any order and still fit
 * together. Because a 32-block cell is exactly 2x2 chunks, each chunk is one quarter of a lot.
 * Things that need to be built all at once (elevators, stair towers, tanks) sit in the far
 * quarter of their lot (local x and z from 16 to 31).
 */
public class CityFeature extends Feature<NoneFeatureConfiguration> {
    private static final int STORY = 5;
    private static final int RAMP_UNITS_PER_LAP = 32;
    private static final double RAMP_RISE_PER_UNIT = 0.25;

    public CityFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        ChunkPos chunkPos = new ChunkPos(context.origin());
        Chunk c = new Chunk(context, level.getChunk(chunkPos.x, chunkPos.z), chunkPos,
                new CityLayout(level.getSeed()));

        // Solid ground under the bottom floor.
        for (int lx = c.lx0; lx < c.lx0 + 16; lx++) {
            for (int lz = c.lz0; lz < c.lz0 + 16; lz++) {
                for (int y = level.getMinBuildHeight() + 1; y < CityLayout.floorY(0) - CityLayout.SLAB_THICKNESS; y++) {
                    c.set(lx, y, lz, Blocks.DEEPSLATE.defaultBlockState());
                }
            }
        }

        for (int floor = 0; floor < CityLayout.FLOORS; floor++) {
            Lot lot = c.layout.lot(floor, c.cx, c.cz);
            buildSlab(c, floor, lot);
            if (!CityLayout.isTopFloor(floor)) {
                buildPillarsAndPipes(c, floor);
            }
            buildLot(c, floor, lot);
            buildStreet(c, floor);
        }
        return true;
    }

    // ---------- Floors ----------

    private static void buildSlab(Chunk c, int floor, Lot lot) {
        int top = CityLayout.floorY(floor) - 1;
        Lot below = floor > 0 ? c.layout.lot(floor - 1, c.cx, c.cz) : null;
        for (int lx = c.lx0; lx < c.lx0 + 16; lx++) {
            for (int lz = c.lz0; lz < c.lz0 + 16; lz++) {
                if (isSlabOpening(lot, below, lx, lz)) {
                    continue;
                }
                boolean road = lx < CityLayout.ROAD_WIDTH || lz < CityLayout.ROAD_WIDTH;
                for (int i = 0; i < CityLayout.SLAB_THICKNESS; i++) {
                    int y = top - i;
                    BlockState state;
                    if (i == 0) {
                        state = road ? roadSurface(c, lx, y, lz) : lotSurface(c, lot, lx, y, lz);
                    } else if (i == CityLayout.SLAB_THICKNESS - 1) {
                        state = c.noise(lx, y, lz, 2) < 0.15 ? Blocks.CRACKED_STONE_BRICKS.defaultBlockState()
                                : Blocks.SMOOTH_STONE.defaultBlockState();
                    } else {
                        state = Blocks.STONE.defaultBlockState();
                    }
                    c.set(lx, y, lz, state);
                }
            }
        }
        // A railing around holes, so you don't walk into them in the dark.
        if (lot == Lot.HOLE) {
            int y = CityLayout.floorY(floor);
            for (int lx = c.lx0; lx < c.lx0 + 16; lx++) {
                for (int lz = c.lz0; lz < c.lz0 + 16; lz++) {
                    boolean ring = (lx == 7 || lx == 30) && lz >= 7 && lz <= 30
                            || (lz == 7 || lz == 30) && lx >= 7 && lx <= 30;
                    if (ring && c.noise(lx, y, lz, 3) > 0.2) {
                        c.setConnecting(lx, y, lz, Blocks.IRON_BARS.defaultBlockState());
                    }
                }
            }
        }
    }

    private static boolean isSlabOpening(Lot lot, Lot below, int lx, int lz) {
        if (lot == Lot.HOLE && lx >= 8 && lx <= 29 && lz >= 8 && lz <= 29) {
            return true;
        }
        if (below == Lot.ELEVATOR && CityLayout.onPlatform(lx, lz)) {
            return true;
        }
        return below == Lot.STAIRS && inRampRing(lx, lz);
    }

    private static boolean inRampRing(int lx, int lz) {
        boolean inside = lx > CityLayout.TOWER_MIN && lx < CityLayout.TOWER_MAX
                && lz > CityLayout.TOWER_MIN && lz < CityLayout.TOWER_MAX;
        boolean core = lx >= CityLayout.CORE_MIN && lx <= CityLayout.CORE_MAX
                && lz >= CityLayout.CORE_MIN && lz <= CityLayout.CORE_MAX;
        return inside && !core;
    }

    private static BlockState roadSurface(Chunk c, int lx, int y, int lz) {
        double n = c.noise(lx, y, lz, 4);
        if (n < 0.03) {
            return Blocks.COBBLESTONE.defaultBlockState();
        }
        if (n < 0.12) {
            return Blocks.ANDESITE.defaultBlockState();
        }
        // Faded lane markings down the middle of each road.
        if ((lx == 3 && lz >= CityLayout.ROAD_WIDTH || lz == 3 && lx >= CityLayout.ROAD_WIDTH)
                && Math.floorMod(lx + lz, 4) < 2 && n > 0.5) {
            return Blocks.WHITE_CONCRETE.defaultBlockState();
        }
        return Blocks.GRAY_CONCRETE.defaultBlockState();
    }

    private static BlockState lotSurface(Chunk c, Lot lot, int lx, int y, int lz) {
        double n = c.noise(lx, y, lz, 5);
        return switch (lot) {
            case PLAZA, ELEVATOR, SUMMIT, AIRFIELD -> n < 0.1 ? Blocks.ANDESITE.defaultBlockState() : Blocks.POLISHED_ANDESITE.defaultBlockState();
            case TEMPLE -> n < 0.2 ? Blocks.MOSSY_STONE_BRICKS.defaultBlockState() : Blocks.STONE_BRICKS.defaultBlockState();
            case FACTORY, TANKS -> n < 0.15 ? Blocks.GRAVEL.defaultBlockState() : Blocks.GRAY_CONCRETE.defaultBlockState();
            default -> n < 0.1 ? Blocks.CRACKED_STONE_BRICKS.defaultBlockState() : Blocks.STONE_BRICKS.defaultBlockState();
        };
    }

    /** Support pillars where roads cross, and big pipes hanging under the floor above along the roads. */
    private static void buildPillarsAndPipes(Chunk c, int floor) {
        int bottom = CityLayout.floorY(floor);
        int ceiling = c.ceiling(floor);
        for (int lx = c.lx0; lx < c.lx0 + 16; lx++) {
            for (int lz = c.lz0; lz < c.lz0 + 16; lz++) {
                if (lx >= 1 && lx <= 4 && lz >= 1 && lz <= 4) {
                    for (int y = bottom; y <= ceiling; y++) {
                        boolean band = (y - bottom) % 12 == 11;
                        c.set(lx, y, lz, band ? Blocks.CHISELED_STONE_BRICKS.defaultBlockState()
                                : c.noise(lx, y, lz, 6) < 0.1 ? Blocks.CRACKED_STONE_BRICKS.defaultBlockState()
                                : Blocks.STONE_BRICKS.defaultBlockState());
                    }
                    continue;
                }
                boolean alongX = (lz == 2 || lz == 3) && lx >= CityLayout.ROAD_WIDTH;
                boolean alongZ = (lx == 2 || lx == 3) && lz >= CityLayout.ROAD_WIDTH;
                if (alongX || alongZ) {
                    BlockState pipe = ModBlocks.PIPE.get().defaultBlockState()
                            .setValue(RotatedPillarBlock.AXIS, alongX ? Direction.Axis.X : Direction.Axis.Z);
                    c.set(lx, ceiling, lz, pipe);
                    c.set(lx, ceiling - 1, lz, pipe);
                }
            }
        }
    }

    // ---------- Lots ----------

    private static void buildLot(Chunk c, int floor, Lot lot) {
        switch (lot) {
            case BUILDING -> buildBuilding(c, floor, false);
            case RUIN -> buildBuilding(c, floor, true);
            case FACTORY -> buildFactory(c, floor);
            case PLAZA -> buildPlaza(c, floor);
            case PIPES -> buildPipes(c, floor);
            case TANKS -> buildTanks(c, floor);
            case TEMPLE -> buildTemple(c, floor);
            case FISH_FARM -> buildFishFarm(c, floor);
            case ELEVATOR -> buildElevator(c, floor);
            case STAIRS -> buildStairTower(c, floor);
            case AIRFIELD -> buildAirfield(c, floor);
            case SUMMIT -> buildSummit(c, floor);
            case HOLE -> { }
        }
    }

    private static BlockState wallBlock(Chunk c, int style, int lx, int y, int lz) {
        double n = c.noise(lx, y, lz, 7);
        if (n < 0.06) {
            return Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
        }
        if (n < 0.1) {
            return Blocks.ANDESITE.defaultBlockState();
        }
        return switch (style) {
            case 0 -> Blocks.LIGHT_GRAY_CONCRETE.defaultBlockState();
            case 1 -> Blocks.GRAY_CONCRETE.defaultBlockState();
            case 2 -> Blocks.STONE_BRICKS.defaultBlockState();
            default -> n < 0.5 ? Blocks.SMOOTH_STONE.defaultBlockState() : Blocks.WHITE_CONCRETE.defaultBlockState();
        };
    }

    /** An apartment block or office, with stories, windows, a ladder, and maybe a few crates. Ruins are crumbled versions. */
    private static void buildBuilding(Chunk c, int floor, boolean ruin) {
        int[] b = c.layout.buildingBounds(floor, c.cx, c.cz);
        int minX = b[0], minZ = b[1], maxX = b[2], maxZ = b[3];
        int y0 = CityLayout.floorY(floor);
        int ceiling = c.ceiling(floor);
        int height = Math.min(c.layout.buildingHeight(floor, c.cx, c.cz), ceiling - y0 + 1);
        // Buildings that almost reach the floor above are joined to it, holding it up.
        if (!CityLayout.isTopFloor(floor) && ceiling - (y0 + height - 1) < 4) {
            height = ceiling - y0 + 1;
        }
        int style = c.layout.buildingStyle(floor, c.cx, c.cz);
        int doorX = (minX + maxX) / 2;
        int ladderX = minX + 1;
        int ladderZ = minZ + 1;

        for (int lx = Math.max(minX, c.lx0); lx <= Math.min(maxX, c.lx0 + 15); lx++) {
            for (int lz = Math.max(minZ, c.lz0); lz <= Math.min(maxZ, c.lz0 + 15); lz++) {
                int columnHeight = height;
                if (ruin) {
                    double crumble = c.layout.random01(floor, c.worldX(lx) >> 2, c.worldZ(lz) >> 2, 21);
                    columnHeight = Math.max(1, (int) (height * (0.1 + 0.6 * crumble)) - (int) (c.noise(lx, 0, lz, 8) * 3));
                }
                boolean wall = lx == minX || lx == maxX || lz == minZ || lz == maxZ;
                boolean corner = (lx == minX || lx == maxX) && (lz == minZ || lz == maxZ);
                int along = (lx == minX || lx == maxX) ? lz : lx;
                for (int dy = 0; dy < columnHeight; dy++) {
                    int y = y0 + dy;
                    if (wall) {
                        boolean door = lz == minZ && (lx == doorX || lx == doorX + 1) && dy < 3;
                        if (door) {
                            continue;
                        }
                        int inStory = dy % STORY;
                        boolean window = !corner && (inStory == 2 || inStory == 3) && along % 3 != 0 && dy < columnHeight - 1;
                        if (window) {
                            if (!ruin && c.noise(lx, y, lz, 9) > 0.45) {
                                c.set(lx, y, lz, Blocks.GLASS.defaultBlockState());
                            }
                        } else {
                            c.set(lx, y, lz, wallBlock(c, style, lx, y, lz));
                        }
                    } else if (lx == ladderX && lz == ladderZ && !ruin) {
                        if (dy < columnHeight - 1) {
                            c.set(lx, y, lz, Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.EAST));
                        }
                    } else if (dy == columnHeight - 1 && !ruin && dy > 0) {
                        c.set(lx, y, lz, Blocks.SMOOTH_STONE.defaultBlockState());
                    } else if (dy > 0 && dy % STORY == 0 && (!ruin || c.noise(lx, y, lz, 10) > 0.5)) {
                        c.set(lx, y, lz, Blocks.SMOOTH_STONE.defaultBlockState());
                    } else if (ruin && dy == 0 && c.noise(lx, y, lz, 11) < 0.12) {
                        c.set(lx, y, lz, c.noise(lx, y, lz, 12) < 0.5 ? Blocks.COBBLESTONE.defaultBlockState()
                                : Blocks.GRAVEL.defaultBlockState());
                    }
                }
            }
        }

        // Crates on some stories, in the back corners.
        int stories = Math.max(1, (height - 1) / STORY);
        for (int s = 0; s < stories; s++) {
            double chance = ruin ? (s == 0 ? 0.3 : 0) : 0.22;
            if (c.layout.random01(floor, c.cx * 64 + s, c.cz, 40) < chance) {
                int y = y0 + (s == 0 ? 0 : s * STORY + 1);
                boolean left = c.layout.random01(floor, c.cx, c.cz * 64 + s, 41) < 0.5;
                c.crate(left ? minX + 2 : maxX - 1, y, maxZ - 1, ModLootTables.SUPPLY_CACHE);
            }
        }
        // An old sign by the door.
        if (!ruin && c.layout.random01(floor, c.cx, c.cz, 42) < 0.3) {
            c.sign(doorX + 3, y0 + 2, minZ, Direction.NORTH, floor);
        }
    }

    /** A low factory hall with an old food factory machine still inside. */
    private static void buildFactory(Chunk c, int floor) {
        int minX = 8, minZ = 8, maxX = 29, maxZ = 29;
        int y0 = CityLayout.floorY(floor);
        int height = Math.min(12, c.ceiling(floor) - y0 + 1);
        int doorZ = 18;
        for (int lx = Math.max(minX, c.lx0); lx <= Math.min(maxX, c.lx0 + 15); lx++) {
            for (int lz = Math.max(minZ, c.lz0); lz <= Math.min(maxZ, c.lz0 + 15); lz++) {
                boolean wall = lx == minX || lx == maxX || lz == minZ || lz == maxZ;
                for (int dy = 0; dy < height; dy++) {
                    int y = y0 + dy;
                    if (wall) {
                        boolean door = lx == minX && lz >= doorZ && lz <= doorZ + 2 && dy < 4;
                        boolean window = dy >= 7 && dy <= 9 && (lx + lz) % 4 != 0;
                        if (door || window) {
                            continue;
                        }
                        c.set(lx, y, lz, c.noise(lx, y, lz, 13) < 0.15 ? ModBlocks.RUSTED_PLATE.get().defaultBlockState() : Blocks.GRAY_CONCRETE.defaultBlockState());
                    } else if (dy == height - 1) {
                        boolean skylight = lx % 5 == 0 && c.noise(lx, y, lz, 14) < 0.7;
                        c.set(lx, y, lz, skylight ? Blocks.GLASS.defaultBlockState() : Blocks.IRON_BLOCK.defaultBlockState());
                    }
                }
            }
        }
        // Rows of old conveyor pipes inside.
        for (int lx = Math.max(minX + 3, c.lx0); lx <= Math.min(maxX - 3, c.lx0 + 15); lx++) {
            for (int lz : new int[]{12, 25}) {
                c.set(lx, y0 + 4, lz, ModBlocks.PIPE.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.X));
            }
        }
        c.special(18, y0, 18, ModBlocks.FOOD_FACTORY.get().defaultBlockState().setValue(FoodFactoryBlock.FACING, Direction.WEST));
        c.crate(maxX - 1, y0, maxZ - 1, ModLootTables.SUPPLY_CACHE);
        c.crate(maxX - 1, y0, minZ + 1, ModLootTables.SUPPLY_CACHE);
        c.sign(minX, y0 + 4, doorZ + 1, Direction.WEST, floor);
        if (c.isFarQuarter()) {
            c.spawn(ModEntities.ROBOT.get(), 22, y0, 24, robot -> robot.setHome(robot.blockPosition(), false));
        }
    }

    /** An open square: maybe a statue, a cairn, benches. */
    private static void buildPlaza(Chunk c, int floor) {
        if (c.isArrival(floor)) {
            return; // keep it clear for the elevator or stairs arriving here
        }
        int y0 = CityLayout.floorY(floor);
        double roll = c.layout.random01(floor, c.cx, c.cz, 43);
        if (roll < 0.25) {
            for (int lx = 17; lx <= 19; lx++) {
                for (int lz = 17; lz <= 19; lz++) {
                    c.set(lx, y0, lz, Blocks.STONE_BRICKS.defaultBlockState());
                }
            }
            c.set(18, y0 + 1, 18, ModBlocks.STATUE.get().defaultBlockState().setValue(StatueBlock.FACING, Direction.NORTH));
        } else if (roll < 0.4) {
            c.set(12, y0, 26, ModBlocks.CAIRN.get().defaultBlockState());
        }
        // Now and then, a traveler resting here: Kanazawa, the mapmaker.
        if (floor >= 1 && c.isFarQuarter() && c.layout.random01(floor, c.cx, c.cz, 46) < 0.08) {
            c.spawn(ModEntities.KANAZAWA.get(), 22, y0, 24, null);
        }
        // Benches along one side.
        for (int lx = 10; lx <= 26; lx += 4) {
            c.set(lx, y0, 8, Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH));
        }
    }

    /** Giant pipes: one lying across the lot and two climbing up to the floor above. Some still carry hot water. */
    private static void buildPipes(Chunk c, int floor) {
        int y0 = CityLayout.floorY(floor);
        boolean hot = c.layout.random01(floor, c.cx, c.cz, 50) < 0.4;
        BlockState lying = (hot ? ModBlocks.HOT_PIPE : ModBlocks.PIPE).get().defaultBlockState()
                .setValue(RotatedPillarBlock.AXIS, Direction.Axis.X);
        int centerY = y0 + 3;
        for (int lx = Math.max(CityLayout.ROAD_WIDTH, c.lx0); lx <= c.lx0 + 15; lx++) {
            for (int dz = -2; dz <= 2; dz++) {
                for (int dy = -2; dy <= 2; dy++) {
                    if (dz * dz + dy * dy <= 6) {
                        c.set(lx, centerY + dy, 18 + dz, lying);
                    }
                }
            }
        }
        BlockState standing = ModBlocks.PIPE.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
        int top = CityLayout.isTopFloor(floor) ? y0 + 20 : c.ceiling(floor);
        for (int y = y0; y <= top; y++) {
            for (int a = 0; a < 3; a++) {
                for (int b = 0; b < 3; b++) {
                    c.set(8 + a, y, 26 + b, standing);
                    c.set(26 + a, y, 8 + b, standing);
                }
            }
        }
        if (c.isFarQuarter() && c.layout.random01(floor, c.cx, c.cz, 47) < 0.3) {
            c.spawn(ModEntities.ROBOT.get(), 20, y0, 24, robot -> robot.setHome(robot.blockPosition(), false));
        }
        if (hot) {
            // A crack in a hot pipe, and the warm pool it fills: a bath.
            c.set(12, y0, 20, ModBlocks.LEAKING_HOT_PIPE.get().defaultBlockState());
            for (int lx = 10; lx <= 14; lx++) {
                for (int lz = 21; lz <= 23; lz++) {
                    c.set(lx, y0 - 1, lz, Blocks.WATER.defaultBlockState());
                    c.set(lx, y0 - 2, lz, Blocks.WATER.defaultBlockState());
                }
            }
        }
    }

    /** An old battlefield: a wrecked tank and scattered armor plates. */
    private static void buildTanks(Chunk c, int floor) {
        int y0 = CityLayout.floorY(floor);
        for (int lx = Math.max(CityLayout.ROAD_WIDTH, c.lx0); lx <= c.lx0 + 15; lx++) {
            for (int lz = Math.max(CityLayout.ROAD_WIDTH, c.lz0); lz <= c.lz0 + 15; lz++) {
                double n = c.noise(lx, y0, lz, 15);
                if (n < 0.015) {
                    c.set(lx, y0, lz, ModBlocks.RUSTED_PLATE.get().defaultBlockState());
                } else if (n < 0.03) {
                    c.set(lx, y0, lz, Blocks.GRAVEL.defaultBlockState());
                }
            }
        }
        if (c.isFarQuarter()) {
            c.placeFeature(ModFeatures.WRECKED_TANK.get(), 24, y0, 24);
        }
    }

    /** A little temple with statues, and stone lanterns. */
    private static void buildTemple(Chunk c, int floor) {
        int y0 = CityLayout.floorY(floor);
        for (int lz = 10; lz <= 28; lz += 6) {
            for (int lx : new int[]{10, 28}) {
                c.set(lx, y0, lz, Blocks.STONE_BRICK_WALL.defaultBlockState());
                c.set(lx, y0 + 1, lz, Blocks.LANTERN.defaultBlockState());
            }
        }
        if (c.isFarQuarter()) {
            c.placeFeature(ModFeatures.SHRINE.get(), 24, y0, 24);
        }
    }

    /** A covered pool where fish are still farmed. Its keeper robot comes later. */
    private static void buildFishFarm(Chunk c, int floor) {
        int minX = 8, minZ = 8, maxX = 29, maxZ = 29;
        int y0 = CityLayout.floorY(floor);
        int height = 7;
        for (int lx = Math.max(minX, c.lx0); lx <= Math.min(maxX, c.lx0 + 15); lx++) {
            for (int lz = Math.max(minZ, c.lz0); lz <= Math.min(maxZ, c.lz0 + 15); lz++) {
                boolean wall = lx == minX || lx == maxX || lz == minZ || lz == maxZ;
                boolean pool = lx >= minX + 3 && lx <= maxX - 3 && lz >= minZ + 3 && lz <= maxZ - 3;
                for (int dy = 0; dy < height; dy++) {
                    int y = y0 + dy;
                    if (wall) {
                        boolean door = lz == minZ && (lx == 18 || lx == 19) && dy < 3;
                        if (!door) {
                            c.set(lx, y, lz, dy == 3 || dy == 4 ? Blocks.GLASS.defaultBlockState() : Blocks.STONE_BRICKS.defaultBlockState());
                        }
                    } else if (dy == height - 1) {
                        boolean lamp = (lx - minX) % 6 == 3 && (lz - minZ) % 6 == 3;
                        c.set(lx, y, lz, lamp ? Blocks.SEA_LANTERN.defaultBlockState() : Blocks.GLASS.defaultBlockState());
                    }
                }
                if (pool) {
                    c.set(lx, y0 - 1, lz, Blocks.WATER.defaultBlockState());
                    c.set(lx, y0 - 2, lz, Blocks.WATER.defaultBlockState());
                    if (c.noise(lx, y0, lz, 16) < 0.08) {
                        c.set(lx, y0 - 3, lz, Blocks.SAND.defaultBlockState());
                        c.set(lx, y0 - 2, lz, Blocks.SEAGRASS.defaultBlockState());
                    }
                }
            }
        }
        c.sign(17, y0 + 2, minZ, Direction.NORTH, floor);
        if (c.isFarQuarter()) {
            for (int i = 0; i < 6; i++) {
                c.fish(18 + (i % 3) * 3, y0 - 1, 18 + (i / 3) * 4);
            }
            BlockPos pool = new BlockPos(c.worldX(20), y0 - 1, c.worldZ(20));
            c.spawn(ModEntities.ROBOT.get(), 28, y0, 24, robot -> robot.setHome(pool, true));
        }
    }

    /** A giant elevator: a platform in an iron frame, and a console on this floor and the one above. */
    private static void buildElevator(Chunk c, int floor) {
        if (!c.isFarQuarter()) {
            return;
        }
        int y0 = CityLayout.floorY(floor);
        int ceiling = c.ceiling(floor);
        int upperY = CityLayout.floorY(floor + 1);
        BlockState platform = ModBlocks.ELEVATOR_PLATFORM.get().defaultBlockState();
        for (int lx = CityLayout.PLATFORM_MIN; lx <= CityLayout.PLATFORM_MAX; lx++) {
            for (int lz = CityLayout.PLATFORM_MIN; lz <= CityLayout.PLATFORM_MAX; lz++) {
                c.set(lx, y0 - 1, lz, platform);
            }
        }
        int min = CityLayout.FRAME_MIN;
        int max = CityLayout.FRAME_MAX;
        for (int y = y0; y <= ceiling; y++) {
            for (int[] corner : new int[][]{{min, min}, {min, max}, {max, min}, {max, max}}) {
                c.set(corner[0], y, corner[1], Blocks.IRON_BLOCK.defaultBlockState());
            }
        }
        for (int i = min + 1; i < max; i++) {
            c.set(i, ceiling, min, Blocks.IRON_BLOCK.defaultBlockState());
            c.set(i, ceiling, max, Blocks.IRON_BLOCK.defaultBlockState());
            c.set(min, ceiling, i, Blocks.IRON_BLOCK.defaultBlockState());
        }
        // Railings on three sides, here and around the hole upstairs. The east side is the way on.
        for (int i = min; i <= max; i++) {
            for (int y : new int[]{y0, upperY}) {
                c.setConnecting(i, y, min, Blocks.IRON_BARS.defaultBlockState());
                c.setConnecting(i, y, max, Blocks.IRON_BARS.defaultBlockState());
                c.setConnecting(min, y, i, Blocks.IRON_BARS.defaultBlockState());
            }
        }
        BlockPos lower = c.special(CityLayout.CONSOLE_X, y0, CityLayout.CONSOLE_Z,
                ModBlocks.ELEVATOR_CONSOLE.get().defaultBlockState().setValue(ElevatorConsoleBlock.FACING, Direction.WEST));
        BlockPos upper = c.special(CityLayout.CONSOLE_X, upperY, CityLayout.CONSOLE_Z,
                ModBlocks.ELEVATOR_CONSOLE.get().defaultBlockState().setValue(ElevatorConsoleBlock.FACING, Direction.WEST));
        if (lower != null && c.level.getBlockEntity(lower) instanceof ElevatorConsoleBlockEntity controller) {
            controller.setupController(c.worldX(CityLayout.PLATFORM_MIN), c.worldZ(CityLayout.PLATFORM_MIN),
                    CityLayout.PLATFORM_MAX - CityLayout.PLATFORM_MIN + 1, y0 - 1, upperY - 1);
            if (upper != null && c.level.getBlockEntity(upper) instanceof ElevatorConsoleBlockEntity remote) {
                remote.linkTo(lower);
            }
        }
        c.lamp(29, y0, 25);
        c.lamp(29, upperY, 25);
        // A cache by the elevator. The very first one always has fuel, so you can start climbing.
        boolean first = floor == 0 && c.cx == 1 && c.cz == 1;
        if (first) {
            c.crate(29, y0, 19, ModLootTables.FIRST_CACHE);
        } else if (c.layout.random01(floor, c.cx, c.cz, 44) < 0.4) {
            c.crate(29, y0, 19, ModLootTables.SUPPLY_CACHE);
        }
    }

    /**
     * A stair tower: a spiral ramp around a stone core, from this floor up through the floor above.
     * It rises a quarter block per step, in half-block slabs, so the Kettenkrad can drive up it too.
     */
    private static void buildStairTower(Chunk c, int floor) {
        if (!c.isFarQuarter()) {
            return;
        }
        int y0 = CityLayout.floorY(floor);
        int ceiling = c.ceiling(floor);
        int rise = CityLayout.FLOOR_SPACING;
        int min = CityLayout.TOWER_MIN;
        int max = CityLayout.TOWER_MAX;
        for (int lx = min; lx <= max; lx++) {
            for (int lz = min; lz <= max; lz++) {
                boolean wall = lx == min || lx == max || lz == min || lz == max;
                boolean core = lx >= CityLayout.CORE_MIN && lx <= CityLayout.CORE_MAX
                        && lz >= CityLayout.CORE_MIN && lz <= CityLayout.CORE_MAX;
                if (wall) {
                    int along = (lx == min || lx == max) ? lz : lx;
                    for (int y = y0; y <= ceiling; y++) {
                        int dy = y - y0;
                        boolean door = lx == max && lz >= 24 && lz <= 26 && dy < 3;
                        boolean window = dy % 8 >= 3 && dy % 8 <= 4 && along % 4 == 2;
                        if (!door && !window) {
                            c.set(lx, y, lz, Blocks.STONE_BRICKS.defaultBlockState());
                        }
                    }
                } else if (core) {
                    boolean face = lx == CityLayout.CORE_MIN || lx == CityLayout.CORE_MAX
                            || lz == CityLayout.CORE_MIN || lz == CityLayout.CORE_MAX;
                    for (int y = y0; y <= ceiling; y++) {
                        boolean light = face && (y - y0) % 8 == 4 && (lx + lz) % 3 == 0;
                        c.set(lx, y, lz, light ? Blocks.GLOWSTONE.defaultBlockState() : Blocks.STONE.defaultBlockState());
                    }
                } else {
                    double angle = Math.atan2(lz + 0.5 - 24.0, lx + 0.5 - 24.0);
                    if (angle < 0) {
                        angle += Math.PI * 2;
                    }
                    double position = angle / (Math.PI * 2) * RAMP_UNITS_PER_LAP;
                    for (int lap = 0; ; lap++) {
                        double height = (lap * RAMP_UNITS_PER_LAP + position) * RAMP_RISE_PER_UNIT;
                        if (height > rise) {
                            break;
                        }
                        int halfSteps = (int) Math.floor(height * 2);
                        int y = y0 + halfSteps / 2;
                        if (halfSteps % 2 == 0) {
                            if (y - 1 >= y0) {
                                c.set(lx, y - 1, lz, Blocks.SMOOTH_STONE.defaultBlockState());
                            }
                        } else {
                            c.set(lx, y, lz, Blocks.SMOOTH_STONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM));
                        }
                    }
                }
            }
        }
        // Upstairs: a railing around the stairwell, open where the ramp comes out (east side, north of the middle).
        int upperY = CityLayout.floorY(floor + 1);
        for (int i = min; i <= max; i++) {
            c.setConnecting(i, upperY, min, Blocks.IRON_BARS.defaultBlockState());
            c.setConnecting(i, upperY, max, Blocks.IRON_BARS.defaultBlockState());
            c.setConnecting(min, upperY, i, Blocks.IRON_BARS.defaultBlockState());
            if (i < 19 || i > 23) {
                c.setConnecting(max, upperY, i, Blocks.IRON_BARS.defaultBlockState());
            }
        }
        c.sign(max, y0 + 3, 23, Direction.EAST, floor);
        c.lamp(31, y0, 28);
    }

    /** A flat runway with a little workshop, where Ishii is building her plane. */
    private static void buildAirfield(Chunk c, int floor) {
        int y0 = CityLayout.floorY(floor);
        // Runway markings down the middle.
        for (int lx = Math.max(CityLayout.ROAD_WIDTH, c.lx0); lx <= c.lx0 + 15; lx++) {
            if (lx % 4 < 2) {
                c.set(lx, y0 - 1, 18, Blocks.WHITE_CONCRETE.defaultBlockState());
                c.set(lx, y0 - 1, 19, Blocks.WHITE_CONCRETE.defaultBlockState());
            }
        }
        if (!c.isFarQuarter()) {
            return;
        }
        // A lean-to workshop in the corner: two walls and a roof.
        for (int lx = 24; lx <= 30; lx++) {
            for (int lz = 24; lz <= 30; lz++) {
                boolean wall = lx == 30 || lz == 30;
                if (wall) {
                    for (int dy = 0; dy < 4; dy++) {
                        c.set(lx, y0 + dy, lz, ModBlocks.RUSTED_PLATE.get().defaultBlockState());
                    }
                }
                c.set(lx, y0 + 4, lz, Blocks.SMOOTH_STONE_SLAB.defaultBlockState());
            }
        }
        c.set(29, y0, 26, Blocks.ANVIL.defaultBlockState());
        c.set(29, y0, 25, ModBlocks.PIPE.get().defaultBlockState());
        c.crate(29, y0, 28, ModLootTables.SUPPLY_CACHE);
        c.sign(27, y0 + 2, 30, Direction.NORTH, floor);
        c.lamp(23, y0, 31);
        c.spawn(ModEntities.ISHII.get(), 26, y0, 27, null);
    }

    /** The very top of the city: an empty square with one big black stone. */
    private static void buildSummit(Chunk c, int floor) {
        int y0 = CityLayout.floorY(floor);
        for (int lx = 17; lx <= 19; lx++) {
            for (int lz = 17; lz <= 19; lz++) {
                for (int dy = 0; dy < 12; dy++) {
                    c.set(lx, y0 + dy, lz, Blocks.POLISHED_BLACKSTONE.defaultBlockState());
                }
            }
        }
    }

    /** Street lamps in lit districts, and the odd crate or pile of rubble on the road. */
    private static void buildStreet(Chunk c, int floor) {
        int y0 = CityLayout.floorY(floor);
        boolean district = floor == 0 && c.cx >= 0 && c.cx <= 1 && c.cz >= 0 && c.cz <= 1
                || c.layout.random01(floor, c.cx >> 1, c.cz >> 1, 31) < 0.55 - 0.06 * floor;
        if (district) {
            c.lamp(5, y0, 14);
            c.lamp(14, y0, 5);
            c.lamp(5, y0, 28);
            c.lamp(28, y0, 5);
        }
        for (int lx = c.lx0; lx < c.lx0 + 16; lx++) {
            for (int lz = c.lz0; lz < c.lz0 + 16; lz++) {
                boolean road = lx < CityLayout.ROAD_WIDTH || lz < CityLayout.ROAD_WIDTH;
                boolean pillar = lx >= 1 && lx <= 4 && lz >= 1 && lz <= 4 && !CityLayout.isTopFloor(floor);
                if (road && !pillar && c.noise(lx, y0, lz, 17) < 0.008) {
                    c.set(lx, y0, lz, Blocks.COBBLESTONE.defaultBlockState());
                }
            }
        }
        if (c.layout.random01(floor, c.cx, c.cz, 60) < 0.2) {
            c.crate(4, y0, 12, ModLootTables.SUPPLY_CACHE);
        }
    }

    // ---------- Writing blocks into one chunk ----------

    /** One chunk being built, plus helpers that use lot-local coordinates (0..31). */
    private static final class Chunk {
        final WorldGenLevel level;
        final FeaturePlaceContext<NoneFeatureConfiguration> context;
        final ChunkAccess access;
        final CityLayout layout;
        final int cx;
        final int cz;
        /** Lot-local coordinates of this chunk's first column (0 or 16). */
        final int lx0;
        final int lz0;
        final int cellX0;
        final int cellZ0;
        final RandomSource random;
        private final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        Chunk(FeaturePlaceContext<NoneFeatureConfiguration> context, ChunkAccess access, ChunkPos pos, CityLayout layout) {
            this.context = context;
            this.level = context.level();
            this.access = access;
            this.layout = layout;
            this.cx = CityLayout.cellOf(pos.getMinBlockX());
            this.cz = CityLayout.cellOf(pos.getMinBlockZ());
            this.lx0 = CityLayout.local(pos.getMinBlockX());
            this.lz0 = CityLayout.local(pos.getMinBlockZ());
            this.cellX0 = cx * CityLayout.CELL;
            this.cellZ0 = cz * CityLayout.CELL;
            this.random = RandomSource.create(layout.hash(pos.x, pos.z, 0, 99));
        }

        int worldX(int lx) {
            return cellX0 + lx;
        }

        int worldZ(int lz) {
            return cellZ0 + lz;
        }

        boolean contains(int lx, int lz) {
            return lx >= lx0 && lx < lx0 + 16 && lz >= lz0 && lz < lz0 + 16;
        }

        /** True if this chunk is the lot's far quarter (local 16..31 on both axes). */
        boolean isFarQuarter() {
            return lx0 == 16 && lz0 == 16;
        }

        /** The highest air block on a floor, just under the floor above (or high in the sky on the top floor). */
        int ceiling(int floor) {
            if (CityLayout.isTopFloor(floor)) {
                return level.getMaxBuildHeight() - 8;
            }
            return CityLayout.floorY(floor + 1) - CityLayout.SLAB_THICKNESS - 1;
        }

        /** True if an elevator or stair tower from the floor below comes out in this lot. */
        boolean isArrival(int floor) {
            if (floor == 0) {
                return false;
            }
            Lot below = layout.lot(floor - 1, cx, cz);
            return below == Lot.ELEVATOR || below == Lot.STAIRS;
        }

        double noise(int lx, int y, int lz, int salt) {
            return layout.blockNoise(worldX(lx), y, worldZ(lz), salt);
        }

        /** Fast write for plain blocks. */
        void set(int lx, int y, int lz, BlockState state) {
            if (!contains(lx, lz) || y < level.getMinBuildHeight() || y >= level.getMaxBuildHeight()) {
                return;
            }
            access.setBlockState(cursor.set(worldX(lx), y, worldZ(lz)), state, false);
        }

        /** Write for bars and panes: their connections are fixed up when the chunk finishes. */
        void setConnecting(int lx, int y, int lz, BlockState state) {
            if (!contains(lx, lz)) {
                return;
            }
            set(lx, y, lz, state);
            access.markPosForPostprocessing(new BlockPos(worldX(lx), y, worldZ(lz)));
        }

        /** Write for blocks with a block entity or a scheduled tick. Returns the position, or null if outside this chunk. */
        BlockPos special(int lx, int y, int lz, BlockState state) {
            if (!contains(lx, lz)) {
                return null;
            }
            BlockPos pos = new BlockPos(worldX(lx), y, worldZ(lz));
            level.setBlock(pos, state, 2);
            return pos;
        }

        void crate(int lx, int y, int lz, ResourceKey<LootTable> table) {
            BlockPos pos = special(lx, y, lz, ModBlocks.SUPPLY_CRATE.get().defaultBlockState());
            if (pos != null && level.getBlockEntity(pos) instanceof LootContainerBlockEntity crate) {
                crate.setLootTable(table, random.nextLong());
            }
        }

        void sign(int lx, int y, int lz, Direction facing, int floor) {
            int text = (int) (layout.random01(floor, worldX(lx), worldZ(lz), 45) * OldSignBlock.TEXT_COUNT);
            set(lx, y, lz, ModBlocks.OLD_SIGN.get().defaultBlockState()
                    .setValue(OldSignBlock.FACING, facing).setValue(OldSignBlock.TEXT, text));
        }

        void lamp(int lx, int y, int lz) {
            if (!contains(lx, lz)) {
                return;
            }
            for (int up = 0; up < 3; up++) {
                set(lx, y + up, lz, ModBlocks.LAMP_POST.get().defaultBlockState());
            }
            BlockPos lamp = special(lx, y + 3, lz, ModBlocks.STREET_LAMP.get().defaultBlockState());
            if (lamp != null) {
                level.scheduleTick(lamp, ModBlocks.STREET_LAMP.get(), 20 + random.nextInt(40));
            }
        }

        void fish(int lx, int y, int lz) {
            if (!contains(lx, lz)) {
                return;
            }
            Cod cod = EntityType.COD.create(level.getLevel());
            if (cod != null) {
                cod.moveTo(worldX(lx) + 0.5, y + 0.2, worldZ(lz) + 0.5, random.nextFloat() * 360f, 0f);
                cod.setFromBucket(true); // so they don't despawn
                level.addFreshEntity(cod);
            }
        }

        /** Adds a creature standing at this spot, and lets the caller set it up. */
        <T extends Mob> void spawn(EntityType<T> type, int lx, int y, int lz, Consumer<T> setup) {
            if (!contains(lx, lz)) {
                return;
            }
            T mob = type.create(level.getLevel());
            if (mob != null) {
                mob.moveTo(worldX(lx) + 0.5, y, worldZ(lz) + 0.5, random.nextFloat() * 360f, 0f);
                mob.setPersistenceRequired();
                if (setup != null) {
                    setup.accept(mob);
                }
                level.addFreshEntity(mob);
            }
        }

        /** Places one of the mod's small features (tank, shrine) with its middle at this spot. */
        void placeFeature(Feature<NoneFeatureConfiguration> feature, int lx, int y, int lz) {
            if (!contains(lx, lz)) {
                return;
            }
            feature.place(NoneFeatureConfiguration.INSTANCE, level, context.chunkGenerator(), random,
                    new BlockPos(worldX(lx), y, worldZ(lz)));
        }
    }
}
