package com.ok2603.glt.registry;

import com.mojang.serialization.Codec;
import com.ok2603.glt.GLTMod;
import com.ok2603.glt.cold.BodyHeat;
import com.ok2603.glt.role.Role;
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

    /** Whether this player has already been given Chito's journal. */
    public static final Supplier<AttachmentType<Boolean>> GOT_JOURNAL = ATTACHMENT_TYPES.register("got_journal",
            () -> AttachmentType.builder(() -> false).serialize(Codec.BOOL).copyOnDeath().build());

    /** Whether this player has already been given Yuuri's rifle. */
    public static final Supplier<AttachmentType<Boolean>> GOT_RIFLE = ATTACHMENT_TYPES.register("got_rifle",
            () -> AttachmentType.builder(() -> false).serialize(Codec.BOOL).copyOnDeath().build());

    /** Whether the player is Chito or Yuuri. Kept when you die. */
    public static final Supplier<AttachmentType<Role>> ROLE = ATTACHMENT_TYPES.register("role",
            () -> AttachmentType.builder(() -> Role.NONE).serialize(Role.CODEC).copyOnDeath().build());
}
