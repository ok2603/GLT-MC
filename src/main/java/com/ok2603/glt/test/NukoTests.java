package com.ok2603.glt.test;

import com.ok2603.glt.GLTMod;
import com.ok2603.glt.entity.NukoEntity;
import com.ok2603.glt.registry.ModEntities;
import com.ok2603.glt.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Automatic checks for Nuko. */
@GameTestHolder(GLTMod.MODID)
@PrefixGameTestTemplate(false)
public class NukoTests {
    /** Nuko walks to a cartridge on the ground and eats it. */
    @GameTest(template = "empty", timeoutTicks = 200)
    public static void nukoEatsAmmo(GameTestHelper helper) {
        for (int x = 0; x < 9; x++) {
            for (int z = 0; z < 9; z++) {
                helper.setBlock(x, 0, z, Blocks.STONE);
            }
        }
        NukoEntity nuko = helper.spawn(ModEntities.NUKO.get(), new BlockPos(1, 1, 1));
        ItemEntity ammo = helper.spawnItem(ModItems.RIFLE_CARTRIDGE.get(), 6, 1, 6);
        helper.succeedWhen(() -> {
            helper.assertTrue(ammo.isRemoved(), "Nuko hasn't eaten the cartridge yet");
            nuko.discard();
        });
    }

    /** Nuko ignores normal food. */
    @GameTest(template = "empty")
    public static void nukoOnlyEatsAmmo(GameTestHelper helper) {
        NukoEntity nuko = helper.spawn(ModEntities.NUKO.get(), new BlockPos(1, 1, 1));
        helper.assertTrue(nuko.isFood(new ItemStack(Items.GUNPOWDER)), "Nuko should like gunpowder");
        helper.assertFalse(nuko.isFood(new ItemStack(ModItems.RATION_BAR.get())), "Nuko shouldn't eat rations");
        nuko.discard();
        helper.succeed();
    }
}
