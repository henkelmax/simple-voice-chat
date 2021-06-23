package de.maxhenkel.voicechat.voice.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.gui.SkinUtils;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class GroupChatManager {

    private static final ResourceLocation TALK_OUTLINE = new ResourceLocation(Voicechat.MODID, "textures/gui/talk_outline.png");
    private static final ResourceLocation SPEAKER_OFF_ICON = new ResourceLocation(Voicechat.MODID, "textures/gui/speaker_group_hud_small_off.png");

    public static void renderIcons(PoseStack matrixStack) {
        Client client = VoicechatClient.CLIENT.getClient();

        if (client == null) {
            return;
        }

        List<PlayerState> groupMembers = getGroupMembers(VoicechatClient.CLIENT_CONFIG.showOwnGroupIcon.get());

        matrixStack.pushPose();
        float scale = VoicechatClient.CLIENT_CONFIG.groupHudIconScale.get().floatValue();
        matrixStack.scale(scale, scale, 1F);
        matrixStack.translate(4, 4, 0);

        for (int i = 0; i < groupMembers.size(); i++) {
            PlayerState state = groupMembers.get(i);
            matrixStack.pushPose();
            if (VoicechatClient.CLIENT_CONFIG.groupPlayerIconOrientation.get().equals(GroupPlayerIconOrientation.VERTICAL)) {
                matrixStack.translate(0, i * 11, 0);
            } else {
                matrixStack.translate(i * 11, 0, 0);
            }

            if (client.getTalkCache().isTalking(state.getGameProfile().getId())) {
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
                RenderSystem.setShaderTexture(0, TALK_OUTLINE);
                Screen.blit(matrixStack, 0, 0, 0, 0, 10, 10, 16, 16);
            }
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
            RenderSystem.setShaderTexture(0, SkinUtils.getSkin(state.getGameProfile()));
            Screen.blit(matrixStack, 1, 1, 8, 8, 8, 8, 64, 64);
            Screen.blit(matrixStack, 1, 1, 40, 8, 8, 8, 64, 64);

            if (state.isDisabled()) {
                matrixStack.pushPose();
                matrixStack.translate(10, 1, 0);
                matrixStack.scale(0.5F, 0.5F, 1F);
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
                RenderSystem.setShaderTexture(0, SPEAKER_OFF_ICON);
                Screen.blit(matrixStack, 0, 0, 0, 0, 16, 16, 16, 16);
                matrixStack.popPose();
            }

            matrixStack.popPose();
        }

        matrixStack.popPose();
    }

    public static List<PlayerState> getGroupMembers() {
        return getGroupMembers(true);
    }

    public static List<PlayerState> getGroupMembers(boolean includeSelf) {
        List<PlayerState> entries = new ArrayList<>();
        String group = getGroup();

        for (PlayerState state : VoicechatClient.CLIENT.getPlayerStateManager().getPlayerStates()) {
            if (!includeSelf && state.getGameProfile().getId().equals(Minecraft.getInstance().player.getUUID())) {
                continue;
            }
            if (state.getGroup() != null && state.getGroup().equals(group)) {
                entries.add(state);
            }
        }

        return entries;
    }

    public static String getGroup() {
        String group = VoicechatClient.CLIENT.getPlayerStateManager().getGroup();
        if (group == null) {
            return "";
        }
        return group;
    }

}
