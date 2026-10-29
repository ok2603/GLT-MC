package com.ok2603.glt.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Small helper for building things during world generation using "local" coordinates:
 * forward (along the facing direction), right, and up, starting from an origin block.
 * This lets one build plan face any of the four directions.
 */
public class Builder {
    private final WorldGenLevel level;
    private final BlockPos origin;
    private final Direction forward;
    private final Direction right;

    public Builder(WorldGenLevel level, BlockPos origin, Direction forward) {
        this.level = level;
        this.origin = origin;
        this.forward = forward;
        this.right = forward.getClockWise();
    }

    public Direction forward() {
        return forward;
    }

    public BlockPos pos(int f, int r, int up) {
        return origin.relative(forward, f).relative(right, r).above(up);
    }

    public void set(int f, int r, int up, BlockState state) {
        level.setBlock(pos(f, r, up), state, 2);
    }

    public BlockState get(int f, int r, int up) {
        return level.getBlockState(pos(f, r, up));
    }

    /** Fills air and plants under a spot down to the ground, so buildings don't float on slopes. */
    public void foundation(int f, int r, BlockState state, int maxDepth) {
        for (int down = 1; down <= maxDepth; down++) {
            BlockPos pos = pos(f, r, -down);
            BlockState existing = level.getBlockState(pos);
            if (!existing.canBeReplaced() && existing.getFluidState().isEmpty()) {
                return;
            }
            level.setBlock(pos, state, 2);
        }
    }

    /** True if the ground under the whole area (from f0..f1, r0..r1) is solid and dry. */
    public boolean solidGround(int f0, int f1, int r0, int r1) {
        for (int f = f0; f <= f1; f++) {
            for (int r = r0; r <= r1; r++) {
                if (!get(f, r, 0).getFluidState().isEmpty()) {
                    return false;
                }
                BlockState below = get(f, r, -1);
                if (!below.getFluidState().isEmpty()) {
                    return false;
                }
            }
        }
        BlockState center = get((f0 + f1) / 2, (r0 + r1) / 2, -1);
        return center.isSolid();
    }
}
