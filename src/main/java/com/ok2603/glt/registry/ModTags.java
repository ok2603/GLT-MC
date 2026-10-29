package com.ok2603.glt.registry;

import com.ok2603.glt.GLTMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

/** Groups of items defined in data/glt/tags. */
public class ModTags {
    /** What Nuko eats: ammunition and explosives. */
    public static final TagKey<Item> NUKO_FOOD = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(GLTMod.MODID, "nuko_food"));
}
