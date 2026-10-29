package com.ok2603.glt.menu;

import com.ok2603.glt.block.FoodFactoryBlockEntity;
import com.ok2603.glt.registry.ModMenus;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** The food factory's screen layout: crops, sugar, power in; rations out; then the player's inventory. */
public class FoodFactoryMenu extends AbstractContainerMenu {
    private final Container container;
    private final ContainerData data;

    /** Used on the player's computer when the server opens the screen. */
    public FoodFactoryMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(FoodFactoryBlockEntity.SIZE), new SimpleContainerData(2));
    }

    public FoodFactoryMenu(int containerId, Inventory playerInventory, Container container, ContainerData data) {
        super(ModMenus.FOOD_FACTORY.get(), containerId);
        checkContainerSize(container, FoodFactoryBlockEntity.SIZE);
        this.container = container;
        this.data = data;
        container.startOpen(playerInventory.player);

        addSlot(new FilteredSlot(container, FoodFactoryBlockEntity.SLOT_CROPS, 44, 26));
        addSlot(new FilteredSlot(container, FoodFactoryBlockEntity.SLOT_SUGAR, 62, 26));
        addSlot(new FilteredSlot(container, FoodFactoryBlockEntity.SLOT_POWER, 53, 53));
        addSlot(new Slot(container, FoodFactoryBlockEntity.SLOT_OUTPUT, 116, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
        addDataSlots(data);
    }

    /** 0 to 1: how far along the current ration is. */
    public float progress() {
        return data.get(0) / (float) FoodFactoryBlockEntity.PRESS_TIME;
    }

    /** Rations left on the loaded power. */
    public int power() {
        return data.get(1);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        int machineSlots = FoodFactoryBlockEntity.SIZE;
        if (index < machineSlots) {
            if (!moveItemStackTo(stack, machineSlots, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (FoodFactoryBlockEntity.isCropItem(stack)) {
            if (!moveItemStackTo(stack, FoodFactoryBlockEntity.SLOT_CROPS, FoodFactoryBlockEntity.SLOT_CROPS + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (stack.is(net.minecraft.world.item.Items.SUGAR)) {
            if (!moveItemStackTo(stack, FoodFactoryBlockEntity.SLOT_SUGAR, FoodFactoryBlockEntity.SLOT_SUGAR + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (FoodFactoryBlockEntity.isPowerItem(stack)) {
            if (!moveItemStackTo(stack, FoodFactoryBlockEntity.SLOT_POWER, FoodFactoryBlockEntity.SLOT_POWER + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        container.stopOpen(player);
    }

    /** A slot that only takes what the factory accepts there. */
    private static class FilteredSlot extends Slot {
        FilteredSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return container.canPlaceItem(getContainerSlot(), stack);
        }
    }
}
