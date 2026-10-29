package com.ok2603.glt.registry;

import com.ok2603.glt.GLTMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * The mod's own sounds. Files live in assets/glt/sounds and are listed in assets/glt/sounds.json.
 * All of them are original (made by a script), so you can replace any .ogg with your own.
 */
public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, GLTMod.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> RIFLE_SHOT = sound("rifle_shot");
    public static final DeferredHolder<SoundEvent, SoundEvent> RIFLE_BOLT = sound("rifle_bolt");
    public static final DeferredHolder<SoundEvent, SoundEvent> RIFLE_EMPTY = sound("rifle_empty");
    public static final DeferredHolder<SoundEvent, SoundEvent> NUKO_AMBIENT = sound("nuko_ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> NUKO_EAT = sound("nuko_eat");
    public static final DeferredHolder<SoundEvent, SoundEvent> NUKO_RADIO = sound("nuko_radio");

    public static final DeferredHolder<SoundEvent, SoundEvent> CAMERA_SHUTTER = sound("camera_shutter");

    private static DeferredHolder<SoundEvent, SoundEvent> sound(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(GLTMod.MODID, name)));
    }
}
