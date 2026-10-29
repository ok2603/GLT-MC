package com.ok2603.glt.test;

import com.ok2603.glt.GLTMod;
import com.ok2603.glt.survival.Together;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Automatic checks for the co-op moments. */
@GameTestHolder(GLTMod.MODID)
@PrefixGameTestTemplate(false)
public class TogetherTests {
    /** Food given to the other player fills them more than eating it yourself would. */
    @GameTest(template = "empty")
    public static void sharedFoodFillsMore(GameTestHelper helper) {
        Player feeder = helper.makeMockPlayer(GameType.SURVIVAL);
        Player hungry = helper.makeMockPlayer(GameType.SURVIVAL);
        hungry.getFoodData().setFoodLevel(4);
        ItemStack bread = new ItemStack(Items.BREAD, 3);
        int nutrition = bread.get(DataComponents.FOOD).nutrition();
        Together.feed(feeder, hungry, bread, bread.get(DataComponents.FOOD));
        helper.assertTrue(bread.getCount() == 2, "The bread wasn't used up");
        helper.assertTrue(hungry.getFoodData().getFoodLevel() > 4 + nutrition, "Shared food didn't give extra");
        helper.succeed();
    }
}
