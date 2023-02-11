package de.maxhenkel.voicechat.voice.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.gui.GameProfileUtils;
import de.maxhenkel.voicechat.voice.common.ClientGroup;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GroupChatManager {

    private static final ResourceLocation TALK_OUTLINE = new ResourceLocation(Voicechat.MODID, "textures/icons/talk_outline.png");
    private static final ResourceLocation SPEAKER_OFF_ICON = new ResourceLocation(Voicechat.MODID, "textures/icons/speaker_small_off.png");

    public static void renderIcons(PoseStack matrixStack) {
        ClientVoicechat client = ClientManager.getClient();

        if (client == null) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();

        List<PlayerState> groupMembers = getGroupMembers(VoicechatClient.CLIENT_CONFIG.showOwnGroupIcon.get());

        matrixStack.pushPose();
        int posX = VoicechatClient.CLIENT_CONFIG.groupPlayerIconPosX.get();
        int posY = VoicechatClient.CLIENT_CONFIG.groupPlayerIconPosY.get();
        if (posX < 0) {
            matrixStack.translate(mc.getWindow().getGuiScaledWidth(), 0D, 0D);
        }
        if (posY < 0) {
            matrixStack.translate(0D, mc.getWindow().getGuiScaledHeight(), 0D);
        }
        matrixStack.translate(posX, posY, 0D);

        float scale = VoicechatClient.CLIENT_CONFIG.groupHudIconScale.get().floatValue();
        matrixStack.scale(scale, scale, 1F);

        boolean vertical = VoicechatClient.CLIENT_CONFIG.groupPlayerIconOrientation.get().equals(GroupPlayerIconOrientation.VERTICAL);

        for (int i = 0; i < groupMembers.size(); i++) {
            PlayerState state = groupMembers.get(i);
            matrixStack.pushPose();
            if (vertical) {
                if (posY < 0) {
                    matrixStack.translate(0D, i * -11D, 0D);
                } else {
                    matrixStack.translate(0D, i * 11D, 0D);
                }
            } else {
                if (posX < 0) {
                    matrixStack.translate(i * -11D, 0D, 0D);
                } else {
                    matrixStack.translate(i * 11D, 0D, 0D);
                }
            }

            if (client.getTalkCache().isTalking(state.getUuid())) {
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
                RenderSystem.setShaderTexture(0, TALK_OUTLINE);
                Screen.blit(matrixStack, posX < 0 ? -10 : 0, posY < 0 ? -10 : 0, 0, 0, 10, 10, 16, 16);
            }
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderTexture(0, GameProfileUtils.getSkin(state.getUuid()));
            Screen.blit(matrixStack, posX < 0 ? -1 - 8 : 1, posY < 0 ? -1 - 8 : 1, 8, 8, 8, 8, 64, 64);
            Screen.blit(matrixStack, posX < 0 ? -1 - 8 : 1, posY < 0 ? -1 - 8 : 1, 40, 8, 8, 8, 64, 64);

            if (state.isDisabled()) {
                matrixStack.pushPose();
                matrixStack.translate((posX < 0 ? -1D - 8D : 1D), posY < 0 ? -1D - 8D : 1D, 0D);
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
        ClientGroup group = ClientManager.getPlayerStateManager().getGroup();

        if (group == null) {
            return entries;
        }

        for (PlayerState state : ClientManager.getPlayerStateManager().getPlayerStates(includeSelf)) {
            if (state.hasGroup() && state.getGroup().equals(group.getId())) {
                entries.add(state);
            }
        }

        entries.sort(Comparator.comparing(PlayerState::getName));

        return entries;
    }

}
