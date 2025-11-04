package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.gui.GameProfileUtils;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerSkin;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class GroupChatManager {

    private static final Identifier TALK_OUTLINE = Identifier.fromNamespaceAndPath(Voicechat.MODID, "textures/icons/talk_outline.png");
    private static final Identifier SPEAKER_OFF_ICON = Identifier.fromNamespaceAndPath(Voicechat.MODID, "textures/icons/speaker_small_off.png");

    public static void renderIcons(GuiGraphics guiGraphics) {
        ClientVoicechat client = ClientManager.getClient();

        if (client == null) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();

        List<PlayerState> groupMembers = getGroupMembers(VoicechatClient.CLIENT_CONFIG.showOwnGroupIcon.get());

        guiGraphics.pose().pushMatrix();
        int posX = VoicechatClient.CLIENT_CONFIG.groupPlayerIconPosX.get();
        int posY = VoicechatClient.CLIENT_CONFIG.groupPlayerIconPosY.get();
        if (posX < 0) {
            guiGraphics.pose().translate(mc.getWindow().getGuiScaledWidth(), 0F);
        }
        if (posY < 0) {
            guiGraphics.pose().translate(0F, mc.getWindow().getGuiScaledHeight());
        }
        guiGraphics.pose().translate(posX, posY);

        float scale = VoicechatClient.CLIENT_CONFIG.groupHudIconScale.get().floatValue();
        guiGraphics.pose().scale(scale, scale);

        boolean vertical = VoicechatClient.CLIENT_CONFIG.groupPlayerIconOrientation.get().equals(GroupPlayerIconOrientation.VERTICAL);

        for (int i = 0; i < groupMembers.size(); i++) {
            PlayerState state = groupMembers.get(i);
            guiGraphics.pose().pushMatrix();
            if (vertical) {
                if (posY < 0) {
                    guiGraphics.pose().translate(0F, i * -11F);
                } else {
                    guiGraphics.pose().translate(0F, i * 11F);
                }
            } else {
                if (posX < 0) {
                    guiGraphics.pose().translate(i * -11F, 0F);
                } else {
                    guiGraphics.pose().translate(i * 11F, 0F);
                }
            }

            if (client.getTalkCache().isTalking(state.getUuid())) {
                guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TALK_OUTLINE, posX < 0 ? -10 : 0, posY < 0 ? -10 : 0, 0, 0, 10, 10, 16, 16);
            }
            PlayerSkin skin = GameProfileUtils.getSkin(state.getUuid());
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, skin.body().texturePath(), posX < 0 ? -1 - 8 : 1, posY < 0 ? -1 - 8 : 1, 8, 8, 8, 8, 64, 64);
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, skin.body().texturePath(), posX < 0 ? -1 - 8 : 1, posY < 0 ? -1 - 8 : 1, 40, 8, 8, 8, 64, 64);

            if (state.isDisabled()) {
                guiGraphics.pose().pushMatrix();
                guiGraphics.pose().translate((posX < 0 ? -1F - 8F : 1F), posY < 0 ? -1F - 8F : 1F);
                guiGraphics.pose().scale(0.5F, 0.5F);
                guiGraphics.blit(RenderPipelines.GUI_TEXTURED, SPEAKER_OFF_ICON, 0, 0, 0, 0, 16, 16, 16, 16);
                guiGraphics.pose().popMatrix();
            }

            guiGraphics.pose().popMatrix();
        }

        guiGraphics.pose().popMatrix();
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
