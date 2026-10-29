package com.ok2603.glt.client;

import com.ok2603.glt.GLTMod;
import com.ok2603.glt.entity.NukoEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** Nuko's model: a round white body, a big head with long floppy ears, stubby legs. */
public class NukoModel extends HierarchicalModel<NukoEntity> {
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(GLTMod.MODID, "nuko"), "main");

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart leftEar;
    private final ModelPart rightEar;
    private final ModelPart frontLeftLeg;
    private final ModelPart frontRightLeg;
    private final ModelPart backLeftLeg;
    private final ModelPart backRightLeg;
    private final ModelPart tail;

    public NukoModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.leftEar = head.getChild("left_ear");
        this.rightEar = head.getChild("right_ear");
        this.frontLeftLeg = root.getChild("front_left_leg");
        this.frontRightLeg = root.getChild("front_right_leg");
        this.backLeftLeg = root.getChild("back_left_leg");
        this.backRightLeg = root.getChild("back_right_leg");
        this.tail = root.getChild("tail");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition parts = mesh.getRoot();

        parts.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-3.0f, -3.0f, -4.0f, 6, 5, 8), PartPose.offset(0.0f, 19.0f, 0.0f));

        PartDefinition head = parts.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 13)
                .addBox(-3.5f, -5.0f, -5.0f, 7, 6, 6), PartPose.offset(0.0f, 18.0f, -3.0f));
        // Long ears that lie back along the head and droop a little.
        head.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(28, 0)
                .addBox(-1.0f, -0.5f, 0.0f, 2, 1, 6), PartPose.offsetAndRotation(2.5f, -4.5f, -2.5f, 0.35f, 0.25f, 0.2f));
        head.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(28, 0).mirror()
                .addBox(-1.0f, -0.5f, 0.0f, 2, 1, 6), PartPose.offsetAndRotation(-2.5f, -4.5f, -2.5f, 0.35f, -0.25f, -0.2f));

        CubeListBuilder leg = CubeListBuilder.create().texOffs(44, 0).addBox(-1.0f, 0.0f, -1.0f, 2, 3, 2);
        parts.addOrReplaceChild("front_left_leg", leg, PartPose.offset(2.0f, 21.0f, -2.5f));
        parts.addOrReplaceChild("front_right_leg", leg, PartPose.offset(-2.0f, 21.0f, -2.5f));
        parts.addOrReplaceChild("back_left_leg", leg, PartPose.offset(2.0f, 21.0f, 2.5f));
        parts.addOrReplaceChild("back_right_leg", leg, PartPose.offset(-2.0f, 21.0f, 2.5f));

        parts.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(44, 5)
                .addBox(-0.5f, -0.5f, 0.0f, 1, 1, 3), PartPose.offsetAndRotation(0.0f, 18.0f, 4.0f, 0.6f, 0.0f, 0.0f));
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(NukoEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                          float netHeadYaw, float headPitch) {
        head.yRot = netHeadYaw * Mth.DEG_TO_RAD;
        head.xRot = headPitch * Mth.DEG_TO_RAD;

        float swing = Mth.cos(limbSwing * 1.2f) * 1.2f * limbSwingAmount;
        frontLeftLeg.xRot = swing;
        backRightLeg.xRot = swing;
        frontRightLeg.xRot = -swing;
        backLeftLeg.xRot = -swing;

        // Ears bob gently, more when walking.
        float bob = Mth.sin(ageInTicks * 0.1f) * 0.06f + limbSwingAmount * 0.25f * Mth.sin(limbSwing * 1.2f);
        leftEar.xRot = 0.35f + bob;
        rightEar.xRot = 0.35f + bob;
        tail.yRot = Mth.sin(ageInTicks * 0.15f) * 0.3f;
    }
}
