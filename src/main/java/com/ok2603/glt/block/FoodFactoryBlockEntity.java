package com.ok2603.glt.block;

import com.ok2603.glt.journal.Journal;
import com.ok2603.glt.journal.JournalMoment;
import com.ok2603.glt.menu.FoodFactoryMenu;
import com.ok2603.glt.registry.ModBlockEntities;
import com.ok2603.glt.registry.ModItems;
import com.ok2603.glt.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/** The food factory's insides: what's loaded, how much power is left, and progress. */
public class FoodFactoryBlockEntity extends BaseContainerBlockEntity {
    public static final int SLOT_CROPS = 0;
    public static final int SLOT_SUGAR = 1;
    public static final int SLOT_POWER = 2;
    public static final int SLOT_OUTPUT = 3;
    public static final int SIZE = 4;

    /** Ticks to press one ration (10 seconds). */
    public static final int PRESS_TIME = 200;
    /** How many rations one power item makes. */
    public static final int POWER_PER_CANISTER = 8;
    public static final int POWER_PER_COAL = 1;
    private static final int CROPS_PER_RATION = 2;
    private static final int COCOA_PER_CHOCOLATE = 2;

    private NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
    private int progress;
    private int power;

    /** Lets the screen show progress and power. */
    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return index == 0 ? progress : power;
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) {
                progress = value;
            } else {
                power = value;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    };

    public FoodFactoryBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FOOD_FACTORY.get(), pos, state);
    }

    public static boolean isPowerItem(ItemStack stack) {
        return stack.is(ModItems.FUEL_CANISTER.get()) || stack.is(Items.COAL) || stack.is(Items.CHARCOAL);
    }

    public static boolean isCropItem(ItemStack stack) {
        return stack.is(ModTags.FACTORY_GRAINS) || stack.is(Items.COCOA_BEANS);
    }

    /** What the loaded ingredients make, or empty if they don't make anything. */
    private ItemStack result() {
        ItemStack crops = items.get(SLOT_CROPS);
        if (items.get(SLOT_SUGAR).isEmpty()) {
            return ItemStack.EMPTY;
        }
        if (crops.is(Items.COCOA_BEANS) && crops.getCount() >= COCOA_PER_CHOCOLATE) {
            return new ItemStack(ModItems.CHOCOLATE_RATION.get());
        }
        if (crops.is(ModTags.FACTORY_GRAINS) && crops.getCount() >= CROPS_PER_RATION) {
            return new ItemStack(ModItems.RATION_BAR.get());
        }
        return ItemStack.EMPTY;
    }

    private boolean outputHasRoom(ItemStack result) {
        ItemStack output = items.get(SLOT_OUTPUT);
        return output.isEmpty() || (ItemStack.isSameItemSameComponents(output, result) && output.getCount() < output.getMaxStackSize());
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FoodFactoryBlockEntity factory) {
        ItemStack result = factory.result();
        boolean canWork = !result.isEmpty() && factory.outputHasRoom(result);

        // Load power when needed.
        if (canWork && factory.power <= 0) {
            ItemStack fuel = factory.items.get(SLOT_POWER);
            if (isPowerItem(fuel)) {
                factory.power += fuel.is(ModItems.FUEL_CANISTER.get()) ? POWER_PER_CANISTER : POWER_PER_COAL;
                fuel.shrink(1);
                factory.setChanged();
            }
        }

        boolean working = canWork && factory.power > 0;
        if (working) {
            factory.progress++;
            if (factory.progress >= PRESS_TIME) {
                factory.progress = 0;
                factory.power--;
                ItemStack crops = factory.items.get(SLOT_CROPS);
                crops.shrink(crops.is(Items.COCOA_BEANS) ? COCOA_PER_CHOCOLATE : CROPS_PER_RATION);
                factory.items.get(SLOT_SUGAR).shrink(1);
                ItemStack output = factory.items.get(SLOT_OUTPUT);
                if (output.isEmpty()) {
                    factory.items.set(SLOT_OUTPUT, result);
                } else {
                    output.grow(1);
                }
                if (level instanceof ServerLevel serverLevel) {
                    for (ServerPlayer player : serverLevel.getEntitiesOfClass(ServerPlayer.class, new AABB(pos).inflate(8))) {
                        Journal.unlock(player, JournalMoment.FOOD_FACTORY);
                    }
                }
            }
            factory.setChanged();
        } else if (factory.progress > 0) {
            factory.progress = 0;
            factory.setChanged();
        }

        if (state.getValue(FoodFactoryBlock.LIT) != working) {
            level.setBlock(pos, state.setValue(FoodFactoryBlock.LIT, working), 3);
        }
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    public int getContainerSize() {
        return SIZE;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return switch (slot) {
            case SLOT_CROPS -> isCropItem(stack);
            case SLOT_SUGAR -> stack.is(Items.SUGAR);
            case SLOT_POWER -> isPowerItem(stack);
            default -> false;
        };
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.glt.food_factory");
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new FoodFactoryMenu(containerId, inventory, this, data);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items, registries);
        progress = tag.getInt("Progress");
        power = tag.getInt("Power");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
        tag.putInt("Progress", progress);
        tag.putInt("Power", power);
    }
}
