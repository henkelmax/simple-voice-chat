package de.maxhenkel.voicechat.voice.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.gui.onboarding.OnboardingManager;
import de.maxhenkel.voicechat.intercompatibility.ClientCompatibilityManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderType;
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

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, texture, posX < 0 ? -16 : 0, posY < 0 ? -16 : 0, 0, 0, 16, 16, 16, 16);
        guiGraphics.pose().popMatrix();
    }

    private void onRenderName(UUID playerId, boolean discrete, Component component, PoseStack.Pose pose, MultiBufferSource vertexConsumers, int light) {
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
        Entity entity = minecraft.level.getEntity(playerId);
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
                renderPlayerIcon(discrete, component, WHISPER_SPEAKER_ICON, pose, vertexConsumers, light);
            } else if (client != null && client.getTalkCache().isTalking(entityId)) {
                renderPlayerIcon(discrete, component, SPEAKER_ICON, pose, vertexConsumers, light);
            } else if (manager.isPlayerDisconnected(entityId)) {
                renderPlayerIcon(discrete, component, DISCONNECT_ICON, pose, vertexConsumers, light);
            } else if (groupId != null && !groupId.equals(manager.getGroupID())) {
                renderPlayerIcon(discrete, component, GROUP_ICON, pose, vertexConsumers, light);
            } else if (manager.isPlayerDisabled(entityId)) {
                renderPlayerIcon(discrete, component, SPEAKER_OFF_ICON, pose, vertexConsumers, light);
            }
        }
    }

    private void renderPlayerIcon(boolean discrete, Component component, ResourceLocation texture, PoseStack.Pose pose, MultiBufferSource buffer, int light) {
        float offset = (float) (minecraft.font.width(component) / 2 + 2);

        VertexConsumer builder = buffer.getBuffer(RenderType.text(texture));
        int alpha = 32;

        float offsetY = -1F;

        if (discrete) {
            vertex(builder, pose, offset, 10F + offsetY, 0F, 0F, 1F, alpha, light);
            vertex(builder, pose, offset + 10F, 10F + offsetY, 0F, 1F, 1F, alpha, light);
            vertex(builder, pose, offset + 10F, offsetY, 0F, 1F, 0F, alpha, light);
            vertex(builder, pose, offset, offsetY, 0F, 0F, 0F, alpha, light);
        } else {
            vertex(builder, pose, offset, 10F + offsetY, 0F, 0F, 1F, light);
            vertex(builder, pose, offset + 10F, 10F + offsetY, 0F, 1F, 1F, light);
            vertex(builder, pose, offset + 10F, offsetY, 0F, 1F, 0F, light);
            vertex(builder, pose, offset, offsetY, 0F, 0F, 0F, light);

            VertexConsumer builderSeeThrough = buffer.getBuffer(RenderType.textSeeThrough(texture));
            vertex(builderSeeThrough, pose, offset, 10F + offsetY, 0F, 0F, 1F, alpha, light);
            vertex(builderSeeThrough, pose, offset + 10F, 10F + offsetY, 0F, 1F, 1F, alpha, light);
            vertex(builderSeeThrough, pose, offset + 10F, offsetY, 0F, 1F, 0F, alpha, light);
            vertex(builderSeeThrough, pose, offset, offsetY, 0F, 0F, 0F, alpha, light);
        }
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

    private static void vertex(VertexConsumer builder, PoseStack.Pose pose, float x, float y, float z, float u, float v, int light) {
        vertex(builder, pose, x, y, z, u, v, 255, light);
    }

    private static void vertex(VertexConsumer builder, PoseStack.Pose pose, float x, float y, float z, float u, float v, int alpha, int light) {
        builder.addVertex(pose.pose(), x, y, z)
                .setColor(255, 255, 255, alpha)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, 0F, 0F, -1F);
    }

}
