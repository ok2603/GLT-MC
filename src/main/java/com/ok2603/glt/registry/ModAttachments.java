package com.ok2603.glt.registry;

import com.mojang.serialization.Codec;
import com.ok2603.glt.GLTMod;
import com.ok2603.glt.cold.BodyHeat;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

/** Extra data the mod stores on players (saved with the world). */
public class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, GLTMod.MODID);

    /** How warm the player is, from 0 (frozen) to 100 (warm). Resets to full when you respawn. */
    public static final Supplier<AttachmentType<Float>> BODY_HEAT = ATTACHMENT_TYPES.register("body_heat",
            () -> AttachmentType.builder(() -> BodyHeat.MAX).serialize(Codec.FLOAT).build());
}
