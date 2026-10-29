package com.ok2603.glt.registry;

import com.mojang.serialization.MapCodec;
import com.ok2603.glt.GLTMod;
import com.ok2603.glt.loot.ScavengeLootModifier;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/** Changes to loot that already exists (like vanilla chests). */
public class ModLootModifiers {
    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> LOOT_MODIFIERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, GLTMod.MODID);

    public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<ScavengeLootModifier>> SCAVENGE =
            LOOT_MODIFIERS.register("scavenge", () -> ScavengeLootModifier.CODEC);
}
