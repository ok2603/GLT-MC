package com.ok2603.glt.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A hot pipe with a crack in it. It steams, it warms you like a fire, and water next to it
 * (within a few blocks) turns into a warm bath. See HotBath.
 */
public class LeakingPipeBlock extends RotatedPillarBlock {
    public LeakingPipeBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(3) == 0) {
            level.addParticle(ParticleTypes.CLOUD, pos.getX() + random.nextDouble(), pos.getY() + 1.0,
                    pos.getZ() + random.nextDouble(), 0, 0.04, 0);
        }
        if (random.nextInt(4) == 0) {
            level.addParticle(ParticleTypes.DRIPPING_WATER, pos.getX() + random.nextDouble(), pos.getY(),
                    pos.getZ() + random.nextDouble(), 0, 0, 0);
        }
        if (random.nextInt(120) == 0) {
            level.playLocalSound(pos, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.15f, 1.6f, false);
        }
    }
}
