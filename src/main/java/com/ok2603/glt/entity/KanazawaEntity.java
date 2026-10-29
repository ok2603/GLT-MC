package com.ok2603.glt.entity;

import com.ok2603.glt.city.CityLayout;
import com.ok2603.glt.city.LastCity;
import com.ok2603.glt.journal.Journal;
import com.ok2603.glt.journal.JournalMoment;
import com.ok2603.glt.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

import java.util.List;

/**
 * Kanazawa, the mapmaker. Give him some food and he gives you one of his maps, with the
 * nearest elevator on this floor marked with an X. The first time, he also gives away his camera.
 */
public class KanazawaEntity extends SurvivorEntity {
    public static final int MAX_MAPS = 3;
    private int mapsGiven;
    private boolean gaveCamera;

    public KanazawaEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    @Override
    protected void talk(Player player) {
        ItemStack held = player.getMainHandItem();
        boolean food = held.get(DataComponents.FOOD) != null;
        if (mapsGiven >= MAX_MAPS) {
            say(player, "chat.glt.kanazawa.no_more");
            return;
        }
        if (!food) {
            say(player, "chat.glt.kanazawa.hello");
            return;
        }
        if (!player.getAbilities().instabuild) {
            held.shrink(1);
        }
        mapsGiven++;
        playSound(SoundEvents.BOOK_PAGE_TURN, 1.0f, 1.0f);
        give(player, makeMap((ServerLevel) level()));
        say(player, "chat.glt.kanazawa.trade");
        if (!gaveCamera) {
            gaveCamera = true;
            give(player, new ItemStack(ModItems.CAMERA.get()));
            say(player, "chat.glt.kanazawa.camera");
        }
        if (player instanceof ServerPlayer serverPlayer) {
            Journal.unlock(serverPlayer, JournalMoment.KANAZAWA);
        }
    }

    private static void give(Player player, ItemStack stack) {
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }

    /** A map centered here. In the city it marks the nearest elevator on this floor (or the summit on the top floor). */
    private ItemStack makeMap(ServerLevel level) {
        ItemStack map = MapItem.create(level, getBlockX(), getBlockZ(), (byte) 2, true, true);
        if (!LastCity.isCity(level)) {
            map.set(DataComponents.ITEM_NAME, Component.translatable("item.glt.kanazawa_map.area"));
            return map;
        }
        int floor = LastCity.floor(getY());
        BlockPos target = CityLayout.isTopFloor(floor)
                ? new BlockPos(18, CityLayout.floorY(floor), 18)
                : nearestElevator(new CityLayout(level.getSeed()), floor);
        MapItemSavedData.addTargetDecoration(map, target, "+", MapDecorationTypes.RED_X);
        map.set(DataComponents.ITEM_NAME, Component.translatable("item.glt.kanazawa_map"));
        map.set(DataComponents.LORE, new ItemLore(List.of(Component.translatable("item.glt.kanazawa_map.lore",
                floor + 1, target.getX(), target.getZ()).withStyle(ChatFormatting.GRAY))));
        return map;
    }

    private BlockPos nearestElevator(CityLayout layout, int floor) {
        int rx = Math.floorDiv(CityLayout.cellOf(getBlockX()), CityLayout.REGION);
        int rz = Math.floorDiv(CityLayout.cellOf(getBlockZ()), CityLayout.REGION);
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                int[] cell = layout.elevatorCellInRegion(floor, rx + dx, rz + dz);
                BlockPos pos = new BlockPos(cell[0] * CityLayout.CELL + CityLayout.CONSOLE_X,
                        CityLayout.floorY(floor), cell[1] * CityLayout.CELL + CityLayout.CONSOLE_Z);
                double distance = pos.distToCenterSqr(position());
                if (distance < bestDistance) {
                    bestDistance = distance;
                    best = pos;
                }
            }
        }
        return best;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("MapsGiven", mapsGiven);
        tag.putBoolean("GaveCamera", gaveCamera);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        mapsGiven = tag.getInt("MapsGiven");
        gaveCamera = tag.getBoolean("GaveCamera");
    }
}
