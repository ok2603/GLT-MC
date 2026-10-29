package com.ok2603.glt.client;

import com.ok2603.glt.GLTMod;
import com.ok2603.glt.entity.RobotEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class RobotRenderer extends MobRenderer<RobotEntity, RobotModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(GLTMod.MODID, "textures/entity/robot.png");

    public RobotRenderer(EntityRendererProvider.Context context) {
        super(context, new RobotModel(context.bakeLayer(RobotModel.LAYER)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(RobotEntity entity) {
        return TEXTURE;
    }
}
