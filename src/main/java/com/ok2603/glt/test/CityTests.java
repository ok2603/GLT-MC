package com.ok2603.glt.test;

import com.ok2603.glt.GLTMod;
import com.ok2603.glt.block.ElevatorConsoleBlockEntity;
import com.ok2603.glt.city.CityLayout;
import com.ok2603.glt.city.CityLayout.Lot;
import com.ok2603.glt.city.LastCity;
import com.ok2603.glt.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Automatic checks for the Last City world. */
@GameTestHolder(GLTMod.MODID)
@PrefixGameTestTemplate(false)
public class CityTests {
    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(GLTMod.MODID, path);
    }

    /** The world type, its biome, dimension type and city feature all loaded from the data files. */
    @GameTest(template = "empty")
    public static void cityDataLoads(GameTestHelper helper) {
        var access = helper.getLevel().registryAccess();
        helper.assertTrue(access.registryOrThrow(Registries.WORLD_PRESET).containsKey(id("last_city")), "World preset missing");
        helper.assertTrue(access.registryOrThrow(Registries.BIOME).containsKey(id("dead_city")), "Biome missing");
        helper.assertTrue(access.registryOrThrow(Registries.DIMENSION_TYPE).containsKey(LastCity.DIMENSION_TYPE), "Dimension type missing");
        helper.assertTrue(access.registryOrThrow(Registries.PLACED_FEATURE).containsKey(id("city")), "City feature missing");
        helper.succeed();
    }

    /** The plan makes sense for many seeds: you can always get up, and arrivals are never blocked. */
    @GameTest(template = "empty")
    public static void cityLayoutIsClimbable(GameTestHelper helper) {
        for (long seed = 0; seed < 20; seed++) {
            CityLayout layout = new CityLayout(seed * 7919L);
            helper.assertTrue(layout.lot(0, 1, 1) == Lot.ELEVATOR, "First elevator missing for seed " + seed);
            for (int floor = 0; floor < CityLayout.FLOORS - 1; floor++) {
                for (int rx = -2; rx <= 2; rx++) {
                    for (int rz = -2; rz <= 2; rz++) {
                        int[] cell = layout.elevatorCellInRegion(floor, rx, rz);
                        helper.assertTrue(layout.lot(floor, cell[0], cell[1]) == Lot.ELEVATOR,
                                "Elevator cell isn't an elevator lot (seed " + seed + ", floor " + floor + ")");
                        helper.assertTrue(layout.lot(floor + 1, cell[0], cell[1]) == Lot.PLAZA,
                                "Elevator arrives into something (seed " + seed + ", floor " + floor + ")");
                    }
                }
            }
        }
        BlockPos spawn = LastCity.SPAWN;
        helper.assertTrue(CityLayout.isRoad(spawn.getX(), spawn.getZ()) && !CityLayout.isPillar(spawn.getX(), spawn.getZ()),
                "Spawn is not on an open road");
        helper.assertTrue(CityLayout.floorAt(CityLayout.floorY(3)) == 3, "floorAt and floorY disagree");
        helper.succeed();
    }

    /** An elevator lifts its platform and whatever stands on it, then stops at the top. */
    @GameTest(template = "empty", timeoutTicks = 200)
    public static void elevatorLiftsRiders(GameTestHelper helper) {
        int size = 3;
        BlockPos min = new BlockPos(2, 1, 2);
        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size; z++) {
                helper.setBlock(min.offset(x, 0, z), ModBlocks.ELEVATOR_PLATFORM.get());
            }
        }
        BlockPos consolePos = new BlockPos(6, 1, 3);
        helper.setBlock(consolePos, ModBlocks.ELEVATOR_CONSOLE.get());
        BlockPos absMin = helper.absolutePos(min);
        int rise = 4;
        ElevatorConsoleBlockEntity console = helper.getBlockEntity(consolePos);
        console.setupController(absMin.getX(), absMin.getZ(), size, absMin.getY(), absMin.getY() + rise);
        ArmorStand rider = helper.spawn(EntityType.ARMOR_STAND, min.offset(1, 1, 1));
        double startY = rider.getY();
        console.start(1);

        helper.succeedWhen(() -> {
            helper.assertFalse(console.isMoving(), "Still moving");
            helper.assertBlockPresent(ModBlocks.ELEVATOR_PLATFORM.get(), min.offset(1, rise, 1));
            helper.assertBlockPresent(Blocks.AIR, min.offset(1, 0, 1));
            helper.assertTrue(rider.getY() > startY + rise - 0.5, "The rider was left behind");
        });
    }
}
