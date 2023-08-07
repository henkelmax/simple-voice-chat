package de.maxhenkel.voicechat.voice.client;

import com.mojang.blaze3d.systems.RenderSystem;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.gui.GameProfileUtils;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class GroupChatManager {

    private static final ResourceLocation TALK_OUTLINE = new ResourceLocation(Voicechat.MODID, "textures/icons/talk_outline.png");
    private static final ResourceLocation SPEAKER_OFF_ICON = new ResourceLocation(Voicechat.MODID, "textures/icons/speaker_small_off.png");

    public static void renderIcons(GuiGraphics guiGraphics) {
        ClientVoicechat client = ClientManager.getClient();

        if (client == null) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();

        List<PlayerState> groupMembers = getGroupMembers(VoicechatClient.CLIENT_CONFIG.showOwnGroupIcon.get());

        guiGraphics.pose().pushPose();
        int posX = VoicechatClient.CLIENT_CONFIG.groupPlayerIconPosX.get();
        int posY = VoicechatClient.CLIENT_CONFIG.groupPlayerIconPosY.get();
        if (posX < 0) {
            guiGraphics.pose().translate(mc.getWindow().getGuiScaledWidth(), 0D, 0D);
        }
        if (posY < 0) {
            guiGraphics.pose().translate(0D, mc.getWindow().getGuiScaledHeight(), 0D);
        }
        guiGraphics.pose().translate(posX, posY, 0D);

        float scale = VoicechatClient.CLIENT_CONFIG.groupHudIconScale.get().floatValue();
        guiGraphics.pose().scale(scale, scale, 1F);

        boolean vertical = VoicechatClient.CLIENT_CONFIG.groupPlayerIconOrientation.get().equals(GroupPlayerIconOrientation.VERTICAL);

        for (int i = 0; i < groupMembers.size(); i++) {
            PlayerState state = groupMembers.get(i);
            guiGraphics.pose().pushPose();
            if (vertical) {
                if (posY < 0) {
                    guiGraphics.pose().translate(0D, i * -11D, 0D);
                } else {
                    guiGraphics.pose().translate(0D, i * 11D, 0D);
                }
            } else {
                if (posX < 0) {
                    guiGraphics.pose().translate(i * -11D, 0D, 0D);
                } else {
                    guiGraphics.pose().translate(i * 11D, 0D, 0D);
                }
            }

            if (client.getTalkCache().isTalking(state.getUuid())) {
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
                guiGraphics.blit(TALK_OUTLINE, posX < 0 ? -10 : 0, posY < 0 ? -10 : 0, 0, 0, 10, 10, 16, 16);
            }
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            PlayerSkin skin = GameProfileUtils.getSkin(state.getUuid());
            guiGraphics.blit(skin.texture(), posX < 0 ? -1 - 8 : 1, posY < 0 ? -1 - 8 : 1, 8, 8, 8, 8, 64, 64);
            guiGraphics.blit(skin.texture(), posX < 0 ? -1 - 8 : 1, posY < 0 ? -1 - 8 : 1, 40, 8, 8, 8, 64, 64);

            if (state.isDisabled()) {
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate((posX < 0 ? -1D - 8D : 1D), posY < 0 ? -1D - 8D : 1D, 0D);
                guiGraphics.pose().scale(0.5F, 0.5F, 1F);
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
                guiGraphics.blit(SPEAKER_OFF_ICON, 0, 0, 0, 0, 16, 16, 16, 16);
                guiGraphics.pose().popPose();
            }

            guiGraphics.pose().popPose();
        }

        guiGraphics.pose().popPose();
    }

    public static List<PlayerState> getGroupMembers() {
        return getGroupMembers(true);
    }

    public static List<PlayerState> getGroupMembers(boolean includeSelf) {
        List<PlayerState> entries = new ArrayList<>();
        UUID group = ClientManager.getPlayerStateManager().getGroupID();

        if (group == null) {
            return entries;
        }

        for (PlayerState state : ClientManager.getPlayerStateManager().getPlayerStates(includeSelf)) {
            if (state.hasGroup() && state.getGroup().equals(group)) {
                entries.add(state);
            }
        }

        entries.sort(Comparator.comparing(PlayerState::getName));

        return entries;
    }

}
