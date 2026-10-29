package com.ok2603.glt.registry;

import com.ok2603.glt.GLTMod;
import com.ok2603.glt.effect.LonelyEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Status effects the mod adds. Each needs an icon in textures/mob_effect and a name in en_us.json. */
public class ModEffects {
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, GLTMod.MODID);

    public static final DeferredHolder<MobEffect, MobEffect> LONELY = EFFECTS.register("lonely", LonelyEffect::new);
}
