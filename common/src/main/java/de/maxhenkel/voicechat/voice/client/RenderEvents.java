package de.maxhenkel.voicechat.voice.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.intercompatibility.ClientCompatibilityManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

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
        minecraft = Minecraft.getInstance();
        ClientCompatibilityManager.INSTANCE.onRenderNamePlate(this::onRenderName);
        ClientCompatibilityManager.INSTANCE.onRenderHUD(this::onRenderHUD);
    }

    private void onRenderHUD(MatrixStack stack, float tickDelta) {
        if (!shouldShowIcons()) {
            return;
        }
        if (VoicechatClient.CLIENT_CONFIG.hideIcons.get()) {
            return;
        }

        ClientPlayerStateManager manager = ClientManager.getPlayerStateManager();
        ClientVoicechat client = ClientManager.getClient();
        if (manager.isDisconnected()) {
            renderIcon(stack, DISCONNECT_ICON);
        } else if (manager.isDisabled()) {
            renderIcon(stack, SPEAKER_OFF_ICON);
        } else if (manager.isMuted() && VoicechatClient.CLIENT_CONFIG.microphoneActivationType.get().equals(MicrophoneActivationType.VOICE)) {
            renderIcon(stack, MICROPHONE_OFF_ICON);
        } else if (client != null && client.getMicThread() != null) {
            if (client.getMicThread().isWhispering()) {
                renderIcon(stack, WHISPER_MICROPHONE_ICON);
            } else if (client.getMicThread().isTalking()) {
                renderIcon(stack, MICROPHONE_ICON);
            }
        }

        if (manager.isInGroup() && VoicechatClient.CLIENT_CONFIG.showGroupHUD.get()) {
            GroupChatManager.renderIcons(stack);
        }
    }

    private void renderIcon(MatrixStack matrixStack, ResourceLocation texture) {
        matrixStack.pushPose();
        minecraft.getTextureManager().bind(texture);
        int posX = VoicechatClient.CLIENT_CONFIG.hudIconPosX.get();
        int posY = VoicechatClient.CLIENT_CONFIG.hudIconPosY.get();
        if (posX < 0) {
            matrixStack.translate(minecraft.getWindow().getGuiScaledWidth(), 0D, 0D);
        }
        if (posY < 0) {
            matrixStack.translate(0D, minecraft.getWindow().getGuiScaledHeight(), 0D);
        }
        matrixStack.translate(posX, posY, 0D);
        float scale = VoicechatClient.CLIENT_CONFIG.hudIconScale.get().floatValue();
        matrixStack.scale(scale, scale, 1F);

        Screen.blit(matrixStack, posX < 0 ? -16 : 0, posY < 0 ? -16 : 0, 0, 0, 16, 16, 16, 16);
        matrixStack.popPose();
    }

    private void onRenderName(Entity entity, ITextComponent component, MatrixStack stack, IRenderTypeBuffer vertexConsumers, int light) {
        if (!shouldShowIcons()) {
            return;
        }
        if (VoicechatClient.CLIENT_CONFIG.hideIcons.get()) {
            return;
        }
        if (!(entity instanceof PlayerEntity)) {
            return;
        }
        PlayerEntity player = (PlayerEntity) entity;
        if (entity == minecraft.player) {
            return;
        }

        if (!minecraft.options.hideGui) {
            ClientPlayerStateManager manager = ClientManager.getPlayerStateManager();
            ClientVoicechat client = ClientManager.getClient();
            UUID groupId = manager.getGroup(player);

            if (client != null && client.getTalkCache().isWhispering(player)) {
                renderPlayerIcon(player, component, WHISPER_SPEAKER_ICON, stack, vertexConsumers, light);
            } else if (client != null && client.getTalkCache().isTalking(player)) {
                renderPlayerIcon(player, component, SPEAKER_ICON, stack, vertexConsumers, light);
            } else if (manager.isPlayerDisconnected(player)) {
                renderPlayerIcon(player, component, DISCONNECT_ICON, stack, vertexConsumers, light);
            } else if (groupId != null && !groupId.equals(manager.getGroup() == null ? null : manager.getGroup().getId())) { // TODO Change
                renderPlayerIcon(player, component, GROUP_ICON, stack, vertexConsumers, light);
            } else if (manager.isPlayerDisabled(player)) {
                renderPlayerIcon(player, component, SPEAKER_OFF_ICON, stack, vertexConsumers, light);
            }
        }
    }

    private void renderPlayerIcon(PlayerEntity player, ITextComponent component, ResourceLocation texture, MatrixStack matrixStackIn, IRenderTypeBuffer buffer, int light) {
        matrixStackIn.pushPose();
        matrixStackIn.translate(0D, player.getBbHeight() + 0.5D, 0D);
        matrixStackIn.mulPose(minecraft.getEntityRenderDispatcher().cameraOrientation());
        matrixStackIn.scale(-0.025F, -0.025F, 0.025F);
        matrixStackIn.translate(0D, -1D, 0D);

        float offset = (float) (minecraft.font.width(component) / 2 + 2);

        IVertexBuilder builder = buffer.getBuffer(RenderType.text(texture));
        int alpha = 32;

        if (player.isDiscrete()) {
            vertex(builder, matrixStackIn, offset, 10F, 0F, 0F, 1F, alpha, light);
            vertex(builder, matrixStackIn, offset + 10F, 10F, 0F, 1F, 1F, alpha, light);
            vertex(builder, matrixStackIn, offset + 10F, 0F, 0F, 1F, 0F, alpha, light);
            vertex(builder, matrixStackIn, offset, 0F, 0F, 0F, 0F, alpha, light);
        } else {
            vertex(builder, matrixStackIn, offset, 10F, 0F, 0F, 1F, light);
            vertex(builder, matrixStackIn, offset + 10F, 10F, 0F, 1F, 1F, light);
            vertex(builder, matrixStackIn, offset + 10F, 0F, 0F, 1F, 0F, light);
            vertex(builder, matrixStackIn, offset, 0F, 0F, 0F, 0F, light);

            IVertexBuilder builderSeeThrough = buffer.getBuffer(RenderType.textSeeThrough(texture));
            vertex(builderSeeThrough, matrixStackIn, offset, 10F, 0F, 0F, 1F, alpha, light);
            vertex(builderSeeThrough, matrixStackIn, offset + 10F, 10F, 0F, 1F, 1F, alpha, light);
            vertex(builderSeeThrough, matrixStackIn, offset + 10F, 0F, 0F, 1F, 0F, alpha, light);
            vertex(builderSeeThrough, matrixStackIn, offset, 0F, 0F, 0F, 0F, alpha, light);
        }

        matrixStackIn.popPose();
    }

    private boolean shouldShowIcons() {
        if (ClientManager.getClient() != null && ClientManager.getClient().getConnection() != null && ClientManager.getClient().getConnection().isInitialized()) {
            return true;
        }
        return minecraft.getCurrentServer() != null && !minecraft.getCurrentServer().isLan();
    }

    private static void vertex(IVertexBuilder builder, MatrixStack matrixStack, float x, float y, float z, float u, float v, int light) {
        vertex(builder, matrixStack, x, y, z, u, v, 255, light);
    }

    private static void vertex(IVertexBuilder builder, MatrixStack matrixStack, float x, float y, float z, float u, float v, int alpha, int light) {
        MatrixStack.Entry entry = matrixStack.last();
        builder.vertex(entry.pose(), x, y, z)
                .color(255, 255, 255, alpha)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(entry.normal(), 0F, 0F, -1F)
                .endVertex();
    }

}
