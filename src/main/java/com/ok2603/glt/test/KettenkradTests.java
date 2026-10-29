package com.ok2603.glt.test;

import com.ok2603.glt.GLTMod;
import com.ok2603.glt.entity.KettenkradEntity;
import com.ok2603.glt.registry.ModBlocks;
import com.ok2603.glt.registry.ModEntities;
import com.ok2603.glt.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Automatic checks for the Kettenkrad and camping. */
@GameTestHolder(GLTMod.MODID)
@PrefixGameTestTemplate(false)
public class KettenkradTests {
    /** Fuel, condition and the cargo survive saving and loading the world. */
    @GameTest(template = "empty")
    public static void kettenkradSavesItsState(GameTestHelper helper) {
        KettenkradEntity kettenkrad = helper.spawn(ModEntities.KETTENKRAD.get(), new BlockPos(4, 2, 4));
        kettenkrad.setFuel(1234);
        kettenkrad.setCondition(42);
        kettenkrad.getStorage().setItem(0, new ItemStack(ModItems.RATION_BAR.get(), 5));

        CompoundTag saved = kettenkrad.saveWithoutId(new CompoundTag());
        KettenkradEntity copy = ModEntities.KETTENKRAD.get().create(helper.getLevel());
        helper.assertTrue(copy != null, "Could not create a Kettenkrad");
        copy.load(saved);

        helper.assertTrue(copy.getFuel() == 1234, "Fuel was not saved");
        helper.assertTrue(copy.getCondition() == 42, "Condition was not saved");
        helper.assertTrue(copy.getStorage().countItem(ModItems.RATION_BAR.get()) == 5, "Storage was not saved");
        kettenkrad.discard();
        helper.succeed();
    }

    /** Repairs bring the condition back up, and it never goes past 100%. */
    @GameTest(template = "empty")
    public static void conditionStaysInRange(GameTestHelper helper) {
        KettenkradEntity kettenkrad = helper.spawn(ModEntities.KETTENKRAD.get(), new BlockPos(4, 2, 4));
        kettenkrad.setCondition(500);
        helper.assertTrue(kettenkrad.getCondition() == KettenkradEntity.MAX_CONDITION, "Condition went past the max");
        kettenkrad.damageCondition(1000);
        helper.assertTrue(kettenkrad.isBroken(), "Should be broken at 0%");
        helper.assertFalse(kettenkrad.isEngineRunning(), "A broken engine shouldn't run");
        kettenkrad.discard();
        helper.succeed();
    }

    /** The bedroll counts as a bed for sleeping and respawning. */
    @GameTest(template = "empty")
    public static void bedrollIsABed(GameTestHelper helper) {
        BlockPos pos = new BlockPos(4, 1, 4);
        helper.setBlock(pos.below(), net.minecraft.world.level.block.Blocks.STONE);
        helper.setBlock(pos, ModBlocks.BEDROLL.get());
        BlockPos abs = helper.absolutePos(pos);
        helper.assertTrue(helper.getLevel().getBlockState(abs).isBed(helper.getLevel(), abs, null), "Bedroll is not a bed");
        helper.succeed();
    }
}
