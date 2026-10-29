package com.ok2603.glt.survival;

import com.ok2603.glt.GLTMod;
import com.ok2603.glt.config.GLTConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.block.CropGrowEvent;

/**
 * In the dead city almost nothing grows, so food has to be scavenged.
 *
 * With the default "COLD" setting:
 * - Outdoors in cold or snowy biomes, crops don't grow at all.
 * - Outdoors elsewhere, they grow slowly (see outdoorCropGrowth in the config).
 * - Under a roof (a greenhouse), they grow normally.
 * "NONE" stops all crop growth and "VANILLA" turns this off.
 */
@EventBusSubscriber(modid = GLTMod.MODID)
public class FarmingRules {
    @SubscribeEvent
    public static void onCropGrow(CropGrowEvent.Pre event) {
        GLTConfig.FarmingMode mode = GLTConfig.get(GLTConfig.FARMING);
        if (mode == GLTConfig.FarmingMode.VANILLA) {
            return;
        }
        if (mode == GLTConfig.FarmingMode.NONE) {
            event.setResult(CropGrowEvent.Pre.Result.DO_NOT_GROW);
            return;
        }

        LevelAccessor level = event.getLevel();
        BlockPos pos = event.getPos();
        if (!level.canSeeSky(pos.above())) {
            return; // greenhouse
        }
        Biome biome = level.getBiome(pos).value();
        boolean cold = biome.getBaseTemperature() < 0.3f || biome.coldEnoughToSnow(pos);
        if (cold || level.getRandom().nextDouble() >= GLTConfig.get(GLTConfig.OUTDOOR_CROP_GROWTH)) {
            event.setResult(CropGrowEvent.Pre.Result.DO_NOT_GROW);
        }
    }
}
