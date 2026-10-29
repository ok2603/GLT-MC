package com.ok2603.glt.client;

import com.ok2603.glt.GLTMod;
import com.ok2603.glt.entity.NukoEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class NukoRenderer extends MobRenderer<NukoEntity, NukoModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(GLTMod.MODID, "textures/entity/nuko.png");

    public NukoRenderer(EntityRendererProvider.Context context) {
        super(context, new NukoModel(context.bakeLayer(NukoModel.LAYER)), 0.3f);
    }

    @Override
    public ResourceLocation getTextureLocation(NukoEntity entity) {
        return TEXTURE;
    }
}
