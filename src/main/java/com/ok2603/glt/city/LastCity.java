package com.ok2603.glt.city;

import com.ok2603.glt.GLTMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;

/**
 * The "Last City" world type. Pick it on the world creation screen (World Type: Last City).
 *
 * How it is made, all in data files plus one feature:
 *  - data/glt/worldgen/world_preset/last_city.json swaps the Overworld for a flat, empty world
 *    with the dimension type glt:last_city (512 blocks tall) and the biome glt:dead_city.
 *  - The biome's only feature, glt:city (CityFeature), builds the whole city one chunk at a time
 *    from the plan in CityLayout.
 * Nether and End stay as in vanilla.
 */
public final class LastCity {
    public static final ResourceKey<DimensionType> DIMENSION_TYPE =
            ResourceKey.create(Registries.DIMENSION_TYPE, ResourceLocation.fromNamespaceAndPath(GLTMod.MODID, "last_city"));

    /** Where new players start: on the bottom floor, on a road next to the first elevator's block. */
    public static final BlockPos SPAWN = new BlockPos(3, CityLayout.floorY(0), 20);

    private LastCity() {
    }

    /** True if this level is a Last City world. */
    public static boolean isCity(Level level) {
        return level.dimensionTypeRegistration().is(DIMENSION_TYPE);
    }

    /** Which floor of the city this y is on (0 = bottom). */
    public static int floor(double y) {
        return CityLayout.floorAt((int) Math.floor(y));
    }
}
