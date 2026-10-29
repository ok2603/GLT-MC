package com.ok2603.glt.registry;

import com.ok2603.glt.GLTMod;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.List;

/** Materials for the girls' clothes. They protect from the cold more than from hits. */
public class ModArmorMaterials {
    public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS =
            DeferredRegister.create(Registries.ARMOR_MATERIAL, GLTMod.MODID);

    /** Chito's warm coat. Same defense as leather. */
    public static final Holder<ArmorMaterial> COAT = ARMOR_MATERIALS.register("coat", () -> new ArmorMaterial(
            Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                map.put(ArmorItem.Type.HELMET, 1);
                map.put(ArmorItem.Type.CHESTPLATE, 3);
                map.put(ArmorItem.Type.LEGGINGS, 2);
                map.put(ArmorItem.Type.BOOTS, 1);
                map.put(ArmorItem.Type.BODY, 3);
            }),
            15,
            SoundEvents.ARMOR_EQUIP_LEATHER,
            () -> Ingredient.of(Items.WHITE_WOOL),
            List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(GLTMod.MODID, "coat"))),
            0f, 0f));

    /** Yuuri's army helmet. Same defense as iron. */
    public static final Holder<ArmorMaterial> ARMY_HELMET = ARMOR_MATERIALS.register("army_helmet", () -> new ArmorMaterial(
            Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                map.put(ArmorItem.Type.HELMET, 2);
                map.put(ArmorItem.Type.CHESTPLATE, 6);
                map.put(ArmorItem.Type.LEGGINGS, 5);
                map.put(ArmorItem.Type.BOOTS, 2);
                map.put(ArmorItem.Type.BODY, 5);
            }),
            9,
            SoundEvents.ARMOR_EQUIP_IRON,
            () -> Ingredient.of(Items.IRON_INGOT),
            List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(GLTMod.MODID, "army_helmet"))),
            0f, 0f));
}
