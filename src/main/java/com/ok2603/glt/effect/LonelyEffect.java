package com.ok2603.glt.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * "Lonely": the girls have been apart for too long. It does nothing on its own; while you have it,
 * fires, baths and the engine warm you only half as well (see BodyHeat).
 */
public class LonelyEffect extends MobEffect {
    public LonelyEffect() {
        super(MobEffectCategory.HARMFUL, 0x5A6A8A);
    }
}
