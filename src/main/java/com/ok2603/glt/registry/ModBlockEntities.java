package com.ok2603.glt.registry;

import com.ok2603.glt.GLTMod;
import com.ok2603.glt.block.FoodFactoryBlockEntity;
import com.ok2603.glt.block.LootContainerBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Blocks that store extra data (like the items inside a crate) use a "block entity". */
public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, GLTMod.MODID);

    @SuppressWarnings("DataFlowIssue") // the "null" is the normal way to build these
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LootContainerBlockEntity>> LOOT_CONTAINER =
            BLOCK_ENTITIES.register("loot_container", () -> BlockEntityType.Builder.of(LootContainerBlockEntity::new,
                    ModBlocks.SUPPLY_CRATE.get(), ModBlocks.TANK_HATCH.get()).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FoodFactoryBlockEntity>> FOOD_FACTORY =
            BLOCK_ENTITIES.register("food_factory", () -> BlockEntityType.Builder.of(FoodFactoryBlockEntity::new,
                    ModBlocks.FOOD_FACTORY.get()).build(null));
}
