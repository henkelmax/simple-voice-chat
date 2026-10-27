package de.maxhenkel.voicechat.voice.client;

import com.mojang.blaze3d.vertex.PoseStack;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.gui.onboarding.OnboardingManager;
import de.maxhenkel.voicechat.intercompatibility.ClientCompatibilityManager;
import de.maxhenkel.voicechat.plugins.ClientPluginManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.GuiPipeline;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.data.AtlasIds;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.objects.AtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.Entity;

import java.util.UUID;

public class RenderEvents {

    private static final Identifier MICROPHONE_ICON = Identifier.fromNamespaceAndPath(Voicechat.MODID, "icons/microphone");
    private static final Identifier WHISPER_MICROPHONE_ICON = Identifier.fromNamespaceAndPath(Voicechat.MODID, "icons/microphone_whisper");
    private static final Identifier MICROPHONE_OFF_ICON = Identifier.fromNamespaceAndPath(Voicechat.MODID, "icons/microphone_off");
    private static final Identifier SPEAKER_ICON = Identifier.fromNamespaceAndPath(Voicechat.MODID, "icons/speaker");
    private static final Identifier WHISPER_SPEAKER_ICON = Identifier.fromNamespaceAndPath(Voicechat.MODID, "icons/speaker_whisper");
    private static final Identifier SPEAKER_OFF_ICON = Identifier.fromNamespaceAndPath(Voicechat.MODID, "icons/speaker_off");
    private static final Identifier DISCONNECT_ICON = Identifier.fromNamespaceAndPath(Voicechat.MODID, "icons/disconnected");
    private static final Identifier GROUP_ICON = Identifier.fromNamespaceAndPath(Voicechat.MODID, "icons/group");

    private final Minecraft minecraft;

    public RenderEvents() {
        minecraft = Minecraft.getInstance();
        ClientCompatibilityManager.INSTANCE.onRenderNamePlate(this::onRenderName);
        ClientCompatibilityManager.INSTANCE.onRenderHUD(this::onRenderHUD);
    }

    private void onRenderHUD(GuiGraphicsExtractor guiGraphics, float tickDelta) {
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

        if (!ClientPluginManager.instance().shouldRenderHudIcons()) {
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

    private void renderIcon(GuiGraphicsExtractor guiGraphics, Identifier texture) {
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

        guiGraphics.blitSprite(GuiPipeline.TEXTURED, texture, posX < 0 ? -16 : 0, posY < 0 ? -16 : 0, 16, 16);
        guiGraphics.pose().popMatrix();
    }

    private void onRenderName(EntityRenderState s, CameraRenderState cameraRenderState, PoseStack stack, SubmitNodeCollector collector) {
        if (!(s instanceof AvatarRenderState state)) {
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
        if (minecraft.gui.hud.isHidden()) {
            return;
        }
        ClientPlayerStateManager manager = ClientManager.getPlayerStateManager();
        ClientVoicechat client = ClientManager.getClient();
        UUID entityId = entity.getUUID();
        UUID groupId = manager.getGroup(entityId);

        stack.pushPose();
        stack.translate(state.nameTagAttachment);
        stack.translate(0D, 0.5D, 0D);
        stack.rotate(cameraRenderState.orientation);
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
        if (!ClientPluginManager.instance().shouldRenderPlayerIcons(entityId, texture == DISCONNECT_ICON)) {
            return;
        }
        float offsetX = (float) (minecraft.font.width(component) / 2 + 2);
        float offsetY = -1F;
        // See SumbitNodeCollection.submitNameTag
        float backgroundAlpha = minecraft.gameRenderer.gameRenderState().optionsRenderState.getBackgroundOpacity(0.25F);
        int translucentColor = ARGB.color(Math.max((backgroundAlpha + 0.75F) * 0.5F, 0.5F), 0xFFFFFF);
        FormattedCharSequence icon = Component.object(new AtlasSprite(AtlasIds.GUI, texture)).getVisualOrderText();
        stack.pushPose();
        stack.translate(offsetX, offsetY, 0F);
        // MC font is 8x8, VC icons are 10x10
        stack.scale(10F / 8F, 10F / 8F, 1F);
        collector.submitText(stack, 0F, 1F, icon, false, Font.DisplayMode.NORMAL, light, discrete ? translucentColor : 0xFFFFFFFF, 0, 0);
        if (!discrete) {
            collector.submitText(stack, 0F, 1F, icon, false, Font.DisplayMode.SEE_THROUGH, light, translucentColor, 0, 0);
        }
        stack.popPose();
    }

    private boolean shouldShowIcons() {
        if (ClientPluginManager.instance().shouldForceShowIcons()) {
            return true;
        }
        if (OnboardingManager.isOnboarding()) {
            return false;
        }
        if (ClientManager.getClient() != null && ClientManager.getClient().getConnection() != null && ClientManager.getClient().getConnection().isInitialized()) {
            return true;
        }
        return minecraft.getSingleplayerServer() == null || minecraft.getSingleplayerServer().isPublished();
    }

}
