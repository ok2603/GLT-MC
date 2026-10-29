package com.ok2603.glt.registry;

import com.ok2603.glt.GLTMod;
import com.ok2603.glt.entity.IshiiEntity;
import com.ok2603.glt.entity.KanazawaEntity;
import com.ok2603.glt.entity.KettenkradEntity;
import com.ok2603.glt.entity.RobotEntity;
import com.ok2603.glt.entity.NukoEntity;
import com.ok2603.glt.entity.SeatEntity;
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
                    .sized(1.75f, 1.1f) // width and height of the hitbox, in blocks
                    .clientTrackingRange(10)
                    .build("kettenkrad"));

    /** Nuko: small, white, harmless, eats ammo. */
    public static final DeferredHolder<EntityType<?>, EntityType<NukoEntity>> NUKO =
            ENTITY_TYPES.register("nuko", () -> EntityType.Builder
                    .<NukoEntity>of(NukoEntity::new, MobCategory.CREATURE)
                    .sized(0.5f, 0.5f)
                    .clientTrackingRange(8)
                    .build("nuko"));

    /** Invisible seat used when a player sits down with Z. */
    public static final DeferredHolder<EntityType<?>, EntityType<SeatEntity>> SEAT =
            ENTITY_TYPES.register("seat", () -> EntityType.Builder
                    .<SeatEntity>of(SeatEntity::new, MobCategory.MISC)
                    .sized(0.1f, 0.1f)
                    .noSave()
                    .fireImmune()
                    .clientTrackingRange(10)
                    .build("seat"));

    /** Kanazawa, the mapmaker. */
    public static final DeferredHolder<EntityType<?>, EntityType<KanazawaEntity>> KANAZAWA =
            ENTITY_TYPES.register("kanazawa", () -> EntityType.Builder
                    .<KanazawaEntity>of(KanazawaEntity::new, MobCategory.MISC)
                    .sized(0.6f, 1.8f)
                    .clientTrackingRange(10)
                    .build("kanazawa"));

    /** Ishii, the pilot. */
    public static final DeferredHolder<EntityType<?>, EntityType<IshiiEntity>> ISHII =
            ENTITY_TYPES.register("ishii", () -> EntityType.Builder
                    .<IshiiEntity>of(IshiiEntity::new, MobCategory.MISC)
                    .sized(0.6f, 1.8f)
                    .clientTrackingRange(16)
                    .build("ishii"));

    /** An old maintenance robot. */
    public static final DeferredHolder<EntityType<?>, EntityType<RobotEntity>> ROBOT =
            ENTITY_TYPES.register("robot", () -> EntityType.Builder
                    .<RobotEntity>of(RobotEntity::new, MobCategory.MISC)
                    .sized(0.9f, 1.3f)
                    .clientTrackingRange(10)
                    .build("robot"));
}
