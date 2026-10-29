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
 * The 3D shape of the Kettenkrad: tracks with road wheels, the front wheel and
 * fork, the engine hood, seats, side storage boxes, fuel drum, bags and rear rails.
 * Units are pixels (16 = one block). The front of the vehicle is at negative Z.
 *
 * The same model is in blockbench/kettenkrad.bbmodel. After editing it in Blockbench,
 * export it with File > Export > Java Entity (Mojmaps) and replace createBodyLayer below.
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
                .texOffs(0, 0).addBox(-9, -6, -12, 4, 6, 30), ground);
        root.addOrReplaceChild("right_track", CubeListBuilder.create()
                .texOffs(0, 0).addBox(5, -6, -12, 4, 6, 30), ground);
        root.addOrReplaceChild("hull", CubeListBuilder.create()
                .texOffs(0, 36).addBox(-5, -9, -12, 10, 7, 30), ground);
        root.addOrReplaceChild("left_fender", CubeListBuilder.create()
                .texOffs(0, 73).addBox(-9, -8, -10, 4, 2, 28), ground);
        root.addOrReplaceChild("right_fender", CubeListBuilder.create()
                .texOffs(0, 73).addBox(5, -8, -10, 4, 2, 28), ground);
        root.addOrReplaceChild("nose", CubeListBuilder.create()
                .texOffs(64, 73).addBox(-5, -11, -15, 10, 3, 10), ground);
        root.addOrReplaceChild("driver_seat", CubeListBuilder.create()
                .texOffs(104, 73).addBox(-3, -12, -6, 6, 2, 5), ground);
        root.addOrReplaceChild("seat_back", CubeListBuilder.create()
                .texOffs(0, 103).addBox(-3, -15, -2, 6, 3, 1), ground);
        root.addOrReplaceChild("hood", CubeListBuilder.create()
                .texOffs(14, 103).addBox(-5, -15, -1, 10, 6, 9), ground);
        root.addOrReplaceChild("toolbox", CubeListBuilder.create()
                .texOffs(52, 103).addBox(-4, -19, 4, 5, 4, 4), ground);
        root.addOrReplaceChild("rear_bed", CubeListBuilder.create()
                .texOffs(70, 103).addBox(-8, -11, 8, 16, 2, 10), ground);
        root.addOrReplaceChild("left_storage", CubeListBuilder.create()
                .texOffs(0, 118).addBox(-12, -13, -2, 3, 6, 18), ground);
        root.addOrReplaceChild("right_storage", CubeListBuilder.create()
                .texOffs(0, 118).addBox(9, -13, -2, 3, 6, 18), ground);
        root.addOrReplaceChild("fuel_drum", CubeListBuilder.create()
                .texOffs(42, 118).addBox(-11, -16, 1, 3, 3, 8), ground);
        root.addOrReplaceChild("can_1", CubeListBuilder.create()
                .texOffs(64, 118).addBox(9, -15, 10, 2, 2, 2), ground);
        root.addOrReplaceChild("can_2", CubeListBuilder.create()
                .texOffs(64, 118).addBox(9, -15, 13, 2, 2, 2), ground);
        root.addOrReplaceChild("bag_1", CubeListBuilder.create()
                .texOffs(72, 118).addBox(-6, -14, 12, 5, 3, 5), ground);
        root.addOrReplaceChild("bag_2", CubeListBuilder.create()
                .texOffs(92, 118).addBox(1, -14, 13, 5, 3, 4), ground);
        root.addOrReplaceChild("rail_left", CubeListBuilder.create()
                .texOffs(110, 118).addBox(-8, -17, 18, 1, 6, 1), ground);
        root.addOrReplaceChild("rail_right", CubeListBuilder.create()
                .texOffs(110, 118).addBox(7, -17, 18, 1, 6, 1), ground);
        root.addOrReplaceChild("rail_top", CubeListBuilder.create()
                .texOffs(0, 142).addBox(-8, -18, 18, 16, 1, 1), ground);
        root.addOrReplaceChild("tailgate", CubeListBuilder.create()
                .texOffs(34, 142).addBox(-7, -13, 18, 14, 2, 1), ground);
        root.addOrReplaceChild("front_wheel", CubeListBuilder.create()
                .texOffs(64, 142).addBox(-1, -8, -24, 2, 8, 8), ground);
        root.addOrReplaceChild("mudguard", CubeListBuilder.create()
                .texOffs(84, 142).addBox(-2, -10, -25, 4, 1, 9), ground);
        root.addOrReplaceChild("left_wheel_0", CubeListBuilder.create()
                .texOffs(110, 142).addBox(-10, -6, -12, 1, 5, 5), ground);
        root.addOrReplaceChild("right_wheel_0", CubeListBuilder.create()
                .texOffs(110, 142).addBox(9, -6, -12, 1, 5, 5), ground);
        root.addOrReplaceChild("left_wheel_1", CubeListBuilder.create()
                .texOffs(110, 142).addBox(-10, -6, -6, 1, 5, 5), ground);
        root.addOrReplaceChild("right_wheel_1", CubeListBuilder.create()
                .texOffs(110, 142).addBox(9, -6, -6, 1, 5, 5), ground);
        root.addOrReplaceChild("left_wheel_2", CubeListBuilder.create()
                .texOffs(110, 142).addBox(-10, -6, 0, 1, 5, 5), ground);
        root.addOrReplaceChild("right_wheel_2", CubeListBuilder.create()
                .texOffs(110, 142).addBox(9, -6, 0, 1, 5, 5), ground);
        root.addOrReplaceChild("left_wheel_3", CubeListBuilder.create()
                .texOffs(110, 142).addBox(-10, -6, 6, 1, 5, 5), ground);
        root.addOrReplaceChild("right_wheel_3", CubeListBuilder.create()
                .texOffs(110, 142).addBox(9, -6, 6, 1, 5, 5), ground);
        root.addOrReplaceChild("left_wheel_4", CubeListBuilder.create()
                .texOffs(110, 142).addBox(-10, -6, 12, 1, 5, 5), ground);
        root.addOrReplaceChild("right_wheel_4", CubeListBuilder.create()
                .texOffs(110, 142).addBox(9, -6, 12, 1, 5, 5), ground);
        // The fork, headlight and handlebars lean back around the front wheel's hub.
        root.addOrReplaceChild("fork", CubeListBuilder.create()
                .texOffs(122, 142).addBox(-2, -11, -1, 1, 11, 1)
                .texOffs(122, 142).addBox(1, -11, -1, 1, 11, 1)
                .texOffs(0, 158).addBox(-2, -12, -1, 4, 1, 2)
                .texOffs(12, 158).addBox(-2, -15, -3, 4, 3, 2)
                .texOffs(24, 158).addBox(-7, -14, 0, 14, 1, 1),
                PartPose.offsetAndRotation(0, 20, -20, -0.3f, 0, 0));

        return LayerDefinition.create(mesh, 128, 256);
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
