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
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;

import java.util.UUID;

public class RenderEvents {

    private static final Identifier MICROPHONE_ICON = Identifier.fromNamespaceAndPath(Voicechat.MODID, "textures/icons/microphone.png");
    private static final Identifier WHISPER_MICROPHONE_ICON = Identifier.fromNamespaceAndPath(Voicechat.MODID, "textures/icons/microphone_whisper.png");
    private static final Identifier MICROPHONE_OFF_ICON = Identifier.fromNamespaceAndPath(Voicechat.MODID, "textures/icons/microphone_off.png");
    private static final Identifier SPEAKER_ICON = Identifier.fromNamespaceAndPath(Voicechat.MODID, "textures/icons/speaker.png");
    private static final Identifier WHISPER_SPEAKER_ICON = Identifier.fromNamespaceAndPath(Voicechat.MODID, "textures/icons/speaker_whisper.png");
    private static final Identifier SPEAKER_OFF_ICON = Identifier.fromNamespaceAndPath(Voicechat.MODID, "textures/icons/speaker_off.png");
    private static final Identifier DISCONNECT_ICON = Identifier.fromNamespaceAndPath(Voicechat.MODID, "textures/icons/disconnected.png");
    private static final Identifier GROUP_ICON = Identifier.fromNamespaceAndPath(Voicechat.MODID, "textures/icons/group.png");

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

    private void renderIcon(GuiGraphics guiGraphics, Identifier texture) {
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

    private void onRenderName(EntityRenderState s, CameraRenderState cameraRenderState, PoseStack stack, SubmitNodeCollector collector) {
        if(!(s instanceof AvatarRenderState state)){
            return;
        }
        Component nameTag = s.nameTag;
        if (nameTag == null) {
            return;
        }
        if (s.nameTagAttachment == null) {
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
        Entity entity = minecraft.level.getEntity(state.id);
        if (entity == null || entity.equals(minecraft.player)) {
            return;
        }
        if (minecraft.options.hideGui) {
            return;
        }
        ClientPlayerStateManager manager = ClientManager.getPlayerStateManager();
        ClientVoicechat client = ClientManager.getClient();
        UUID entityId = entity.getUUID();
        UUID groupId = manager.getGroup(entityId);

        stack.pushPose();
        stack.translate(state.nameTagAttachment);
        stack.translate(0D, 0.5D, 0D);
        stack.mulPose(cameraRenderState.orientation);
        stack.scale(0.025F, -0.025F, 0.025F);

        if (client != null && client.getTalkCache().isWhispering(entityId)) {
            renderPlayerIcon(entityId, state.isDiscrete, nameTag, WHISPER_SPEAKER_ICON, stack, collector, state.lightCoords);
        } else if (client != null && client.getTalkCache().isTalking(entityId)) {
            renderPlayerIcon(entityId, state.isDiscrete, nameTag, SPEAKER_ICON, stack, collector, state.lightCoords);
        } else if (manager.isPlayerDisconnected(entityId)) {
            renderPlayerIcon(entityId, state.isDiscrete, nameTag, DISCONNECT_ICON, stack, collector, state.lightCoords);
        } else if (groupId != null && !groupId.equals(manager.getGroupID())) {
            renderPlayerIcon(entityId, state.isDiscrete, nameTag, GROUP_ICON, stack, collector, state.lightCoords);
        } else if (manager.isPlayerDisabled(entityId)) {
            renderPlayerIcon(entityId, state.isDiscrete, nameTag, SPEAKER_OFF_ICON, stack, collector, state.lightCoords);
        }

        stack.popPose();
    }

    private void renderPlayerIcon(UUID entityId, boolean discrete, Component component, Identifier texture, PoseStack stack, SubmitNodeCollector collector, int light) {
        if (!ClientPluginManager.instance().shouldRenderPlayerIcons(entityId)) {
            return;
        }
        float offsetX = (float) (minecraft.font.width(component) / 2 + 2);
        int alpha = 32;
        float offsetY = -1F;
        collector.submitCustomGeometry(stack, RenderTypes.text(texture), (pose, c) -> {
            if (discrete) {
                vertex(c, pose, offsetX, 10F + offsetY, 0F, 0F, 1F, alpha, light);
                vertex(c, pose, offsetX + 10F, 10F + offsetY, 0F, 1F, 1F, alpha, light);
                vertex(c, pose, offsetX + 10F, offsetY, 0F, 1F, 0F, alpha, light);
                vertex(c, pose, offsetX, offsetY, 0F, 0F, 0F, alpha, light);
            } else {
                vertex(c, pose, offsetX, 10F + offsetY, 0F, 0F, 1F, light);
                vertex(c, pose, offsetX + 10F, 10F + offsetY, 0F, 1F, 1F, light);
                vertex(c, pose, offsetX + 10F, offsetY, 0F, 1F, 0F, light);
                vertex(c, pose, offsetX, offsetY, 0F, 0F, 0F, light);
            }
        });
        if (!discrete) {
            collector.submitCustomGeometry(stack, RenderTypes.textSeeThrough(texture), (pose, c) -> {
                vertex(c, pose, offsetX, 10F + offsetY, 0F, 0F, 1F, alpha, light);
                vertex(c, pose, offsetX + 10F, 10F + offsetY, 0F, 1F, 1F, alpha, light);
                vertex(c, pose, offsetX + 10F, offsetY, 0F, 1F, 0F, alpha, light);
                vertex(c, pose, offsetX, offsetY, 0F, 0F, 0F, alpha, light);
            });
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
