package com.ok2603.glt.registry;

import com.ok2603.glt.GLTMod;
import com.ok2603.glt.entity.KettenkradEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Every entity (mob or vehicle) the mod adds. */
public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, GLTMod.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<KettenkradEntity>> KETTENKRAD =
            ENTITY_TYPES.register("kettenkrad", () -> EntityType.Builder
                    .<KettenkradEntity>of(KettenkradEntity::new, MobCategory.MISC)
                    .sized(1.5f, 1.0f) // width and height of the hitbox, in blocks
                    .clientTrackingRange(10)
                    .build("kettenkrad"));
}
