package com.ok2603.glt;

import com.ok2603.glt.registry.ModCreativeTabs;
import com.ok2603.glt.registry.ModItems;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

/**
 * Main class of the mod. Minecraft finds it through the @Mod annotation
 * and calls the constructor when the game starts.
 */
@Mod(GLTMod.MODID)
public class GLTMod {
    public static final String MODID = "glt";
    public static final Logger LOGGER = LogUtils.getLogger();

    public GLTMod(IEventBus modEventBus, ModContainer modContainer) {
        // Tell the game about everything this mod adds.
        ModItems.ITEMS.register(modEventBus);
        ModCreativeTabs.CREATIVE_TABS.register(modEventBus);

        LOGGER.info("Girls' Last Tour mod loaded. Let's keep going, Chii.");
    }
}
