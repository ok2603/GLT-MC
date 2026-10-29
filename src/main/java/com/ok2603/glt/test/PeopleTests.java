package com.ok2603.glt.test;

import com.ok2603.glt.GLTMod;
import com.ok2603.glt.entity.IshiiEntity;
import com.ok2603.glt.entity.KanazawaEntity;
import com.ok2603.glt.entity.RobotEntity;
import com.ok2603.glt.registry.ModEntities;
import com.ok2603.glt.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Automatic checks for Kanazawa, Ishii and the robots. */
@GameTestHolder(GLTMod.MODID)
@PrefixGameTestTemplate(false)
public class PeopleTests {
    /** Food in, a map and his camera out. */
    @GameTest(template = "empty")
    public static void kanazawaTradesMapForFood(GameTestHelper helper) {
        KanazawaEntity kanazawa = helper.spawn(ModEntities.KANAZAWA.get(), new BlockPos(4, 2, 4));
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BREAD, 2));
        kanazawa.interact(player, InteractionHand.MAIN_HAND);
        helper.assertTrue(player.getInventory().countItem(Items.FILLED_MAP) > 0, "No map");
        helper.assertTrue(player.getInventory().countItem(ModItems.CAMERA.get()) > 0, "No camera");
        helper.assertTrue(player.getMainHandItem().getCount() == 1, "He didn't take the food");
        kanazawa.discard();
        helper.succeed();
    }

    /** Ishii takes parts until she has enough, then takes off with a fuel canister. */
    @GameTest(template = "empty")
    public static void ishiiBuildsAndFlies(GameTestHelper helper) {
        IshiiEntity ishii = helper.spawn(ModEntities.ISHII.get(), new BlockPos(4, 2, 4));
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.SCRAP_PARTS.get(), 20));
        ishii.interact(player, InteractionHand.MAIN_HAND);
        helper.assertTrue(ishii.parts() == IshiiEntity.PARTS_NEEDED, "Wrong number of parts taken");
        helper.assertTrue(player.getMainHandItem().getCount() == 20 - IshiiEntity.PARTS_NEEDED, "Took too many parts");
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.FUEL_CANISTER.get()));
        ishii.interact(player, InteractionHand.MAIN_HAND);
        helper.assertTrue(ishii.isFlyingPlane(), "She didn't take off");
        ishii.discard();
        helper.succeed();
    }

    /** A fish farm keeper puts up with a few fish taken, then gets upset. */
    @GameTest(template = "empty")
    public static void keeperGetsUpset(GameTestHelper helper) {
        RobotEntity robot = helper.spawn(ModEntities.ROBOT.get(), new BlockPos(4, 2, 4));
        robot.setHome(helper.absolutePos(new BlockPos(4, 1, 4)), true);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        for (int i = 0; i < RobotEntity.FISH_ALLOWED; i++) {
            robot.onFishTaken(player);
        }
        helper.assertFalse(robot.isUpset(), "Upset too early");
        robot.onFishTaken(player);
        helper.assertTrue(robot.isUpset(), "Should be upset after too many fish");
        robot.discard();
        helper.succeed();
    }
}
