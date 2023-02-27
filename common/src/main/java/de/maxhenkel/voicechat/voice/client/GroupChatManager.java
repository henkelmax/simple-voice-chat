package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.gui.GameProfileUtils;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class GroupChatManager {

    private static final ResourceLocation TALK_OUTLINE = new ResourceLocation(Voicechat.MODID, "textures/icons/talk_outline.png");
    private static final ResourceLocation SPEAKER_OFF_ICON = new ResourceLocation(Voicechat.MODID, "textures/icons/speaker_small_off.png");

    public static void renderIcons() {
        ClientVoicechat client = ClientManager.getClient();

        if (client == null) {
            return;
        }
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution scaledResolution = new ScaledResolution(mc);

        List<PlayerState> groupMembers = getGroupMembers(VoicechatClient.CLIENT_CONFIG.showOwnGroupIcon.get());

        GlStateManager.pushMatrix();
        int posX = VoicechatClient.CLIENT_CONFIG.groupPlayerIconPosX.get();
        int posY = VoicechatClient.CLIENT_CONFIG.groupPlayerIconPosY.get();
        if (posX < 0) {
            GlStateManager.translate(scaledResolution.getScaledWidth(), 0D, 0D);
        }
        if (posY < 0) {
            GlStateManager.translate(0D, scaledResolution.getScaledHeight(), 0D);
        }
        GlStateManager.translate(posX, posY, 0D);

        float scale = VoicechatClient.CLIENT_CONFIG.groupHudIconScale.get().floatValue();
        GlStateManager.scale(scale, scale, 1F);

        boolean vertical = VoicechatClient.CLIENT_CONFIG.groupPlayerIconOrientation.get().equals(GroupPlayerIconOrientation.VERTICAL);

        for (int i = 0; i < groupMembers.size(); i++) {
            PlayerState state = groupMembers.get(i);
            GlStateManager.pushMatrix();
            if (vertical) {
                if (posY < 0) {
                    GlStateManager.translate(0D, i * -11D, 0D);
                } else {
                    GlStateManager.translate(0D, i * 11D, 0D);
                }
            } else {
                if (posX < 0) {
                    GlStateManager.translate(i * -11D, 0D, 0D);
                } else {
                    GlStateManager.translate(i * 11D, 0D, 0D);
                }
            }

            if (client.getTalkCache().isTalking(state.getUuid())) {
                mc.getTextureManager().bindTexture(TALK_OUTLINE);
                GuiScreen.drawModalRectWithCustomSizedTexture(posX < 0 ? -10 : 0, posY < 0 ? -10 : 0, 0, 0, 10, 10, 16, 16);
            }
            GlStateManager.enableBlend();
            mc.getTextureManager().bindTexture(GameProfileUtils.getSkin(state.getUuid()));
            GuiScreen.drawModalRectWithCustomSizedTexture(posX < 0 ? -1 - 8 : 1, posY < 0 ? -1 - 8 : 1, 8, 8, 8, 8, 64, 64);
            GuiScreen.drawModalRectWithCustomSizedTexture(posX < 0 ? -1 - 8 : 1, posY < 0 ? -1 - 8 : 1, 40, 8, 8, 8, 64, 64);

            if (state.isDisabled()) {
                GlStateManager.pushMatrix();
                GlStateManager.translate((posX < 0 ? -1D - 8D : 1D), posY < 0 ? -1D - 8D : 1D, 0D);
                GlStateManager.scale(0.5F, 0.5F, 1F);
                mc.getTextureManager().bindTexture(SPEAKER_OFF_ICON);
                GuiScreen.drawModalRectWithCustomSizedTexture(0, 0, 0, 0, 16, 16, 16, 16);
                GlStateManager.popMatrix();
            }

            GlStateManager.popMatrix();
        }

        GlStateManager.popMatrix();
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
