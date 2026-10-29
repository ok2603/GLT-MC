package com.ok2603.glt.registry;

import com.mojang.serialization.Codec;
import com.ok2603.glt.GLTMod;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Extra data that can be stored on an item stack. */
public class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, GLTMod.MODID);

    /** Which text an Old Note has written on it. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> NOTE =
            DATA_COMPONENTS.register("note", () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT).build());
}
