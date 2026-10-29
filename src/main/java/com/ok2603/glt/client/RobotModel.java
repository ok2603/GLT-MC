package com.ok2603.glt.client;

import com.ok2603.glt.GLTMod;
import com.ok2603.glt.entity.RobotEntity;
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

/** An old maintenance robot: a boxy body on two treads, a round-ish head with one eye, and thin arms. */
public class RobotModel extends HierarchicalModel<RobotEntity> {
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(GLTMod.MODID, "robot"), "main");

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart leftArm;
    private final ModelPart rightArm;

    public RobotModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.leftArm = root.getChild("left_arm");
        this.rightArm = root.getChild("right_arm");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition parts = mesh.getRoot();
        parts.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-5.0f, -7.0f, -4.0f, 10, 7, 8), PartPose.offset(0.0f, 20.0f, 0.0f));
        PartDefinition head = parts.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 16)
                .addBox(-3.0f, -5.0f, -3.0f, 6, 5, 6), PartPose.offset(0.0f, 13.0f, 0.0f));
        head.addOrReplaceChild("antenna", CubeListBuilder.create().texOffs(40, 16)
                .addBox(-0.5f, -3.0f, -0.5f, 1, 3, 1), PartPose.offset(1.5f, -5.0f, 0.0f));
        CubeListBuilder tread = CubeListBuilder.create().texOffs(0, 28).addBox(-1.5f, 0.0f, -5.0f, 3, 4, 10);
        parts.addOrReplaceChild("left_tread", tread, PartPose.offset(3.5f, 20.0f, 0.0f));
        parts.addOrReplaceChild("right_tread", tread, PartPose.offset(-3.5f, 20.0f, 0.0f));
        CubeListBuilder arm = CubeListBuilder.create().texOffs(28, 16).addBox(-1.0f, 0.0f, -1.0f, 2, 7, 2);
        parts.addOrReplaceChild("left_arm", arm, PartPose.offset(6.0f, 14.0f, 0.0f));
        parts.addOrReplaceChild("right_arm", arm, PartPose.offset(-6.0f, 14.0f, 0.0f));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(RobotEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                          float netHeadYaw, float headPitch) {
        head.yRot = netHeadYaw * Mth.DEG_TO_RAD;
        head.xRot = headPitch * Mth.DEG_TO_RAD * 0.5f;
        // Arms tinker a little, as if fixing something.
        leftArm.xRot = -0.3f + Mth.sin(ageInTicks * 0.15f) * 0.2f;
        rightArm.xRot = -0.3f + Mth.cos(ageInTicks * 0.13f) * 0.2f;
    }
}
