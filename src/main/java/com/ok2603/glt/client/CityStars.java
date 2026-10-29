package com.ok2603.glt.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import com.ok2603.glt.GLTMod;
import com.ok2603.glt.city.CityLayout;
import com.ok2603.glt.city.LastCity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * The Last City's night sky: thousands of extra stars on top of vanilla's, brighter the
 * higher you climb. Hidden by clouds and snow. Built the same way vanilla builds its stars.
 */
@EventBusSubscriber(modid = GLTMod.MODID, value = Dist.CLIENT)
public class CityStars {
    private static final int STAR_COUNT = 4000;
    private static VertexBuffer stars;

    @SubscribeEvent
    public static void onRenderStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_SKY) {
            return;
        }
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || !LastCity.isCity(level)) {
            return;
        }
        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        float height = Mth.clamp((float) (event.getCamera().getPosition().y / CityLayout.floorY(CityLayout.FLOORS - 1)), 0f, 1f);
        float brightness = level.getStarBrightness(partialTick) * (1f - level.getRainLevel(partialTick)) * (0.35f + 0.65f * height);
        if (brightness <= 0.01f) {
            return;
        }
        if (stars == null) {
            stars = buildStars();
        }

        PoseStack pose = new PoseStack();
        pose.mulPose(event.getModelViewMatrix());
        pose.mulPose(Axis.YP.rotationDegrees(-90.0f));
        pose.mulPose(Axis.XP.rotationDegrees(level.getTimeOfDay(partialTick) * 360.0f));

        float fogStart = RenderSystem.getShaderFogStart();
        RenderSystem.setShaderFogStart(Float.MAX_VALUE);
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        RenderSystem.setShaderColor(brightness, brightness, brightness, brightness);
        stars.bind();
        stars.drawWithShader(pose.last().pose(), event.getProjectionMatrix(), GameRenderer.getPositionShader());
        VertexBuffer.unbind();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.setShaderFogStart(fogStart);
    }

    private static VertexBuffer buildStars() {
        RandomSource random = RandomSource.create(20171006L);
        BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        for (int i = 0; i < STAR_COUNT; i++) {
            float x = random.nextFloat() * 2f - 1f;
            float y = random.nextFloat() * 2f - 1f;
            float z = random.nextFloat() * 2f - 1f;
            float size = 0.08f + random.nextFloat() * random.nextFloat() * 0.22f;
            float lengthSq = x * x + y * y + z * z;
            if (lengthSq <= 0.01f || lengthSq >= 1f) {
                continue;
            }
            Vector3f center = new Vector3f(x, y, z).normalize(100f);
            float spin = (float) (random.nextDouble() * Math.PI * 2);
            Quaternionf rotation = new Quaternionf().rotateTo(new Vector3f(0f, 0f, -1f), center).rotateZ(spin);
            float[][] corners = {{size, -size}, {size, size}, {-size, size}, {-size, -size}};
            for (float[] corner : corners) {
                Vector3f p = new Vector3f(corner[0], corner[1], 0f).rotate(rotation).add(center);
                builder.addVertex(p.x, p.y, p.z);
            }
        }
        VertexBuffer buffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
        buffer.bind();
        buffer.upload(builder.buildOrThrow());
        VertexBuffer.unbind();
        return buffer;
    }
}
