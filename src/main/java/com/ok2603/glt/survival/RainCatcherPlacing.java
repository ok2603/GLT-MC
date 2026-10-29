package com.ok2603.glt.survival;

import com.ok2603.glt.GLTMod;
import com.ok2603.glt.block.RainCatcherBlock;
import com.ok2603.glt.registry.ModBlocks;
import com.ok2603.glt.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** Sneak + right-click the top of a block with an empty bucket or Yuuri's helmet to set it out for the rain. */
@EventBusSubscriber(modid = GLTMod.MODID)
public class RainCatcherPlacing {
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        ItemStack stack = event.getItemStack();
        RainCatcherBlock.Kind kind;
        if (stack.is(Items.BUCKET)) {
            kind = RainCatcherBlock.Kind.BUCKET;
        } else if (stack.is(ModItems.YUURIS_HELMET.get())) {
            kind = RainCatcherBlock.Kind.HELMET;
        } else {
            return;
        }
        if (!event.getEntity().isShiftKeyDown() || event.getFace() != Direction.UP) {
            return;
        }
        Level level = event.getLevel();
        BlockPos pos = event.getPos().above();
        BlockState state = ModBlocks.RAIN_CATCHER.get().defaultBlockState().setValue(RainCatcherBlock.KIND, kind);
        if (!level.getBlockState(pos).canBeReplaced() || !state.canSurvive(level, pos)) {
            return;
        }
        if (!level.isClientSide) {
            level.setBlock(pos, state, 3);
            level.playSound(null, pos, SoundEvents.ARMOR_EQUIP_IRON.value(), SoundSource.BLOCKS, 0.8f, 1.2f);
            if (!event.getEntity().getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide));
    }
}
