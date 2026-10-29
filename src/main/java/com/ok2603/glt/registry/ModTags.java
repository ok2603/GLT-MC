package com.ok2603.glt.registry;

import com.ok2603.glt.GLTMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/** Groups of items defined in data/glt/tags. */
public class ModTags {
    /** What Nuko eats: ammunition and explosives. */
    public static final TagKey<Item> NUKO_FOOD = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(GLTMod.MODID, "nuko_food"));

    /** Blocks that warm you like a campfire (hot pipes, lit street lamps don't count). */
    public static final TagKey<Block> HEAT_SOURCES = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(GLTMod.MODID, "heat_sources"));

    /** Crops the food factory can turn into ration bars. */
    public static final TagKey<Item> FACTORY_GRAINS = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(GLTMod.MODID, "factory_grains"));
}
