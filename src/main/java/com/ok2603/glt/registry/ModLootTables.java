package com.ok2603.glt.registry;

import com.ok2603.glt.GLTMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;

/** Names of the mod's loot tables (the files live in data/glt/loot_table). */
public class ModLootTables {
    public static final ResourceKey<LootTable> SUPPLY_CACHE = key("chests/supply_cache");
    public static final ResourceKey<LootTable> WRECKED_TANK = key("chests/wrecked_tank");
    public static final ResourceKey<LootTable> VANILLA_EXTRA = key("gameplay/vanilla_extra");
    public static final ResourceKey<LootTable> CHITO_FINDS = key("gameplay/chito_finds");
    public static final ResourceKey<LootTable> YUURI_FINDS = key("gameplay/yuuri_finds");

    private static ResourceKey<LootTable> key(String path) {
        return ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath(GLTMod.MODID, path));
    }
}
