package com.ok2603.glt.registry;

import com.ok2603.glt.GLTMod;
import com.ok2603.glt.block.LootContainerBlock;
import com.ok2603.glt.block.TankHatchBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Every block the mod adds. Each block also needs: a block item in ModItems, a blockstate,
 * a block model, an item model, a texture, a name in en_us.json and a loot table (what it drops).
 */
public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(GLTMod.MODID);

    /** An old army supply crate found in ruins. Holds loot the first time you open it. */
    public static final DeferredBlock<LootContainerBlock> SUPPLY_CRATE = BLOCKS.register("supply_crate",
            () -> new LootContainerBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BARREL)));

    /** The hatch on top of a wrecked tank. Sometimes there's chocolate hidden inside, but only Yuuri finds it. */
    public static final DeferredBlock<TankHatchBlock> TANK_HATCH = BLOCKS.register("tank_hatch",
            () -> new TankHatchBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
                    .mapColor(MapColor.TERRACOTTA_BROWN).strength(4.0f, 6.0f)));

    /** Rusty armor plate from wrecked machines. Breaks into scrap parts. */
    public static final DeferredBlock<Block> RUSTED_PLATE = BLOCKS.register("rusted_plate",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
                    .mapColor(MapColor.TERRACOTTA_BROWN).strength(3.0f, 6.0f)));
}
