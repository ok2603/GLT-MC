package com.ok2603.glt.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.ok2603.glt.GLTMod;
import com.ok2603.glt.entity.IshiiEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/** Ishii, plus her little homemade plane while she is flying. */
public class IshiiRenderer extends SurvivorRenderer<IshiiEntity> {
    public static final ModelLayerLocation PLANE_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(GLTMod.MODID, "ishii_plane"), "main");
    private static final ResourceLocation PLANE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(GLTMod.MODID, "textures/entity/ishii_plane.png");

    private final ModelPart plane;

    public IshiiRenderer(EntityRendererProvider.Context context) {
        super(context, "ishii");
        this.plane = context.bakeLayer(PLANE_LAYER);
    }

    /** A small fuselage she sits in, long thin wings, a tail and a propeller. Sizes are in pixels. */
    public static LayerDefinition createPlaneLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("fuselage", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-3.0f, 0.0f, -10.0f, 6, 5, 22), PartPose.ZERO);
        root.addOrReplaceChild("wings", CubeListBuilder.create().texOffs(0, 27)
                .addBox(-24.0f, 4.0f, -4.0f, 48, 1, 7), PartPose.ZERO);
        root.addOrReplaceChild("tail_wing", CubeListBuilder.create().texOffs(0, 35)
                .addBox(-8.0f, 4.0f, 9.0f, 16, 1, 4), PartPose.ZERO);
        root.addOrReplaceChild("tail_fin", CubeListBuilder.create().texOffs(40, 35)
                .addBox(-0.5f, -3.0f, 9.0f, 1, 4, 4), PartPose.ZERO);
        root.addOrReplaceChild("propeller", CubeListBuilder.create().texOffs(56, 0)
                .addBox(-5.0f, 2.0f, -11.0f, 10, 1, 1), PartPose.ZERO);
        return LayerDefinition.create(mesh, 128, 64);
    }

    @Override
    public void render(IshiiEntity entity, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        if (!entity.isFlyingPlane()) {
            return;
        }
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0f - entityYaw));
        // The model is drawn upside down like all entity models, so flip it.
        poseStack.scale(-1.0f, -1.0f, 1.0f);
        poseStack.translate(0.0f, -0.9f, 0.0f);
        plane.getChild("propeller").zRot = (entity.tickCount + partialTicks) * 1.3f;
        plane.render(poseStack, buffer.getBuffer(RenderType.entityCutoutNoCull(PLANE_TEXTURE)), packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }
}
