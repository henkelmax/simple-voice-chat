package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.intercompatibility.ClientCompatibilityManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import java.util.UUID;

public class RenderEvents {

    private static final ResourceLocation MICROPHONE_ICON = new ResourceLocation(Voicechat.MODID, "textures/icons/microphone.png");
    private static final ResourceLocation WHISPER_MICROPHONE_ICON = new ResourceLocation(Voicechat.MODID, "textures/icons/microphone_whisper.png");
    private static final ResourceLocation MICROPHONE_OFF_ICON = new ResourceLocation(Voicechat.MODID, "textures/icons/microphone_off.png");
    private static final ResourceLocation SPEAKER_ICON = new ResourceLocation(Voicechat.MODID, "textures/icons/speaker.png");
    private static final ResourceLocation WHISPER_SPEAKER_ICON = new ResourceLocation(Voicechat.MODID, "textures/icons/speaker_whisper.png");
    private static final ResourceLocation SPEAKER_OFF_ICON = new ResourceLocation(Voicechat.MODID, "textures/icons/speaker_off.png");
    private static final ResourceLocation DISCONNECT_ICON = new ResourceLocation(Voicechat.MODID, "textures/icons/disconnected.png");
    private static final ResourceLocation GROUP_ICON = new ResourceLocation(Voicechat.MODID, "textures/icons/group.png");

    private final Minecraft minecraft;

    public RenderEvents() {
        minecraft = Minecraft.getMinecraft();
        ClientCompatibilityManager.INSTANCE.onRenderNamePlate(this::onRenderName);
        ClientCompatibilityManager.INSTANCE.onRenderHUD(this::onRenderHUD);
    }

    private void onRenderHUD(float tickDelta) {
        if (!shouldShowIcons()) {
            return;
        }
        if (VoicechatClient.CLIENT_CONFIG.hideIcons.get()) {
            return;
        }

        ClientPlayerStateManager manager = ClientManager.getPlayerStateManager();
        ClientVoicechat client = ClientManager.getClient();
        if (manager.isDisconnected()) {
            renderIcon(DISCONNECT_ICON);
        } else if (manager.isDisabled()) {
            renderIcon(SPEAKER_OFF_ICON);
        } else if (manager.isMuted() && VoicechatClient.CLIENT_CONFIG.microphoneActivationType.get().equals(MicrophoneActivationType.VOICE)) {
            renderIcon(MICROPHONE_OFF_ICON);
        } else if (client != null && client.getMicThread() != null) {
            if (client.getMicThread().isWhispering()) {
                renderIcon(WHISPER_MICROPHONE_ICON);
            } else if (client.getMicThread().isTalking()) {
                renderIcon(MICROPHONE_ICON);
            }
        }

        if (manager.getGroupID() != null && VoicechatClient.CLIENT_CONFIG.showGroupHUD.get()) {
            GroupChatManager.renderIcons();
        }
    }

    private void renderIcon(ResourceLocation texture) {
        GlStateManager.pushMatrix();
        ScaledResolution scaledResolution = new ScaledResolution(minecraft);
        minecraft.getTextureManager().bindTexture(texture);
        int posX = VoicechatClient.CLIENT_CONFIG.hudIconPosX.get();
        int posY = VoicechatClient.CLIENT_CONFIG.hudIconPosY.get();
        if (posX < 0) {
            GlStateManager.translate(scaledResolution.getScaledWidth(), 0D, 0D);
        }
        if (posY < 0) {
            GlStateManager.translate(0D, scaledResolution.getScaledHeight(), 0D);
        }
        GlStateManager.translate(posX, posY, 0D);
        float scale = VoicechatClient.CLIENT_CONFIG.hudIconScale.get().floatValue();
        GlStateManager.scale(scale, scale, 1F);

        GuiScreen.drawModalRectWithCustomSizedTexture(posX < 0 ? -16 : 0, posY < 0 ? -16 : 0, 0, 0, 16, 16, 16, 16);
        GlStateManager.popMatrix();
    }

    private void onRenderName(Entity entity, String str, double x, double y, double z, int maxDistance) {
        if (!shouldShowIcons()) {
            return;
        }
        if (VoicechatClient.CLIENT_CONFIG.hideIcons.get()) {
            return;
        }
        if (!(entity instanceof EntityPlayer)) {
            return;
        }
        EntityPlayer player = (EntityPlayer) entity;
        if (entity == minecraft.player) {
            return;
        }

        if (!minecraft.gameSettings.hideGUI) {
            ClientPlayerStateManager manager = ClientManager.getPlayerStateManager();
            ClientVoicechat client = ClientManager.getClient();
            UUID groupId = manager.getGroup(player);

            if (client != null && client.getTalkCache().isWhispering(player)) {
                renderPlayerIcon(player, str, x, y, z, maxDistance, WHISPER_SPEAKER_ICON);
            } else if (client != null && client.getTalkCache().isTalking(player)) {
                renderPlayerIcon(player, str, x, y, z, maxDistance, SPEAKER_ICON);
            } else if (manager.isPlayerDisconnected(player)) {
                renderPlayerIcon(player, str, x, y, z, maxDistance, DISCONNECT_ICON);
            } else if (groupId != null && !groupId.equals(manager.getGroupID())) {
                renderPlayerIcon(player, str, x, y, z, maxDistance, GROUP_ICON);
            } else if (manager.isPlayerDisabled(player)) {
                renderPlayerIcon(player, str, x, y, z, maxDistance, SPEAKER_OFF_ICON);
            }
        }
    }

    private void renderPlayerIcon(EntityPlayer entity, String str, double x, double y, double z, int maxDistance, ResourceLocation texture) {
        RenderManager renderManager = minecraft.getRenderManager();
        boolean isThirdPersonFrontal = renderManager.options.thirdPersonView == 2;
        int verticalShift = "deadmau5".equals(str) ? -10 : 0;

        float height = entity.height + 0.5F - (entity.isSneaking() ? 0.25F : 0F);

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y + height, z);
        GlStateManager.glNormal3f(0F, 1F, 0F);
        GlStateManager.rotate(-renderManager.playerViewY, 0F, 1F, 0F);
        GlStateManager.rotate((float) (isThirdPersonFrontal ? -1 : 1) * renderManager.playerViewX, 1F, 0F, 0F);
        GlStateManager.scale(-0.025F, -0.025F, 0.025F);
        GlStateManager.disableLighting();
        GlStateManager.depthMask(false);

        if (!entity.isSneaking()) {
            GlStateManager.disableDepth();
        }

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        int halfNameWidth = minecraft.fontRenderer.getStringWidth(str) / 2;
        GlStateManager.translate(halfNameWidth, verticalShift - 1F, 0F);
        if (!entity.isSneaking()) {
            drawIcon(texture, true);
            GlStateManager.enableDepth();
        }

        GlStateManager.depthMask(true);
        drawIcon(texture, false);
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.color(1F, 1F, 1F, 1F);
        GlStateManager.popMatrix();
    }

    private void drawIcon(ResourceLocation texture, boolean transparent) {
        minecraft.getTextureManager().bindTexture(texture);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        bufferbuilder.pos(2D, 10D, 0D).tex(0D, 1D).color(255, 255, 255, transparent ? 32 : 255).endVertex();
        bufferbuilder.pos(2D + 10D, 10D, 0D).tex(1D, 1D).color(255, 255, 255, transparent ? 32 : 255).endVertex();
        bufferbuilder.pos(2D + 10D, 0D, 0D).tex(1D, 0D).color(255, 255, 255, transparent ? 32 : 255).endVertex();
        bufferbuilder.pos(2D, 0D, 0D).tex(0D, 0D).color(255, 255, 255, transparent ? 32 : 255).endVertex();
        tessellator.draw();
    }

    private boolean shouldShowIcons() {
        if (ClientManager.getClient() != null && ClientManager.getClient().getConnection() != null && ClientManager.getClient().getConnection().isInitialized()) {
            return true;
        }
        return minecraft.getIntegratedServer() != null && !minecraft.getIntegratedServer().getPublic();
    }

}
