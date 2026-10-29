package com.ok2603.glt.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.ok2603.glt.GLTMod;
import com.ok2603.glt.network.SitPayload;
import com.ok2603.glt.registry.ModItems;
import com.ok2603.glt.survival.HotBath;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import com.ok2603.glt.role.Role;
import com.ok2603.glt.role.Roles;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderArmEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/** Client-side game events: the H and Z keys, and drawing players as Chito or Yuuri. */
@EventBusSubscriber(modid = GLTMod.MODID, value = Dist.CLIENT)
public class ClientGameEvents {
    /** Built in ClientSetup once the game's renderers are ready. */
    static RolePlayerRenderer chitoRenderer;
    static RolePlayerRenderer yuuriRenderer;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        NukoRadio.tick(mc);
        while (ClientSetup.ROLE_MENU_KEY.consumeClick()) {
            if (mc.screen == null && mc.player != null) {
                mc.setScreen(new RoleSelectScreen());
            }
        }
        while (ClientSetup.SIT_KEY.consumeClick()) {
            if (mc.screen == null && mc.player != null) {
                PacketDistributor.sendToServer(SitPayload.INSTANCE);
            }
        }
    }

    /** A freshly washed coat says so in its tooltip. */
    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null && event.getItemStack().is(ModItems.CHITOS_COAT.get())
                && HotBath.isFreshlyWashed(event.getItemStack(), mc.level)) {
            event.getToolTip().add(Component.translatable("item.glt.chitos_coat.washed").withStyle(ChatFormatting.AQUA));
        }
    }

    private static RolePlayerRenderer rendererFor(Player player) {
        Role role = Roles.get(player);
        if (role == Role.CHITO) {
            return chitoRenderer;
        }
        if (role == Role.YUURI) {
            return yuuriRenderer;
        }
        return null;
    }

    /** Draw players who picked a role with that character's look instead of their own skin. */
    @SubscribeEvent
    public static void onRenderPlayer(RenderPlayerEvent.Pre event) {
        if (event.getRenderer() instanceof RolePlayerRenderer) {
            return; // this is our own renderer drawing, let it through
        }
        RolePlayerRenderer renderer = rendererFor(event.getEntity());
        if (renderer == null || !(event.getEntity() instanceof AbstractClientPlayer player)) {
            return;
        }
        event.setCanceled(true);
        float partialTick = event.getPartialTick();
        float yaw = Mth.lerp(partialTick, player.yRotO, player.getYRot());
        renderer.render(player, yaw, partialTick, event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight());
    }

    /** First-person arm with the character's skin. */
    @SubscribeEvent
    public static void onRenderArm(RenderArmEvent event) {
        RolePlayerRenderer renderer = rendererFor(event.getPlayer());
        if (renderer == null) {
            return;
        }
        event.setCanceled(true);

        AbstractClientPlayer player = event.getPlayer();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource buffers = event.getMultiBufferSource();
        int light = event.getPackedLight();

        PlayerModel<AbstractClientPlayer> model = renderer.getModel();
        model.attackTime = 0.0f;
        model.crouching = false;
        model.swimAmount = 0.0f;
        model.setupAnim(player, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);

        boolean right = event.getArm() == HumanoidArm.RIGHT;
        ModelPart arm = right ? model.rightArm : model.leftArm;
        ModelPart sleeve = right ? model.rightSleeve : model.leftSleeve;
        arm.xRot = 0.0f;
        arm.render(poseStack, buffers.getBuffer(RenderType.entitySolid(renderer.getSkin())), light, OverlayTexture.NO_OVERLAY);
        sleeve.xRot = 0.0f;
        sleeve.render(poseStack, buffers.getBuffer(RenderType.entityTranslucent(renderer.getSkin())), light, OverlayTexture.NO_OVERLAY);
    }
}
