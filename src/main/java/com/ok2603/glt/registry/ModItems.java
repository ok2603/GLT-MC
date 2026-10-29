package com.ok2603.glt.registry;

import com.ok2603.glt.GLTMod;
import com.ok2603.glt.item.KettenkradItem;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Every item the mod adds. To add a new item, copy one of the lines below,
 * then add a texture, a model file and a name in en_us.json.
 */
public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(GLTMod.MODID);

    // --- Food: the rations Chito and Yuuri live on ---

    /** The classic ration bar. Filling, a bit dry. */
    public static final DeferredItem<Item> RATION_BAR = ITEMS.registerSimpleItem("ration_bar",
            new Item.Properties().food(new FoodProperties.Builder()
                    .nutrition(6).saturationModifier(0.6f).build()));

    /** The rare chocolate-flavored ration bar. Eats fast and tastes great. */
    public static final DeferredItem<Item> CHOCOLATE_RATION = ITEMS.registerSimpleItem("chocolate_ration",
            new Item.Properties().food(new FoodProperties.Builder()
                    .nutrition(7).saturationModifier(0.8f).fast().build()));

    /** Canned fish, like the fish from the factory. */
    public static final DeferredItem<Item> CANNED_FISH = ITEMS.registerSimpleItem("canned_fish",
            new Item.Properties().food(new FoodProperties.Builder()
                    .nutrition(8).saturationModifier(0.7f).build()));

    /** Warm soup. Good on a cold night. */
    public static final DeferredItem<Item> SOUP = ITEMS.registerSimpleItem("soup",
            new Item.Properties().stacksTo(16).food(new FoodProperties.Builder()
                    .nutrition(5).saturationModifier(0.9f).build()));

    // --- Supplies ---

    /** Fuel for the Kettenkrad. */
    public static final DeferredItem<Item> FUEL_CANISTER = ITEMS.registerSimpleItem("fuel_canister",
            new Item.Properties().stacksTo(16));

    /** Chito's journal. For now a plain item; later it will open a writable book. */
    public static final DeferredItem<Item> CHITOS_JOURNAL = ITEMS.registerSimpleItem("chitos_journal",
            new Item.Properties().stacksTo(1));

    // --- Vehicles ---

    /** The Kettenkrad. Right-click the ground to place it. */
    public static final DeferredItem<KettenkradItem> KETTENKRAD = ITEMS.registerItem("kettenkrad",
            KettenkradItem::new, new Item.Properties().stacksTo(1));
}
