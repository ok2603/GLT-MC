package com.ok2603.glt.cold;

import com.ok2603.glt.GLTMod;
import com.ok2603.glt.entity.KettenkradEntity;
import com.ok2603.glt.network.BodyHeatPayload;
import com.ok2603.glt.registry.ModAttachments;
import com.ok2603.glt.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Body heat: the main danger of the mod.
 *
 * Every second the game works out how cold it is around the player and
 * lowers (or raises) their body heat. When it gets low, frost creeps over
 * the screen and the player slows down. At zero they start to freeze to death.
 *
 * All the numbers are "body heat per second" and can be tuned below.
 */
@EventBusSubscriber(modid = GLTMod.MODID)
public class BodyHeat {
    public static final float MAX = 100f;
    /** Below this, frost appears on the screen and the player slows down. */
    public static final float CHILLY = 30f;

    // --- How fast you get cold (per second) ---
    private static final float BASE_LOSS = 0.15f;          // the world is always a bit cold
    private static final float COLD_BIOME_LOSS = 0.35f;    // snowy biomes
    private static final float COOL_BIOME_LOSS = 0.15f;    // normal biomes like plains and forests
    private static final float NIGHT_LOSS = 0.15f;
    private static final float SNOWSTORM_LOSS = 0.25f;     // snowing or raining on you
    private static final float WATER_LOSS = 0.6f;          // swimming in cold water

    // --- What protects you ---
    private static final float SHELTER_FACTOR = 0.5f;      // under a roof: lose heat half as fast
    private static final float COAT_PROTECTION = 0.40f;    // Chito's coat
    private static final float HELMET_PROTECTION = 0.15f;  // Yuuri's helmet
    private static final float LEATHER_PROTECTION = 0.10f; // each piece of leather armor
    private static final float HUDDLE_FACTOR = 0.7f;       // another player right next to you

    // --- What warms you up ---
    private static final float FIRE_GAIN = 2.0f;           // campfire, fire, lava, lit furnace
    private static final float ENGINE_GAIN = 1.0f;         // next to a running Kettenkrad
    private static final float HOT_BIOME_GAIN = 0.5f;      // deserts, savannas, the Nether

    private static final int HEAT_SOURCE_RANGE = 3;

    // ---------- Reading and changing body heat ----------

    public static float get(Player player) {
        return player.getData(ModAttachments.BODY_HEAT);
    }

    public static void set(ServerPlayer player, float heat) {
        player.setData(ModAttachments.BODY_HEAT, Mth.clamp(heat, 0, MAX));
        PacketDistributor.sendToPlayer(player, new BodyHeatPayload(get(player)));
    }

    /** Used by warm food like soup. */
    public static void add(ServerPlayer player, float amount) {
        set(player, get(player) + amount);
    }

    // ---------- Every tick ----------

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (player.isCreative() || player.isSpectator()) {
            player.setData(ModAttachments.BODY_HEAT, MAX);
            return;
        }

        // Update body heat once per second.
        if (player.tickCount % 20 == 0) {
            float change = warmthPerSecond(player) - coldPerSecond(player);
            set(player, get(player) + change);

            // Out of heat: freezing damage every 2 seconds.
            if (get(player) <= 0 && player.tickCount % 40 == 0) {
                player.hurt(player.damageSources().freeze(), 1.0f);
            }
        }

        // Frost on the screen and slowness, using vanilla's powder-snow freezing effect.
        // Kept just under "fully frozen" so vanilla doesn't add its own damage on top of ours.
        float heat = get(player);
        int required = player.getTicksRequiredToFreeze();
        if (heat < CHILLY) {
            int frozen = (int) ((1 - heat / CHILLY) * (required - 1));
            player.setTicksFrozen(Math.max(player.getTicksFrozen(), frozen));
        }
    }

    private static float coldPerSecond(ServerPlayer player) {
        Level level = player.level();
        BlockPos pos = player.blockPosition();
        Biome biome = level.getBiome(pos).value();
        boolean underSky = level.canSeeSky(BlockPos.containing(player.getEyePosition()));

        float loss = BASE_LOSS;
        float biomeTemp = biome.getBaseTemperature();
        if (biomeTemp < 0.3f || biome.coldEnoughToSnow(pos)) {
            loss += COLD_BIOME_LOSS;
        } else if (biomeTemp < 1.0f) {
            loss += COOL_BIOME_LOSS;
        }
        if (level.dimension() == Level.OVERWORLD && !level.isDay()) {
            loss += NIGHT_LOSS;
        }
        if (level.isRaining() && underSky && biome.getPrecipitationAt(pos) != Biome.Precipitation.NONE) {
            loss += SNOWSTORM_LOSS;
        }
        if (player.isInWater()) {
            loss += WATER_LOSS;
        }

        if (!underSky) {
            loss *= SHELTER_FACTOR;
        }
        loss *= 1 - clothingProtection(player);
        if (hasPlayerNearby(player)) {
            loss *= HUDDLE_FACTOR;
        }
        return loss;
    }

    private static float clothingProtection(Player player) {
        float protection = 0;
        for (ItemStack armor : player.getArmorSlots()) {
            if (armor.is(ModItems.CHITOS_COAT.get())) {
                protection += COAT_PROTECTION;
            } else if (armor.is(ModItems.YUURIS_HELMET.get())) {
                protection += HELMET_PROTECTION;
            } else if (armor.is(ItemTags.FREEZE_IMMUNE_WEARABLES)) {
                protection += LEATHER_PROTECTION;
            }
        }
        return Math.min(protection, 0.8f);
    }

    /** Huddling together like Chito and Yuuri. */
    private static boolean hasPlayerNearby(ServerPlayer player) {
        for (Player other : player.level().players()) {
            if (other != player && !other.isSpectator() && other.distanceToSqr(player) < 3 * 3) {
                return true;
            }
        }
        return false;
    }

    private static float warmthPerSecond(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        float gain = 0;

        if (level.getBiome(player.blockPosition()).value().getBaseTemperature() >= 1.0f) {
            gain += HOT_BIOME_GAIN;
        }

        BlockPos center = player.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-HEAT_SOURCE_RANGE, -1, -HEAT_SOURCE_RANGE),
                center.offset(HEAT_SOURCE_RANGE, 2, HEAT_SOURCE_RANGE))) {
            if (isHeatSource(level.getBlockState(pos))) {
                gain += FIRE_GAIN;
                break;
            }
        }

        boolean nearEngine = !level.getEntitiesOfClass(KettenkradEntity.class,
                player.getBoundingBox().inflate(HEAT_SOURCE_RANGE), KettenkradEntity::isEngineRunning).isEmpty();
        if (nearEngine) {
            gain += ENGINE_GAIN;
        }
        return gain;
    }

    private static boolean isHeatSource(BlockState state) {
        if (state.is(BlockTags.CAMPFIRES)) {
            return state.getValue(CampfireBlock.LIT);
        }
        if (state.getBlock() instanceof AbstractFurnaceBlock) {
            return state.getValue(AbstractFurnaceBlock.LIT);
        }
        return state.is(BlockTags.FIRE) || state.is(Blocks.LAVA) || state.is(Blocks.MAGMA_BLOCK);
    }

    // ---------- Joining and respawning ----------

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            set(player, get(player));
        }
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            set(player, MAX);
        }
    }
}
