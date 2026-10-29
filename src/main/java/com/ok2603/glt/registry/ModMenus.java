package com.ok2603.glt.registry;

import com.ok2603.glt.GLTMod;
import com.ok2603.glt.menu.FoodFactoryMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Screens with slots (like the food factory). */
public class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, GLTMod.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<FoodFactoryMenu>> FOOD_FACTORY =
            MENUS.register("food_factory", () -> new MenuType<>(FoodFactoryMenu::new, FeatureFlags.DEFAULT_FLAGS));
}
