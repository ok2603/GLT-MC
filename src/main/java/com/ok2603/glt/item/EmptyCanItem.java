package com.ok2603.glt.item;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;

/** An empty can. Put it down outside and the rain will play a note on it. */
public class EmptyCanItem extends BlockItem {
    public EmptyCanItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public String getDescriptionId() {
        return "item.glt.empty_can";
    }
}
