package com.ok2603.glt.item;

import com.ok2603.glt.journal.Journal;
import com.ok2603.glt.journal.JournalMoment;
import com.ok2603.glt.registry.ModItems;
import com.ok2603.glt.registry.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Kanazawa's old camera. Right-click to take a photo: you get a Photo item that remembers
 * where it was taken, on which day of the trip, and who was in the picture.
 * Each shot uses up film (durability). Repair it in an anvil with Paper to load more.
 */
public class CameraItem extends Item {
    private static final double FRAME_RANGE = 16.0;

    public CameraItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack camera = player.getItemInHand(hand);
        if (camera.getDamageValue() >= camera.getMaxDamage() - 1) {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.translatable("message.glt.camera.no_film"), true);
            }
            return InteractionResultHolder.fail(camera);
        }
        if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
            ItemStack photo = takePhoto(serverLevel, serverPlayer);
            if (!player.getInventory().add(photo)) {
                player.drop(photo, false);
            }
            camera.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
            level.playSound(null, player.blockPosition(), ModSounds.CAMERA_SHUTTER.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
            Vec3 look = player.getLookAngle();
            serverLevel.sendParticles(ParticleTypes.FLASH, player.getX() + look.x, player.getEyeY() + look.y, player.getZ() + look.z, 1, 0, 0, 0, 0);
            if (photo.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getList("People", 8).size() > 0) {
                Journal.unlock(serverPlayer, JournalMoment.PHOTO);
            }
        }
        player.getCooldowns().addCooldown(this, 20);
        return InteractionResultHolder.sidedSuccess(camera, level.isClientSide);
    }

    private static ItemStack takePhoto(ServerLevel level, ServerPlayer photographer) {
        ItemStack photo = new ItemStack(ModItems.PHOTO.get());
        CompoundTag tag = new CompoundTag();
        String biome = level.getBiome(photographer.blockPosition()).unwrapKey()
                .map(key -> "biome." + key.location().getNamespace() + "." + key.location().getPath()).orElse("biome.minecraft.plains");
        tag.putString("Place", biome);
        tag.putInt("Day", Journal.today(level.getServer()));
        tag.putInt("Height", photographer.getBlockY());

        // Everyone roughly in front of the camera is in the picture.
        ListTag people = new ListTag();
        Vec3 look = photographer.getLookAngle();
        for (Player other : level.players()) {
            if (other == photographer || other.isSpectator()) {
                continue;
            }
            Vec3 toOther = other.getEyePosition().subtract(photographer.getEyePosition());
            if (toOther.length() < FRAME_RANGE && toOther.normalize().dot(look) > 0.8 && photographer.hasLineOfSight(other)) {
                people.add(StringTag.valueOf(other.getDisplayName().getString()));
            }
        }
        tag.put("People", people);
        photo.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return photo;
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
        return repairCandidate.is(Items.PAPER);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.glt.camera.tooltip", stack.getMaxDamage() - 1 - stack.getDamageValue())
                .withStyle(ChatFormatting.GRAY));
    }
}
