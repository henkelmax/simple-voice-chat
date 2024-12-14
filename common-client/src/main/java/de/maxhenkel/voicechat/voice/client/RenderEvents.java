package de.maxhenkel.voicechat.voice.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.gui.onboarding.OnboardingManager;
import de.maxhenkel.voicechat.intercompatibility.ClientCompatibilityManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.CoreShaders;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import java.util.UUID;

public class RenderEvents {

    private static final ResourceLocation MICROPHONE_ICON = ResourceLocation.fromNamespaceAndPath(Voicechat.MODID, "textures/icons/microphone.png");
    private static final ResourceLocation WHISPER_MICROPHONE_ICON = ResourceLocation.fromNamespaceAndPath(Voicechat.MODID, "textures/icons/microphone_whisper.png");
    private static final ResourceLocation MICROPHONE_OFF_ICON = ResourceLocation.fromNamespaceAndPath(Voicechat.MODID, "textures/icons/microphone_off.png");
    private static final ResourceLocation SPEAKER_ICON = ResourceLocation.fromNamespaceAndPath(Voicechat.MODID, "textures/icons/speaker.png");
    private static final ResourceLocation WHISPER_SPEAKER_ICON = ResourceLocation.fromNamespaceAndPath(Voicechat.MODID, "textures/icons/speaker_whisper.png");
    private static final ResourceLocation SPEAKER_OFF_ICON = ResourceLocation.fromNamespaceAndPath(Voicechat.MODID, "textures/icons/speaker_off.png");
    private static final ResourceLocation DISCONNECT_ICON = ResourceLocation.fromNamespaceAndPath(Voicechat.MODID, "textures/icons/disconnected.png");
    private static final ResourceLocation GROUP_ICON = ResourceLocation.fromNamespaceAndPath(Voicechat.MODID, "textures/icons/group.png");

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

        if (manager.getGroupID() != null && VoicechatClient.CLIENT_CONFIG.showGroupHUD.get()) {
            GroupChatManager.renderIcons(guiGraphics);
        }
    }

    private boolean isStartup() {
        ClientVoicechat client = ClientManager.getClient();
        return client != null && (System.currentTimeMillis() - client.getStartTime()) < 5000;
    }

    private void renderIcon(GuiGraphics guiGraphics, ResourceLocation texture) {
        guiGraphics.pose().pushPose();
        RenderSystem.setShader(CoreShaders.POSITION_TEX);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        int posX = VoicechatClient.CLIENT_CONFIG.hudIconPosX.get();
        int posY = VoicechatClient.CLIENT_CONFIG.hudIconPosY.get();
        if (posX < 0) {
            guiGraphics.pose().translate(minecraft.getWindow().getGuiScaledWidth(), 0D, 0D);
        }
        if (posY < 0) {
            guiGraphics.pose().translate(0D, minecraft.getWindow().getGuiScaledHeight(), 0D);
        }
        guiGraphics.pose().translate(posX, posY, 0D);
        float scale = VoicechatClient.CLIENT_CONFIG.hudIconScale.get().floatValue();
        guiGraphics.pose().scale(scale, scale, 1F);

        guiGraphics.blit(RenderType::guiTextured, texture, posX < 0 ? -16 : 0, posY < 0 ? -16 : 0, 0, 0, 16, 16, 16, 16);
        guiGraphics.pose().popPose();
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
                renderPlayerIcon(renderState, component, WHISPER_SPEAKER_ICON, stack, vertexConsumers, light);
            } else if (client != null && client.getTalkCache().isTalking(entityId)) {
                renderPlayerIcon(renderState, component, SPEAKER_ICON, stack, vertexConsumers, light);
            } else if (manager.isPlayerDisconnected(entityId)) {
                renderPlayerIcon(renderState, component, DISCONNECT_ICON, stack, vertexConsumers, light);
            } else if (groupId != null && !groupId.equals(manager.getGroupID())) {
                renderPlayerIcon(renderState, component, GROUP_ICON, stack, vertexConsumers, light);
            } else if (manager.isPlayerDisabled(entityId)) {
                renderPlayerIcon(renderState, component, SPEAKER_OFF_ICON, stack, vertexConsumers, light);
            }
        }
    }

    private void renderPlayerIcon(EntityRenderState renderState, Component component, ResourceLocation texture, PoseStack poseStack, MultiBufferSource buffer, int light) {
        if (renderState.nameTagAttachment == null) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(renderState.nameTagAttachment.x, renderState.nameTagAttachment.y + 0.5D, renderState.nameTagAttachment.z);
        poseStack.mulPose(minecraft.getEntityRenderDispatcher().cameraOrientation());
        poseStack.scale(0.025F, -0.025F, 0.025F);
        poseStack.translate(0D, -1D, 0D);

        float offset = (float) (minecraft.font.width(component) / 2 + 2);

        VertexConsumer builder = buffer.getBuffer(RenderType.text(texture));
        int alpha = 32;

        if (renderState.isDiscrete) {
            vertex(builder, poseStack, offset, 10F, 0F, 0F, 1F, alpha, light);
            vertex(builder, poseStack, offset + 10F, 10F, 0F, 1F, 1F, alpha, light);
            vertex(builder, poseStack, offset + 10F, 0F, 0F, 1F, 0F, alpha, light);
            vertex(builder, poseStack, offset, 0F, 0F, 0F, 0F, alpha, light);
        } else {
            vertex(builder, poseStack, offset, 10F, 0F, 0F, 1F, light);
            vertex(builder, poseStack, offset + 10F, 10F, 0F, 1F, 1F, light);
            vertex(builder, poseStack, offset + 10F, 0F, 0F, 1F, 0F, light);
            vertex(builder, poseStack, offset, 0F, 0F, 0F, 0F, light);

            VertexConsumer builderSeeThrough = buffer.getBuffer(RenderType.textSeeThrough(texture));
            vertex(builderSeeThrough, poseStack, offset, 10F, 0F, 0F, 1F, alpha, light);
            vertex(builderSeeThrough, poseStack, offset + 10F, 10F, 0F, 1F, 1F, alpha, light);
            vertex(builderSeeThrough, poseStack, offset + 10F, 0F, 0F, 1F, 0F, alpha, light);
            vertex(builderSeeThrough, poseStack, offset, 0F, 0F, 0F, 0F, alpha, light);
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
