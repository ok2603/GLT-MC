package com.ok2603.glt.item;

import com.ok2603.glt.entity.KettenkradEntity;
import com.ok2603.glt.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

/** Right-click on the ground to place a Kettenkrad. */
public class KettenkradItem extends Item {
    public KettenkradItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
        KettenkradEntity kettenkrad = ModEntities.KETTENKRAD.get().create(level);
        if (kettenkrad == null) {
            return InteractionResult.FAIL;
        }
        // Face the same way as the player.
        kettenkrad.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, context.getRotation(), 0);
        if (!level.noCollision(kettenkrad, kettenkrad.getBoundingBox())) {
            return InteractionResult.FAIL;
        }

        level.addFreshEntity(kettenkrad);
        Player player = context.getPlayer();
        level.gameEvent(player, GameEvent.ENTITY_PLACE, pos);
        if (player == null || !player.getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }
        return InteractionResult.CONSUME;
    }
}
