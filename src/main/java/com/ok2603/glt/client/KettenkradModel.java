package com.ok2603.glt.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.ok2603.glt.GLTMod;
import com.ok2603.glt.entity.KettenkradEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;

/**
 * The 3D shape of the Kettenkrad, built from boxes.
 * Units are pixels (16 = one block). The front of the vehicle is at negative Z.
 * Later this can be replaced by a nicer model made in Blockbench.
 */
public class KettenkradModel extends EntityModel<KettenkradEntity> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(GLTMod.MODID, "kettenkrad"), "main");

    private final ModelPart root;

    public KettenkradModel(ModelPart root) {
        this.root = root;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartPose ground = PartPose.offset(0, 24, 0); // y = 24 is ground level

        root.addOrReplaceChild("left_track", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-11, -7, -14, 5, 7, 26), ground);
        root.addOrReplaceChild("right_track", CubeListBuilder.create()
                .texOffs(0, 0).mirror().addBox(6, -7, -14, 5, 7, 26), ground);
        root.addOrReplaceChild("hull", CubeListBuilder.create()
                .texOffs(0, 34).addBox(-6, -12, -14, 12, 8, 24), ground);
        root.addOrReplaceChild("front_wheel", CubeListBuilder.create()
                .texOffs(74, 34).addBox(-1.5f, -7, -27, 3, 7, 7), ground);
        root.addOrReplaceChild("fork", CubeListBuilder.create()
                .texOffs(96, 34).addBox(-1, -16, -24, 2, 10, 2), ground);
        root.addOrReplaceChild("handlebar", CubeListBuilder.create()
                .texOffs(74, 50).addBox(-6, -17, -24, 12, 1, 1), ground);
        root.addOrReplaceChild("fender", CubeListBuilder.create()
                .texOffs(74, 64).addBox(-2, -9, -28, 4, 2, 9), ground);
        root.addOrReplaceChild("driver_seat", CubeListBuilder.create()
                .texOffs(74, 54).addBox(-3, -14, -6, 6, 2, 6), ground);
        root.addOrReplaceChild("rear_bench", CubeListBuilder.create()
                .texOffs(0, 68).addBox(-6, -14, 2, 12, 2, 8), ground);

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(KettenkradEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        // No animation yet.
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        root.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
