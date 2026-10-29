package com.ok2603.glt.client;

import com.ok2603.glt.GLTMod;
import com.ok2603.glt.registry.ModEntities;
import com.ok2603.glt.registry.ModMenus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/** Client-only setup: renderers, the body-heat meter, and key bindings. */
@EventBusSubscriber(modid = GLTMod.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {
    /** Opens the Chito / Yuuri menu. Default key: H (can be changed in Controls). */
    public static final KeyMapping ROLE_MENU_KEY = new KeyMapping("key.glt.role_menu",
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_H, "key.categories.glt");

    /** Sit down or stand up. Default key: Z. */
    public static final KeyMapping SIT_KEY = new KeyMapping("key.glt.sit",
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Z, "key.categories.glt");

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(ROLE_MENU_KEY);
        event.register(SIT_KEY);
    }

    /** Builds the Chito and Yuuri player renderers once the game is ready. */
    @SubscribeEvent
    public static void buildRoleRenderers(EntityRenderersEvent.AddLayers event) {
        ClientGameEvents.chitoRenderer = new RolePlayerRenderer(event.getContext(), RolePlayerRenderer.CHITO_SKIN);
        ClientGameEvents.yuuriRenderer = new RolePlayerRenderer(event.getContext(), RolePlayerRenderer.YUURI_SKIN);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.KETTENKRAD.get(), KettenkradRenderer::new);
        event.registerEntityRenderer(ModEntities.SEAT.get(), NoopRenderer::new); // the seat is invisible
        event.registerEntityRenderer(ModEntities.NUKO.get(), NukoRenderer::new);
    }

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(KettenkradModel.LAYER, KettenkradModel::createBodyLayer);
        event.registerLayerDefinition(NukoModel.LAYER, NukoModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.FOOD_FACTORY.get(), FoodFactoryScreen::new);
    }

    @SubscribeEvent
    public static void registerHud(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR,
                ResourceLocation.fromNamespaceAndPath(GLTMod.MODID, "body_heat"), new BodyHeatHud());
        event.registerAbove(VanillaGuiLayers.CROSSHAIR,
                ResourceLocation.fromNamespaceAndPath(GLTMod.MODID, "kettenkrad_prompt"), new KettenkradPromptHud());
    }
}
