package com.ok2603.glt.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Settings for the mod. They live in the world's "serverconfig/glt-server.toml" file,
 * so each world can have its own settings. The host's settings apply to everyone.
 */
public class GLTConfig {
    public enum FarmingMode {
        /** Crops grow like in normal Minecraft. */
        VANILLA,
        /** Crops don't grow outdoors in cold places and grow slowly elsewhere. Under a roof they grow normally. */
        COLD,
        /** Crops never grow. All food must be scavenged. */
        NONE
    }

    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue NO_MONSTERS;
    public static final ModConfigSpec.EnumValue<FarmingMode> FARMING;
    public static final ModConfigSpec.DoubleValue OUTDOOR_CROP_GROWTH;
    public static final ModConfigSpec.BooleanValue AUTO_ROLES;
    public static final ModConfigSpec.BooleanValue CHARACTER_NAMES;
    public static final ModConfigSpec.BooleanValue JOURNAL_NEEDS_BOTH;
    public static final ModConfigSpec.BooleanValue VANILLA_CHEST_LOOT;
    public static final ModConfigSpec.DoubleValue ROLE_LOOT_CHANCE;
    public static final ModConfigSpec.BooleanValue ELEVATOR_NEEDS_FUEL;
    public static final ModConfigSpec.DoubleValue CITY_HEIGHT_COLD;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("survival");
        NO_MONSTERS = builder
                .comment("Stop hostile mobs from appearing on their own in the Overworld. The cold is the danger instead.")
                .define("noMonsters", true);
        FARMING = builder
                .comment("How farming works. VANILLA = normal, COLD = no crops outdoors in cold biomes and slow elsewhere"
                        + " (under a roof they grow normally), NONE = crops never grow.")
                .defineEnum("farming", FarmingMode.COLD);
        OUTDOOR_CROP_GROWTH = builder
                .comment("In COLD mode: how often outdoor crops in mild biomes still grow (1.0 = normal speed).")
                .defineInRange("outdoorCropGrowth", 0.35, 0.0, 1.0);
        builder.pop();

        builder.push("coop");
        AUTO_ROLES = builder
                .comment("The host becomes Chito and the next player to join becomes Yuuri automatically.")
                .define("autoRoles", true);
        CHARACTER_NAMES = builder
                .comment("Show players as \"Chito\" and \"Yuuri\" in chat, the player list and name tags.")
                .define("characterNames", true);
        JOURNAL_NEEDS_BOTH = builder
                .comment("Some journal moments (the stars, camping, beer...) only count when both girls are close together."
                        + " Single player is never blocked by this.")
                .define("journalNeedsBoth", true);
        builder.pop();

        builder.push("loot");
        VANILLA_CHEST_LOOT = builder
                .comment("Add rations, fuel and other supplies to vanilla chests (villages, dungeons, shipwrecks...).")
                .define("vanillaChestLoot", true);
        ROLE_LOOT_CHANCE = builder
                .comment("Chance that opening a chest gives a role bonus: Chito finds notes, maps and parts; Yuuri finds food.")
                .defineInRange("roleLootChance", 0.35, 0.0, 1.0);
        builder.pop();

        builder.push("lastCity");
        ELEVATOR_NEEDS_FUEL = builder
                .comment("Riding a city elevator up burns one Fuel Canister. Going down is always free.")
                .define("elevatorNeedsFuel", true);
        CITY_HEIGHT_COLD = builder
                .comment("Extra cold per second on the top floor of the Last City (lower floors get a share of it).")
                .defineInRange("cityHeightCold", 0.3, 0.0, 5.0);
        builder.pop();

        SPEC = builder.build();
    }

    /** Reads a setting, or uses the default if the world's settings aren't loaded yet. */
    public static <T> T get(ModConfigSpec.ConfigValue<T> value) {
        return SPEC.isLoaded() ? value.get() : value.getDefault();
    }
}
