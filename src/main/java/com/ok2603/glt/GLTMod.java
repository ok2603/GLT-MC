package com.ok2603.glt;

import com.ok2603.glt.config.GLTConfig;
import com.ok2603.glt.registry.ModArmorMaterials;
import com.ok2603.glt.registry.ModAttachments;
import com.ok2603.glt.registry.ModBlockEntities;
import com.ok2603.glt.registry.ModBlocks;
import com.ok2603.glt.registry.ModCreativeTabs;
import com.ok2603.glt.registry.ModDataComponents;
import com.ok2603.glt.registry.ModEntities;
import com.ok2603.glt.registry.ModFeatures;
import com.ok2603.glt.registry.ModItems;
import com.ok2603.glt.registry.ModLootModifiers;
import com.ok2603.glt.registry.ModMenus;
import com.ok2603.glt.registry.ModSounds;
import com.ok2603.glt.registry.ModEffects;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
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
        ModArmorMaterials.ARMOR_MATERIALS.register(modEventBus);
        ModAttachments.ATTACHMENT_TYPES.register(modEventBus);
        ModDataComponents.DATA_COMPONENTS.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModLootModifiers.LOOT_MODIFIERS.register(modEventBus);
        ModFeatures.FEATURES.register(modEventBus);
        ModEntities.ENTITY_TYPES.register(modEventBus);
        ModSounds.SOUNDS.register(modEventBus);
        ModEffects.EFFECTS.register(modEventBus);
        ModMenus.MENUS.register(modEventBus);
        ModCreativeTabs.CREATIVE_TABS.register(modEventBus);

        // Settings, saved per world in serverconfig/glt-server.toml.
        modContainer.registerConfig(ModConfig.Type.SERVER, GLTConfig.SPEC);

        LOGGER.info("Girls' Last Tour mod loaded. Let's keep going, Chii.");
    }
}
