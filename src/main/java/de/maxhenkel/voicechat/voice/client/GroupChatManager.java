package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.gui.SkinUtils;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class GroupChatManager {

    private static final Identifier TALK_OUTLINE = new Identifier(Voicechat.MODID, "textures/gui/talk_outline.png");
    private static final Identifier SPEAKER_OFF_ICON = new Identifier(Voicechat.MODID, "textures/gui/speaker_off.png");
    private static final MinecraftClient minecraft = MinecraftClient.getInstance();

    public static void renderIcons(MatrixStack matrixStack) {
        Client client = VoicechatClient.CLIENT.getClient();

        if (client == null) {
            return;
        }

        List<PlayerState> groupMembers = getGroupMembers(false);

        matrixStack.push();
        matrixStack.translate(8, 8, 0);
        matrixStack.scale(2F, 2F, 1F);

        for (int i = 0; i < groupMembers.size(); i++) {
            PlayerState state = groupMembers.get(i);
            matrixStack.push();
            matrixStack.translate(0, i * 11, 0);
            if (client.getTalkCache().isTalking(state.getGameProfile().getId())) {
                minecraft.getTextureManager().bindTexture(TALK_OUTLINE);
                Screen.drawTexture(matrixStack, 0, 0, 0, 0, 10, 10, 16, 16);
            }
            minecraft.getTextureManager().bindTexture(SkinUtils.getSkin(state.getGameProfile()));
            Screen.drawTexture(matrixStack, 1, 1, 8, 8, 8, 8, 64, 64);
            Screen.drawTexture(matrixStack, 1, 1, 40, 8, 8, 8, 64, 64);

            if (state.isDisabled()) {
                matrixStack.push();
                matrixStack.translate(10, 5, 0);
                matrixStack.scale(0.25F, 0.25F, 1F);
                minecraft.getTextureManager().bindTexture(SPEAKER_OFF_ICON);
                Screen.drawTexture(matrixStack, 0, 0, 0, 0, 16, 16, 16, 16);
                matrixStack.pop();
            }

            matrixStack.pop();
        }

        matrixStack.pop();
    }

    public static List<PlayerState> getGroupMembers() {
        return getGroupMembers(true);
    }

    public static List<PlayerState> getGroupMembers(boolean includeSelf) {
        List<PlayerState> entries = new ArrayList<>();
        String group = getGroup();

        for (PlayerState state : VoicechatClient.CLIENT.getPlayerStateManager().getPlayerStates()) {
            if (!includeSelf && state.getGameProfile().getId().equals(MinecraftClient.getInstance().player.getUuid())) {
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
