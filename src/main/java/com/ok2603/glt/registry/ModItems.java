package com.ok2603.glt.registry;

import com.ok2603.glt.GLTMod;
import com.ok2603.glt.item.BeerItem;
import com.ok2603.glt.item.CameraItem;
import com.ok2603.glt.item.CannedFoodItem;
import com.ok2603.glt.item.EmptyCanItem;
import com.ok2603.glt.item.PhotoItem;
import com.ok2603.glt.item.ChitosJournalItem;
import com.ok2603.glt.item.KettenkradItem;
import com.ok2603.glt.item.NukoItem;
import com.ok2603.glt.item.RifleItem;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
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

    /** Canned fish, like the fish from the factory. Leaves an empty can. */
    public static final DeferredItem<CannedFoodItem> CANNED_FISH = ITEMS.registerItem("canned_fish",
            properties -> new CannedFoodItem(properties, () -> ModItems.EMPTY_CAN.get()),
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

    // --- Yuuri's rifle and Nuko ---

    /** Yuuri's rifle. Only Yuuri can really aim it. */
    public static final DeferredItem<RifleItem> YUURIS_RIFLE = ITEMS.registerItem("yuuris_rifle",
            RifleItem::new, new Item.Properties().durability(250));

    /** Ammunition for the rifle. Very rare. Nuko loves them. */
    public static final DeferredItem<Item> RIFLE_CARTRIDGE = ITEMS.registerSimpleItem("rifle_cartridge",
            new Item.Properties().stacksTo(16));

    /** Nuko, held in your arms. */
    public static final DeferredItem<NukoItem> NUKO = ITEMS.registerItem("nuko",
            NukoItem::new, new Item.Properties().stacksTo(1));

    public static final DeferredItem<DeferredSpawnEggItem> NUKO_SPAWN_EGG = ITEMS.register("nuko_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.NUKO, 0xF4F1EA, 0x2B2B2B, new Item.Properties()));

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

    // --- Small moments ---

    /** An empty can. Set it out in the rain to hear it ping. */
    public static final DeferredItem<EmptyCanItem> EMPTY_CAN = ITEMS.register("empty_can",
            () -> new EmptyCanItem(ModBlocks.RAIN_CATCHER.get(), new Item.Properties()));

    /** Kanazawa's camera. 24 shots of film; reload it with paper in an anvil. */
    public static final DeferredItem<CameraItem> CAMERA = ITEMS.registerItem("camera",
            CameraItem::new, new Item.Properties().durability(25));

    /** A photo from the trip. */
    public static final DeferredItem<PhotoItem> PHOTO = ITEMS.registerItem("photo",
            PhotoItem::new, new Item.Properties().stacksTo(1));

    public static final DeferredItem<BlockItem> PIPE = blockItem("pipe", ModBlocks.PIPE);
    public static final DeferredItem<BlockItem> HOT_PIPE = blockItem("hot_pipe", ModBlocks.HOT_PIPE);
    public static final DeferredItem<BlockItem> LEAKING_HOT_PIPE = blockItem("leaking_hot_pipe", ModBlocks.LEAKING_HOT_PIPE);
    public static final DeferredItem<BlockItem> FOOD_FACTORY = blockItem("food_factory", ModBlocks.FOOD_FACTORY);
    public static final DeferredItem<BlockItem> STATUE = blockItem("statue", ModBlocks.STATUE);
    public static final DeferredItem<BlockItem> CAIRN = blockItem("cairn", ModBlocks.CAIRN);
    public static final DeferredItem<BlockItem> STREET_LAMP = blockItem("street_lamp", ModBlocks.STREET_LAMP);
    public static final DeferredItem<BlockItem> LAMP_POST = blockItem("lamp_post", ModBlocks.LAMP_POST);

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

    private static DeferredItem<BlockItem> blockItem(String name, java.util.function.Supplier<? extends net.minecraft.world.level.block.Block> block) {
        return ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }
}
