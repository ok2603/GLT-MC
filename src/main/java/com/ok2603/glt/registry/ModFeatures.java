package com.ok2603.glt.registry;

import com.ok2603.glt.GLTMod;
import com.ok2603.glt.worldgen.HotSpringFeature;
import com.ok2603.glt.worldgen.ShrineFeature;
import com.ok2603.glt.worldgen.SupplyCacheFeature;
import com.ok2603.glt.worldgen.WreckedTankFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Things placed in the world while it generates. Where and how often they appear is set in
 * data/glt/worldgen/placed_feature and data/glt/neoforge/biome_modifier.
 */
public class ModFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, GLTMod.MODID);

    public static final DeferredHolder<Feature<?>, WreckedTankFeature> WRECKED_TANK =
            FEATURES.register("wrecked_tank", () -> new WreckedTankFeature(NoneFeatureConfiguration.CODEC));

    public static final DeferredHolder<Feature<?>, SupplyCacheFeature> SUPPLY_CACHE =
            FEATURES.register("supply_cache", () -> new SupplyCacheFeature(NoneFeatureConfiguration.CODEC));

    public static final DeferredHolder<Feature<?>, HotSpringFeature> HOT_SPRING =
            FEATURES.register("hot_spring", () -> new HotSpringFeature(NoneFeatureConfiguration.CODEC));

    public static final DeferredHolder<Feature<?>, ShrineFeature> SHRINE =
            FEATURES.register("shrine", () -> new ShrineFeature(NoneFeatureConfiguration.CODEC));
}
