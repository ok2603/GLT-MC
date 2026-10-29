package com.ok2603.glt.client;

import com.ok2603.glt.GLTMod;
import com.ok2603.glt.role.Role;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Chito and Yuuri's look: the normal player model (classic 4-pixel arms)
 * with their own skin. Later this can be swapped for a Blockbench model.
 */
public class RolePlayerRenderer extends PlayerRenderer {
    public static final ResourceLocation CHITO_SKIN =
            ResourceLocation.fromNamespaceAndPath(GLTMod.MODID, "textures/entity/player/chito.png");
    public static final ResourceLocation YUURI_SKIN =
            ResourceLocation.fromNamespaceAndPath(GLTMod.MODID, "textures/entity/player/yuuri.png");

    private final ResourceLocation skin;

    public RolePlayerRenderer(EntityRendererProvider.Context context, ResourceLocation skin) {
        super(context, false); // false = classic arms, to match the skins
        this.skin = skin;
    }

    public ResourceLocation getSkin() {
        return skin;
    }

    @Override
    public ResourceLocation getTextureLocation(AbstractClientPlayer player) {
        return skin;
    }

    public static ResourceLocation skinFor(Role role) {
        return role == Role.YUURI ? YUURI_SKIN : CHITO_SKIN;
    }
}
