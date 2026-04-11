package de.maxhenkel.voicechat.voice.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.gui.onboarding.OnboardingManager;
import de.maxhenkel.voicechat.intercompatibility.ClientCompatibilityManager;
import de.maxhenkel.voicechat.plugins.ClientPluginManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import java.util.UUID;

public class RenderEvents {

    private static final ResourceLocation MICROPHONE_ICON = ResourceLocation.fromNamespaceAndPath(Voicechat.MODID, "icons/microphone");
    private static final ResourceLocation WHISPER_MICROPHONE_ICON = ResourceLocation.fromNamespaceAndPath(Voicechat.MODID, "icons/microphone_whisper");
    private static final ResourceLocation MICROPHONE_OFF_ICON = ResourceLocation.fromNamespaceAndPath(Voicechat.MODID, "icons/microphone_off");
    private static final ResourceLocation SPEAKER_ICON = ResourceLocation.fromNamespaceAndPath(Voicechat.MODID, "icons/speaker");
    private static final ResourceLocation WHISPER_SPEAKER_ICON = ResourceLocation.fromNamespaceAndPath(Voicechat.MODID, "icons/speaker_whisper");
    private static final ResourceLocation SPEAKER_OFF_ICON = ResourceLocation.fromNamespaceAndPath(Voicechat.MODID, "icons/speaker_off");
    private static final ResourceLocation DISCONNECT_ICON = ResourceLocation.fromNamespaceAndPath(Voicechat.MODID, "icons/disconnected");
    private static final ResourceLocation GROUP_ICON = ResourceLocation.fromNamespaceAndPath(Voicechat.MODID, "icons/group");

    private final Minecraft minecraft;

    public RenderEvents() {
        minecraft = Minecraft.getInstance();
        ClientCompatibilityManager.INSTANCE.onRenderNamePlate(this::onRenderName);
        ClientCompatibilityManager.INSTANCE.onRenderHUD(this::onRenderHUD);
    }

    private void onRenderHUD(GuiGraphics guiGraphics, float tickDelta) {
        if (!shouldShowIcons()) {
            return;
        }
        if (VoicechatClient.CLIENT_CONFIG.hideIcons.get()) {
            return;
        }
        if (!VoicechatClient.CLIENT_CONFIG.showHudIcons.get()) {
            return;
        }

        ClientPlayerStateManager manager = ClientManager.getPlayerStateManager();
        ClientVoicechat client = ClientManager.getClient();

        if (manager.isDisconnected() && isStartup()) {
            return;
        }

        if (manager.isDisconnected()) {
            renderIcon(guiGraphics, DISCONNECT_ICON);
        } else if (manager.isDisabled()) {
            renderIcon(guiGraphics, SPEAKER_OFF_ICON);
        } else if (manager.isMuted() && VoicechatClient.CLIENT_CONFIG.microphoneActivationType.get().equals(MicrophoneActivationType.VOICE)) {
            renderIcon(guiGraphics, MICROPHONE_OFF_ICON);
        } else if (client != null && client.getMicThread() != null) {
            if (client.getMicThread().isWhispering()) {
                renderIcon(guiGraphics, WHISPER_MICROPHONE_ICON);
            } else if (client.getMicThread().isTalking()) {
                renderIcon(guiGraphics, MICROPHONE_ICON);
            }
        }

        if (manager.getGroupID() != null && VoicechatClient.CLIENT_CONFIG.showGroupHud.get()) {
            GroupChatManager.renderIcons(guiGraphics);
        }
    }

    private boolean isStartup() {
        ClientVoicechat client = ClientManager.getClient();
        return client != null && (System.currentTimeMillis() - client.getStartTime()) < 5000;
    }

    private void renderIcon(GuiGraphics guiGraphics, ResourceLocation texture) {
        guiGraphics.pose().pushMatrix();
        int posX = VoicechatClient.CLIENT_CONFIG.hudIconPosX.get();
        int posY = VoicechatClient.CLIENT_CONFIG.hudIconPosY.get();
        if (posX < 0) {
            guiGraphics.pose().translate(minecraft.getWindow().getGuiScaledWidth(), 0F);
        }
        if (posY < 0) {
            guiGraphics.pose().translate(0F, minecraft.getWindow().getGuiScaledHeight());
        }
        guiGraphics.pose().translate(posX, posY);
        float scale = VoicechatClient.CLIENT_CONFIG.hudIconScale.get().floatValue();
        guiGraphics.pose().scale(scale, scale);

        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, texture, posX < 0 ? -16 : 0, posY < 0 ? -16 : 0, 16, 16);
        guiGraphics.pose().popMatrix();
    }

    private void onRenderName(EntityRenderState renderState, Component component, PoseStack stack, MultiBufferSource vertexConsumers, int light) {
        if (component == null) {
            return;
        }
        if (!shouldShowIcons()) {
            return;
        }
        if (VoicechatClient.CLIENT_CONFIG.hideIcons.get()) {
            return;
        }
        if (!VoicechatClient.CLIENT_CONFIG.showNametagIcons.get()) {
            return;
        }
        if (minecraft.player == null || minecraft.level == null) {
            return;
        }
        if (!(renderState instanceof PlayerRenderState playerRenderState)) {
            return;
        }
        Entity entity = minecraft.level.getEntity(playerRenderState.id);
        if (entity == null) {
            return;
        }
        if (minecraft.player.equals(entity)) {
            return;
        }
        if (!minecraft.options.hideGui) {
            ClientPlayerStateManager manager = ClientManager.getPlayerStateManager();
            ClientVoicechat client = ClientManager.getClient();
            UUID entityId = entity.getUUID();
            UUID groupId = manager.getGroup(entityId);

            if (client != null && client.getTalkCache().isWhispering(entityId)) {
                renderPlayerIcon(entityId, renderState, component, WHISPER_SPEAKER_ICON, stack, vertexConsumers, light);
            } else if (client != null && client.getTalkCache().isTalking(entityId)) {
                renderPlayerIcon(entityId, renderState, component, SPEAKER_ICON, stack, vertexConsumers, light);
            } else if (manager.isPlayerDisconnected(entityId)) {
                renderPlayerIcon(entityId, renderState, component, DISCONNECT_ICON, stack, vertexConsumers, light);
            } else if (groupId != null && !groupId.equals(manager.getGroupID())) {
                renderPlayerIcon(entityId, renderState, component, GROUP_ICON, stack, vertexConsumers, light);
            } else if (manager.isPlayerDisabled(entityId)) {
                renderPlayerIcon(entityId, renderState, component, SPEAKER_OFF_ICON, stack, vertexConsumers, light);
            }
        }
    }

    private void renderPlayerIcon(UUID entityId, EntityRenderState renderState, Component component, ResourceLocation texture, PoseStack poseStack, MultiBufferSource buffer, int light) {
        if (renderState.nameTagAttachment == null) {
            return;
        }
        if (!ClientPluginManager.instance().shouldRenderPlayerIcons(entityId)) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(renderState.nameTagAttachment.x, renderState.nameTagAttachment.y + 0.5D, renderState.nameTagAttachment.z);
        poseStack.mulPose(minecraft.getEntityRenderDispatcher().cameraOrientation());
        poseStack.scale(0.025F, -0.025F, 0.025F);
        poseStack.translate(0D, -1D, 0D);

        float offset = (float) (minecraft.font.width(component) / 2 + 2);

        TextureAtlasSprite sprite = minecraft.getGuiSprites().getSprite(texture);

        VertexConsumer builder = buffer.getBuffer(RenderType.text(sprite.atlasLocation()));
        int alpha = 32;

        if (renderState.isDiscrete) {
            vertex(builder, poseStack, offset, 10F, 0F, sprite.getU0(), sprite.getV1(), alpha, light);
            vertex(builder, poseStack, offset + 10F, 10F, 0F, sprite.getU1(), sprite.getV1(), alpha, light);
            vertex(builder, poseStack, offset + 10F, 0F, 0F, sprite.getU1(), sprite.getV0(), alpha, light);
            vertex(builder, poseStack, offset, 0F, 0F, sprite.getU0(), sprite.getV0(), alpha, light);
        } else {
            vertex(builder, poseStack, offset, 10F, 0F, sprite.getU0(), sprite.getV1(), light);
            vertex(builder, poseStack, offset + 10F, 10F, 0F, sprite.getU1(), sprite.getV1(), light);
            vertex(builder, poseStack, offset + 10F, 0F, 0F, sprite.getU1(), sprite.getV0(), light);
            vertex(builder, poseStack, offset, 0F, 0F, sprite.getU0(), sprite.getV0(), light);

            VertexConsumer builderSeeThrough = buffer.getBuffer(RenderType.textSeeThrough(sprite.atlasLocation()));
            vertex(builderSeeThrough, poseStack, offset, 10F, 0F, sprite.getU0(), sprite.getV1(), alpha, light);
            vertex(builderSeeThrough, poseStack, offset + 10F, 10F, 0F, sprite.getU1(), sprite.getV1(), alpha, light);
            vertex(builderSeeThrough, poseStack, offset + 10F, 0F, 0F, sprite.getU1(), sprite.getV0(), alpha, light);
            vertex(builderSeeThrough, poseStack, offset, 0F, 0F, sprite.getU0(), sprite.getV0(), alpha, light);
        }

        poseStack.popPose();
    }

    private boolean shouldShowIcons() {
        if (OnboardingManager.isOnboarding()) {
            return false;
        }
        if (ClientManager.getClient() != null && ClientManager.getClient().getConnection() != null && ClientManager.getClient().getConnection().isInitialized()) {
            return true;
        }
        return minecraft.getSingleplayerServer() == null || minecraft.getSingleplayerServer().isPublished();
    }

    private static void vertex(VertexConsumer builder, PoseStack matrixStack, float x, float y, float z, float u, float v, int light) {
        vertex(builder, matrixStack, x, y, z, u, v, 255, light);
    }

    private static void vertex(VertexConsumer builder, PoseStack matrixStack, float x, float y, float z, float u, float v, int alpha, int light) {
        PoseStack.Pose entry = matrixStack.last();
        builder.addVertex(entry.pose(), x, y, z)
                .setColor(255, 255, 255, alpha)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(entry, 0F, 0F, -1F);
    }

}
