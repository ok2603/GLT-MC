package com.ok2603.glt.registry;

import com.ok2603.glt.GLTMod;
import com.ok2603.glt.item.BeerItem;
import com.ok2603.glt.item.ChitosJournalItem;
import com.ok2603.glt.item.KettenkradItem;
import com.ok2603.glt.item.OldNoteItem;
import com.ok2603.glt.item.WarmFoodItem;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Items;
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

    /** Warm soup. Good on a cold night: also restores body heat. */
    public static final DeferredItem<WarmFoodItem> SOUP = ITEMS.registerItem("soup",
            properties -> new WarmFoodItem(properties, 30f),
            new Item.Properties().stacksTo(16).food(new FoodProperties.Builder()
                    .nutrition(5).saturationModifier(0.9f).build()));

    /** Beer from the old world. Rare. Warms you a little and makes the world sway. */
    public static final DeferredItem<BeerItem> BEER = ITEMS.registerItem("beer",
            BeerItem::new,
            new Item.Properties().stacksTo(16).food(new FoodProperties.Builder()
                    .nutrition(1).saturationModifier(0.1f).alwaysEdible()
                    .effect(new MobEffectInstance(MobEffects.CONFUSION, 20 * 12), 1.0f)
                    .usingConvertsTo(Items.GLASS_BOTTLE).build()));

    // --- Supplies ---

    /** Fuel for the Kettenkrad. */
    public static final DeferredItem<Item> FUEL_CANISTER = ITEMS.registerSimpleItem("fuel_canister",
            new Item.Properties().stacksTo(16));

    /** Chito's journal: fills itself in with the moments of the trip. Chito can write in it too. */
    public static final DeferredItem<ChitosJournalItem> CHITOS_JOURNAL = ITEMS.registerItem("chitos_journal",
            ChitosJournalItem::new, new Item.Properties().stacksTo(1));

    /** Bits of old machines. Used to repair the Kettenkrad and to build things. */
    public static final DeferredItem<Item> SCRAP_PARTS = ITEMS.registerSimpleItem("scrap_parts",
            new Item.Properties());

    /** A note from the old world. Only Chito can read it. */
    public static final DeferredItem<OldNoteItem> OLD_NOTE = ITEMS.registerItem("old_note",
            OldNoteItem::new, new Item.Properties().stacksTo(1));

    // --- Blocks found in ruins ---

    public static final DeferredItem<BlockItem> SUPPLY_CRATE = ITEMS.register("supply_crate",
            () -> new BlockItem(ModBlocks.SUPPLY_CRATE.get(), new Item.Properties()));
    public static final DeferredItem<BlockItem> TANK_HATCH = ITEMS.register("tank_hatch",
            () -> new BlockItem(ModBlocks.TANK_HATCH.get(), new Item.Properties()));
    public static final DeferredItem<BlockItem> RUSTED_PLATE = ITEMS.register("rusted_plate",
            () -> new BlockItem(ModBlocks.RUSTED_PLATE.get(), new Item.Properties()));

    /** Lay it out next to the Kettenkrad (or anywhere) and sleep on it. */
    public static final DeferredItem<BlockItem> BEDROLL = ITEMS.register("bedroll",
            () -> new BlockItem(ModBlocks.BEDROLL.get(), new Item.Properties().stacksTo(1)));

    // --- Vehicles ---

    /** The Kettenkrad. Right-click the ground to place it. */
    public static final DeferredItem<KettenkradItem> KETTENKRAD = ITEMS.registerItem("kettenkrad",
            KettenkradItem::new, new Item.Properties().stacksTo(1));

    // --- Clothes: they keep you warm ---

    /** Chito's coat. The best protection against the cold. */
    public static final DeferredItem<ArmorItem> CHITOS_COAT = ITEMS.registerItem("chitos_coat",
            properties -> new ArmorItem(ModArmorMaterials.COAT, ArmorItem.Type.CHESTPLATE, properties),
            new Item.Properties().durability(ArmorItem.Type.CHESTPLATE.getDurability(15)));

    /** Yuuri's army helmet. Keeps your head warm and protects it too. */
    public static final DeferredItem<ArmorItem> YUURIS_HELMET = ITEMS.registerItem("yuuris_helmet",
            properties -> new ArmorItem(ModArmorMaterials.ARMY_HELMET, ArmorItem.Type.HELMET, properties),
            new Item.Properties().durability(ArmorItem.Type.HELMET.getDurability(15)));
}
