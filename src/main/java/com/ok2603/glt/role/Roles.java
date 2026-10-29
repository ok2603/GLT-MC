package com.ok2603.glt.role;

import com.ok2603.glt.GLTMod;
import com.ok2603.glt.network.RoleSyncPayload;
import com.ok2603.glt.registry.ModAttachments;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Chito and Yuuri's perks and cons. All the numbers live here.
 *
 * Chito: the better driver (faster, saves fuel). Weaker, and gets cold faster.
 * Yuuri: stronger and handles the cold better. Clumsy driver, and gets hungry faster.
 */
public class Roles {
    // --- Chito ---
    public static final float CHITO_COLD_MULTIPLIER = 1.15f;   // gets cold 15% faster
    public static final double CHITO_MINING = -0.25;           // breaks blocks 25% slower
    public static final double CHITO_ATTACK = -0.30;           // hits 30% softer
    /** Chito skips burning fuel 1 tick in this many, so fuel lasts 25% longer. */
    public static final int CHITO_FUEL_SAVING = 4;
    /** Extra Kettenkrad top speed for Chito, in blocks per tick (0.05 = 1 block per second). */
    public static final float CHITO_EXTRA_SPEED = 0.05f;

    // --- Yuuri ---
    public static final float YUURI_COLD_MULTIPLIER = 0.75f;   // gets cold 25% slower
    public static final double YUURI_MINING = 0.30;            // breaks blocks 30% faster
    public static final double YUURI_ATTACK = 0.30;            // hits 30% harder
    public static final float YUURI_EXTRA_HUNGER = 0.0025f;    // extra exhaustion per tick
    public static final float YUURI_ACCELERATION = 0.4f;       // speeds up the Kettenkrad at 40% of the normal rate
    public static final float YUURI_STEERING = 0.6f;           // turns the Kettenkrad at 60% of the normal rate

    private static final ResourceLocation MINING_ID = ResourceLocation.fromNamespaceAndPath(GLTMod.MODID, "role_mining");
    private static final ResourceLocation ATTACK_ID = ResourceLocation.fromNamespaceAndPath(GLTMod.MODID, "role_attack");

    public static Role get(Player player) {
        return player.getData(ModAttachments.ROLE);
    }

    /** Changes a player's role, tells everyone nearby, and applies the perks. */
    public static void set(ServerPlayer player, Role role) {
        player.setData(ModAttachments.ROLE, role);
        sync(player);
        applyAttributes(player);
        player.refreshDisplayName();
        player.refreshTabListName();
    }

    /** True if any other player in the world is already playing this role. */
    public static boolean isTaken(ServerPlayer player, Role role) {
        return player.server.getPlayerList().getPlayers().stream()
                .anyMatch(other -> other != player && get(other) == role);
    }

    /**
     * Picks a role for a player who doesn't have one yet: the host (whoever opened the world)
     * becomes Chito, the friend who joins becomes Yuuri. On a dedicated server the first
     * player to join is Chito. A third player gets no role and can pick one with H.
     */
    public static Role pickAutomaticRole(ServerPlayer player) {
        boolean host = player.server.isSingleplayerOwner(player.getGameProfile());
        boolean chitoFree = !isTaken(player, Role.CHITO);
        boolean yuuriFree = !isTaken(player, Role.YUURI);
        if (chitoFree && (host || player.server.isDedicatedServer())) {
            return Role.CHITO;
        }
        if (yuuriFree && !host) {
            return Role.YUURI;
        }
        if (chitoFree) {
            return Role.CHITO;
        }
        return yuuriFree ? Role.YUURI : Role.NONE;
    }

    /** Sends this player's role to themselves and everyone who can see them. */
    public static void sync(ServerPlayer player) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new RoleSyncPayload(player.getId(), get(player)));
    }

    /** Extra top speed this driver gets on the Kettenkrad. */
    public static float extraDrivingSpeed(Player driver) {
        return get(driver) == Role.CHITO ? CHITO_EXTRA_SPEED : 0f;
    }

    /** How fast this driver speeds the Kettenkrad up (1.0 = normal). */
    public static float accelerationMultiplier(Player driver) {
        return get(driver) == Role.YUURI ? YUURI_ACCELERATION : 1f;
    }

    /** How fast this driver can steer the Kettenkrad (1.0 = normal). */
    public static float steeringMultiplier(Player driver) {
        return get(driver) == Role.YUURI ? YUURI_STEERING : 1f;
    }

    public static float coldMultiplier(Player player) {
        return switch (get(player)) {
            case CHITO -> CHITO_COLD_MULTIPLIER;
            case YUURI -> YUURI_COLD_MULTIPLIER;
            case NONE -> 1.0f;
        };
    }

    /** Sets the mining speed and attack strength for the player's role. */
    public static void applyAttributes(ServerPlayer player) {
        Role role = get(player);
        setModifier(player, Attributes.BLOCK_BREAK_SPEED, MINING_ID,
                role == Role.CHITO ? CHITO_MINING : role == Role.YUURI ? YUURI_MINING : 0);
        setModifier(player, Attributes.ATTACK_DAMAGE, ATTACK_ID,
                role == Role.CHITO ? CHITO_ATTACK : role == Role.YUURI ? YUURI_ATTACK : 0);
    }

    private static void setModifier(Player player, Holder<Attribute> attribute, ResourceLocation id, double amount) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) {
            return;
        }
        if (amount == 0) {
            instance.removeModifier(id);
        } else {
            instance.addOrUpdateTransientModifier(
                    new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
    }
}
