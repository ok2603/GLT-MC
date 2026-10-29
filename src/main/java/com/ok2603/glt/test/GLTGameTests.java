package com.ok2603.glt.test;

import com.ok2603.glt.GLTMod;
import com.ok2603.glt.block.LootContainerBlockEntity;
import com.ok2603.glt.registry.ModBlocks;
import com.ok2603.glt.registry.ModFeatures;
import com.ok2603.glt.registry.ModLootTables;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.List;
import java.util.Optional;

/**
 * Automatic in-game checks. GitHub runs them on a real Minecraft server after every push
 * ("./gradlew runGameTestServer"), so broken data files or world generation are caught
 * before anyone has to test by hand.
 */
@GameTestHolder(GLTMod.MODID)
@PrefixGameTestTemplate(false)
public class GLTGameTests {
    private static final String EMPTY = "empty";

    /** All the mod's loot tables load. */
    @GameTest(template = EMPTY)
    public static void lootTablesLoad(GameTestHelper helper) {
        for (ResourceKey<LootTable> key : List.of(ModLootTables.SUPPLY_CACHE, ModLootTables.WRECKED_TANK,
                ModLootTables.VANILLA_EXTRA, ModLootTables.CHITO_FINDS, ModLootTables.YUURI_FINDS)) {
            LootTable table = helper.getLevel().getServer().reloadableRegistries().getLootTable(key);
            helper.assertTrue(table != LootTable.EMPTY, "Missing loot table " + key.location());
        }
        helper.succeed();
    }

    /** World generation files load: placed features and biome modifiers. */
    @GameTest(template = EMPTY)
    public static void worldgenLoads(GameTestHelper helper) {
        var access = helper.getLevel().registryAccess();
        for (String name : List.of("wrecked_tank", "supply_cache")) {
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(GLTMod.MODID, name);
            helper.assertTrue(access.registryOrThrow(Registries.PLACED_FEATURE).containsKey(id), "Missing placed feature " + id);
            helper.assertTrue(access.registryOrThrow(NeoForgeRegistries.Keys.BIOME_MODIFIERS).containsKey(id), "Missing biome modifier " + id);
        }
        helper.succeed();
    }

    /** A wrecked tank can be built on flat ground and its hatch has loot. */
    @GameTest(template = EMPTY)
    public static void wreckedTankBuilds(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos origin = flatGround(helper);
        boolean placed = ModFeatures.WRECKED_TANK.get().place(new FeaturePlaceContext<>(Optional.empty(), level,
                level.getChunkSource().getGenerator(), RandomSource.create(1), origin, NoneFeatureConfiguration.INSTANCE));
        helper.assertTrue(placed, "Wrecked tank did not place");
        BlockPos hatch = findBlock(helper, origin, ModBlocks.TANK_HATCH.get());
        helper.assertTrue(hatch != null, "No tank hatch");
        helper.assertTrue(level.getBlockEntity(hatch) instanceof LootContainerBlockEntity, "Hatch has no container");
        helper.succeed();
    }

    /** A supply cache can be built and has at least one crate. */
    @GameTest(template = EMPTY)
    public static void supplyCacheBuilds(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos origin = flatGround(helper);
        boolean placed = ModFeatures.SUPPLY_CACHE.get().place(new FeaturePlaceContext<>(Optional.empty(), level,
                level.getChunkSource().getGenerator(), RandomSource.create(2), origin, NoneFeatureConfiguration.INSTANCE));
        helper.assertTrue(placed, "Supply cache did not place");
        BlockPos crate = findBlock(helper, origin, ModBlocks.SUPPLY_CRATE.get());
        helper.assertTrue(crate != null, "No supply crate");
        LootContainerBlockEntity container = (LootContainerBlockEntity) level.getBlockEntity(crate);
        helper.assertTrue(container != null && container.getLootTable() != null, "Crate has no loot table");
        container.unpackLootTable(null);
        helper.assertFalse(container.isEmpty(), "Crate rolled no loot");
        helper.succeed();
    }

    /** Lays a 9x9 stone floor in the test area and returns the block just above its middle. */
    static BlockPos flatGround(GameTestHelper helper) {
        for (int x = 0; x < 9; x++) {
            for (int z = 0; z < 9; z++) {
                helper.setBlock(x, 0, z, Blocks.STONE);
            }
        }
        return helper.absolutePos(new BlockPos(4, 1, 4));
    }

    static BlockPos findBlock(GameTestHelper helper, BlockPos center, net.minecraft.world.level.block.Block block) {
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-5, -1, -5), center.offset(5, 5, 5))) {
            if (helper.getLevel().getBlockState(pos).is(block)) {
                return pos.immutable();
            }
        }
        return null;
    }

}
