package com.ok2603.glt.city;

/**
 * The plan of the Last City: where every floor, road, building, pipe and elevator is.
 *
 * Everything here is a pure function of the world seed and the coordinates, so any part
 * of the city can be built on its own (one chunk at a time) and always comes out the same.
 *
 * The city is a stack of FLOORS concrete slabs, FLOOR_SPACING blocks apart. Each floor is
 * split into CELL x CELL squares: roads run along the edges of each square, and the inside
 * holds one "lot": a building, a ruin, a plaza, giant pipes, a hole down to the floor below, etc.
 */
public final class CityLayout {
    public static final int FLOORS = 8;
    public static final int FLOOR_SPACING = 56;
    /** Walking surface of floor 0. */
    public static final int BASE_Y = 8;
    public static final int SLAB_THICKNESS = 4;
    public static final int CELL = 32;
    public static final int ROAD_WIDTH = 6;
    /** Elevators: one per REGION x REGION cells on every floor except the top. */
    public static final int REGION = 4;
    public static final int WORLD_HEIGHT = 512;

    public enum Lot {
        BUILDING, RUIN, PLAZA, PIPES, HOLE, TANKS, TEMPLE, FACTORY, STAIRS, ELEVATOR, FISH_FARM, AIRFIELD, SUMMIT
    }

    private final long seed;
    /** Elevator positions already worked out (they depend on the floors below, so this saves a lot of work). */
    private final java.util.Map<Long, int[]> elevatorCache = new java.util.HashMap<>();

    public CityLayout(long seed) {
        this.seed = seed;
    }

    // ---------- Floors ----------

    /** The y of the first air block above floor k's slab (where you stand). */
    public static int floorY(int floor) {
        return BASE_Y + floor * FLOOR_SPACING;
    }

    /** Which floor a y coordinate belongs to (the floor you'd be standing on). */
    public static int floorAt(int y) {
        int floor = Math.floorDiv(y - BASE_Y + SLAB_THICKNESS, FLOOR_SPACING);
        return Math.max(0, Math.min(FLOORS - 1, floor));
    }

    public static boolean isTopFloor(int floor) {
        return floor == FLOORS - 1;
    }

    // ---------- Cells ----------

    public static int cellOf(int blockCoord) {
        return Math.floorDiv(blockCoord, CELL);
    }

    public static int local(int blockCoord) {
        return Math.floorMod(blockCoord, CELL);
    }

    public static boolean isRoad(int x, int z) {
        return local(x) < ROAD_WIDTH || local(z) < ROAD_WIDTH;
    }

    /** True at the big support pillars where roads cross (not on the top floor). */
    public static boolean isPillar(int x, int z) {
        int lx = local(x);
        int lz = local(z);
        return lx >= 1 && lx <= 4 && lz >= 1 && lz <= 4;
    }

    /** What is built on the lot of cell (cx, cz) on this floor. */
    public Lot lot(int floor, int cx, int cz) {
        if (isTopFloor(floor) && cx == 0 && cz == 0) {
            return Lot.SUMMIT;
        }
        if (!isTopFloor(floor) && isElevatorCell(floor, cx, cz)) {
            return Lot.ELEVATOR;
        }
        // The cell just above an elevator or stair tower on the floor below stays open, so you can arrive.
        if (floor > 0 && (isElevatorCell(floor - 1, cx, cz) || isStairsCell(floor - 1, cx, cz))) {
            return Lot.PLAZA;
        }
        if (!isTopFloor(floor) && isStairsCell(floor, cx, cz)) {
            return Lot.STAIRS;
        }
        // Higher floors are emptier: fewer buildings, more ruins and empty plazas.
        double emptiness = floor / (double) (FLOORS - 1);
        double roll = random01(floor, cx, cz, 1);
        double building = 0.42 - 0.2 * emptiness;
        double ruin = 0.16 + 0.1 * emptiness;
        double plaza = 0.10 + 0.08 * emptiness;
        double pipes = 0.10;
        double hole = floor == 0 ? 0 : 0.06;
        double tanks = 0.05;
        double temple = 0.035;
        double factory = 0.03;
        double fishFarm = floor <= 3 ? 0.02 : 0.0;
        double airfield = floor >= 3 && floor <= 6 ? 0.012 : 0.0;
        double[] weights = {building, ruin, plaza, pipes, hole, tanks, temple, factory, fishFarm, airfield};
        Lot[] lots = {Lot.BUILDING, Lot.RUIN, Lot.PLAZA, Lot.PIPES, Lot.HOLE, Lot.TANKS, Lot.TEMPLE, Lot.FACTORY, Lot.FISH_FARM, Lot.AIRFIELD};
        double total = 0;
        for (double w : weights) {
            total += w;
        }
        double pick = roll * total;
        for (int i = 0; i < weights.length; i++) {
            pick -= weights[i];
            if (pick < 0) {
                return lots[i];
            }
        }
        return Lot.PLAZA;
    }

    // ---------- Elevators and stairs ----------

    /**
     * The elevator cell inside a region on a floor. It never sits right above the elevator or
     * stair tower of the floor below, because those cells are where you arrive.
     */
    public int[] elevatorCellInRegion(int floor, int rx, int rz) {
        long key = ((long) floor << 48) ^ ((long) (rx & 0xFFFFFF) << 24) ^ (rz & 0xFFFFFF);
        int[] cached = elevatorCache.get(key);
        if (cached != null) {
            return cached;
        }
        int[] cell = findElevatorCell(floor, rx, rz);
        elevatorCache.put(key, cell);
        return cell;
    }

    private int[] findElevatorCell(int floor, int rx, int rz) {
        // The very first elevator is always close to where you start.
        if (rx == 0 && rz == 0 && floor == 0) {
            return new int[]{1, 1};
        }
        int ox = (int) (random01(floor, rx, rz, 7) * REGION);
        int oz = (int) (random01(floor, rx, rz, 8) * REGION);
        for (int attempt = 0; attempt < REGION * REGION; attempt++) {
            int cx = rx * REGION + (ox + attempt) % REGION;
            int cz = rz * REGION + (oz + attempt / REGION) % REGION;
            if ((floor == 0 || !arrivalCell(floor - 1, cx, cz)) && !belowSummit(floor, cx, cz)) {
                return new int[]{cx, cz};
            }
        }
        return new int[]{rx * REGION + ox, rz * REGION + oz};
    }

    /** The summit square on the top floor is left alone: nothing comes up right into it. */
    private static boolean belowSummit(int floor, int cx, int cz) {
        return floor == FLOORS - 2 && cx == 0 && cz == 0;
    }

    /** True where something from floor k arrives on floor k + 1 (an elevator or stair tower). */
    private boolean arrivalCell(int floor, int cx, int cz) {
        return isElevatorCell(floor, cx, cz) || isStairsCell(floor, cx, cz);
    }

    public boolean isElevatorCell(int floor, int cx, int cz) {
        if (floor < 0 || isTopFloor(floor)) {
            return false;
        }
        int[] cell = elevatorCellInRegion(floor, Math.floorDiv(cx, REGION), Math.floorDiv(cz, REGION));
        return cell[0] == cx && cell[1] == cz;
    }

    /** Spiral stair towers: a few per region, so you can climb without fuel (slowly). */
    public boolean isStairsCell(int floor, int cx, int cz) {
        if (floor < 0 || isTopFloor(floor) || isElevatorCell(floor, cx, cz) || belowSummit(floor, cx, cz)) {
            return false;
        }
        if (floor > 0 && (isElevatorCell(floor - 1, cx, cz) || isStairsCell(floor - 1, cx, cz))) {
            return false;
        }
        return random01(floor, cx, cz, 9) < 0.035;
    }

    // Elevators and stair towers sit in the far quarter of their lot (local 16..31 on both axes),
    // which is exactly one chunk. That way each one is built by a single chunk, all at once.

    /** The elevator's iron frame, around the platform. */
    public static final int FRAME_MIN = 19;
    public static final int FRAME_MAX = 25;
    /** The 5x5 platform, and the hole it rides through in the floor above. */
    public static final int PLATFORM_MIN = 20;
    public static final int PLATFORM_MAX = 24;
    /** Where the console stands on both floors (local x, local z). */
    public static final int CONSOLE_X = 27;
    public static final int CONSOLE_Z = 22;

    public static boolean onPlatform(int lx, int lz) {
        return lx >= PLATFORM_MIN && lx <= PLATFORM_MAX && lz >= PLATFORM_MIN && lz <= PLATFORM_MAX;
    }

    /** Stair tower walls, in local coordinates. The spiral ramp runs inside, around a stone core. */
    public static final int TOWER_MIN = 17;
    public static final int TOWER_MAX = 30;
    public static final int CORE_MIN = 22;
    public static final int CORE_MAX = 25;

    // ---------- Buildings ----------

    /** Building footprint inside a lot: [minX, minZ, maxX, maxZ] in local coordinates. */
    public int[] buildingBounds(int floor, int cx, int cz) {
        int inset = 1 + (int) (random01(floor, cx, cz, 2) * 4);
        int minX = ROAD_WIDTH + inset;
        int minZ = ROAD_WIDTH + 1 + (int) (random01(floor, cx, cz, 3) * 4);
        int maxX = CELL - 1 - (int) (random01(floor, cx, cz, 4) * 4);
        int maxZ = CELL - 1 - (int) (random01(floor, cx, cz, 5) * 4);
        return new int[]{minX, minZ, maxX, maxZ};
    }

    /** How tall a building is, in blocks above its floor. */
    public int buildingHeight(int floor, int cx, int cz) {
        int max = isTopFloor(floor) ? 60 : FLOOR_SPACING - SLAB_THICKNESS - 3;
        int min = 10;
        double r = random01(floor, cx, cz, 6);
        return min + (int) (r * r * (max - min));
    }

    /** A style index for a building's walls. */
    public int buildingStyle(int floor, int cx, int cz) {
        return (int) (random01(floor, cx, cz, 10) * 4);
    }

    // ---------- Random numbers ----------

    /** A random number in [0, 1) that is always the same for the same inputs. */
    public double random01(int a, int b, int c, int salt) {
        return (hash(a, b, c, salt) >>> 11) * 0x1.0p-53;
    }

    public long hash(int a, int b, int c, int salt) {
        long h = seed ^ 0x9E3779B97F4A7C15L;
        h = mix(h + a * 0x632BE59BD9B4E019L);
        h = mix(h + b * 0x85157AF5L);
        h = mix(h + c * 0xC2B2AE3D27D4EB4FL);
        h = mix(h + salt * 0x165667B19E3779F9L);
        return h;
    }

    /** Per-block noise for cracks, rubble and broken windows. */
    public double blockNoise(int x, int y, int z, int salt) {
        return random01(x, y, z, salt + 1000);
    }

    private static long mix(long z) {
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >>> 31);
    }
}
