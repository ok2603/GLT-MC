package com.ok2603.glt.client;

import com.ok2603.glt.GLTMod;
import com.ok2603.glt.entity.SurvivorEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

/** Draws Kanazawa and Ishii as people, using the player model with their own skin. */
public class SurvivorRenderer<T extends SurvivorEntity> extends HumanoidMobRenderer<T, HumanoidModel<T>> {
    private final ResourceLocation texture;

    public SurvivorRenderer(EntityRendererProvider.Context context, String name) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5f);
        this.texture = ResourceLocation.fromNamespaceAndPath(GLTMod.MODID, "textures/entity/" + name + ".png");
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return texture;
    }
}
