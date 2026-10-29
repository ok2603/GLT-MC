package com.ok2603.glt.registry;

import com.ok2603.glt.GLTMod;
import com.ok2603.glt.block.BedrollBlock;
import com.ok2603.glt.block.CairnBlock;
import com.ok2603.glt.block.FoodFactoryBlock;
import com.ok2603.glt.block.LampPostBlock;
import com.ok2603.glt.block.LeakingPipeBlock;
import com.ok2603.glt.block.RainCatcherBlock;
import com.ok2603.glt.block.StatueBlock;
import com.ok2603.glt.block.StreetLampBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import com.ok2603.glt.block.LootContainerBlock;
import com.ok2603.glt.block.TankHatchBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
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

    /** A blanket to sleep on, next to the Kettenkrad or anywhere else. */
    public static final DeferredBlock<BedrollBlock> BEDROLL = BLOCKS.register("bedroll",
            () -> new BedrollBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GREEN)
                    .sound(SoundType.WOOL).strength(0.2f).noOcclusion().ignitedByLava()
                    .pushReaction(PushReaction.DESTROY).instrument(NoteBlockInstrument.GUITAR)));

    // --- The city's pipes ---

    /** A big iron pipe. Cold. Good for building. */
    public static final DeferredBlock<RotatedPillarBlock> PIPE = BLOCKS.register("pipe",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
                    .mapColor(MapColor.METAL).strength(3.0f, 6.0f)));

    /** A pipe with hot water still running through it. Warms you like a campfire. */
    public static final DeferredBlock<RotatedPillarBlock> HOT_PIPE = BLOCKS.register("hot_pipe",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
                    .mapColor(MapColor.COLOR_ORANGE).strength(3.0f, 6.0f)));

    /** A cracked hot pipe. It steams, and the water around it becomes a warm bath. */
    public static final DeferredBlock<LeakingPipeBlock> LEAKING_HOT_PIPE = BLOCKS.register("leaking_hot_pipe",
            () -> new LeakingPipeBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
                    .mapColor(MapColor.COLOR_ORANGE).strength(3.0f, 6.0f)));

    // --- Small moments ---

    /** A can, bucket or helmet set out in the rain. Plays a note when rain falls on it. */
    public static final DeferredBlock<RainCatcherBlock> RAIN_CATCHER = BLOCKS.register("rain_catcher",
            () -> new RainCatcherBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL)
                    .sound(SoundType.LANTERN).strength(0.3f).noOcclusion().pushReaction(PushReaction.DESTROY)));

    /** The old machine that presses crops and sugar into ration bars. */
    public static final DeferredBlock<FoodFactoryBlock> FOOD_FACTORY = BLOCKS.register("food_factory",
            () -> new FoodFactoryBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).strength(4.0f, 6.0f)
                    .lightLevel(state -> state.getValue(FoodFactoryBlock.LIT) ? 7 : 0)));

    /** A small stone statue from a temple. Leave food as an offering for luck. */
    public static final DeferredBlock<StatueBlock> STATUE = BLOCKS.register("statue",
            () -> new StatueBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).noOcclusion()));

    /** A little pile of stones. Add one with cobblestone. */
    public static final DeferredBlock<CairnBlock> CAIRN = BLOCKS.register("cairn",
            () -> new CairnBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.COBBLESTONE).noOcclusion().strength(1.0f)));

    /** A street lamp that switches on at night. */
    public static final DeferredBlock<StreetLampBlock> STREET_LAMP = BLOCKS.register("street_lamp",
            () -> new StreetLampBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).noOcclusion().strength(2.0f)
                    .sound(SoundType.LANTERN).lightLevel(state -> state.getValue(StreetLampBlock.LIT) ? 15 : 0)));

    /** The post a street lamp stands on. */
    public static final DeferredBlock<LampPostBlock> LAMP_POST = BLOCKS.register("lamp_post",
            () -> new LampPostBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BARS).strength(2.0f)));
}
