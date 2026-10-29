package com.ok2603.glt.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.ok2603.glt.config.GLTConfig;
import com.ok2603.glt.registry.ModLootTables;
import com.ok2603.glt.role.Role;
import com.ok2603.glt.role.Roles;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

/**
 * Runs every time a chest's loot is rolled.
 *
 * - Vanilla chests (villages, dungeons, shipwrecks...) sometimes also hold rations, fuel and notes.
 * - Opening any chest as Chito sometimes gives an extra note, map, book or machine part.
 * - Opening any chest as Yuuri sometimes gives extra food.
 */
public class ScavengeLootModifier extends LootModifier {
    public static final MapCodec<ScavengeLootModifier> CODEC = RecordCodecBuilder.mapCodec(
            instance -> codecStart(instance).apply(instance, ScavengeLootModifier::new));

    public ScavengeLootModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext context) {
        ResourceLocation table = context.getQueriedLootTableId();
        if (!table.getPath().startsWith("chests/")) {
            return loot;
        }

        if (table.getNamespace().equals("minecraft") && GLTConfig.get(GLTConfig.VANILLA_CHEST_LOOT)) {
            roll(context, ModLootTables.VANILLA_EXTRA, loot);
        }

        if (context.getParamOrNull(LootContextParams.THIS_ENTITY) instanceof Player player
                && context.getRandom().nextDouble() < GLTConfig.get(GLTConfig.ROLE_LOOT_CHANCE)) {
            Role role = Roles.get(player);
            if (role == Role.CHITO) {
                roll(context, ModLootTables.CHITO_FINDS, loot);
            } else if (role == Role.YUURI) {
                roll(context, ModLootTables.YUURI_FINDS, loot);
            }
        }
        return loot;
    }

    /** Rolls another loot table and adds its items to this chest. */
    private static void roll(LootContext context, ResourceKey<LootTable> key, ObjectArrayList<ItemStack> loot) {
        context.getResolver().get(Registries.LOOT_TABLE, key).ifPresent(table ->
                table.value().getRandomItemsRaw(context, LootTable.createStackSplitter(context.getLevel(), loot::add)));
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
