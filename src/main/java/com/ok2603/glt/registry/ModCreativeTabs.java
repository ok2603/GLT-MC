package com.ok2603.glt.registry;

import com.ok2603.glt.GLTMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** The "Girls' Last Tour" tab in the creative inventory. */
public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, GLTMod.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> GLT_TAB = CREATIVE_TABS.register("glt_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.glt"))
                    .icon(() -> ModItems.RATION_BAR.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        // Every registered item from this mod goes in the tab.
                        ModItems.ITEMS.getEntries().forEach(item -> output.accept(item.get()));
                    })
                    .build());
}
