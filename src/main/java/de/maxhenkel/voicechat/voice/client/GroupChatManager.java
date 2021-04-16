package de.maxhenkel.voicechat.voice.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import de.maxhenkel.corelib.client.PlayerSkins;
import de.maxhenkel.voicechat.Main;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class GroupChatManager {

    private static final ResourceLocation TALK_OUTLINE = new ResourceLocation(Main.MODID, "textures/gui/talk_outline.png");
    private static final ResourceLocation SPEAKER_OFF_ICON = new ResourceLocation(Main.MODID, "textures/gui/speaker_off.png");
    private static final Minecraft minecraft = Minecraft.getInstance();

    public static void renderIcons(MatrixStack matrixStack) {
        Client client = Main.CLIENT_VOICE_EVENTS.getClient();

        if (client == null) {
            return;
        }

        List<PlayerState> groupMembers = getGroupMembers(false);

        matrixStack.pushPose();
        matrixStack.translate(8, 8, 0);
        matrixStack.scale(2F, 2F, 1F);

        for (int i = 0; i < groupMembers.size(); i++) {
            PlayerState state = groupMembers.get(i);
            matrixStack.pushPose();
            matrixStack.translate(0, i * 11, 0);
            if (client.getTalkCache().isTalking(state.getGameProfile().getId())) {
                minecraft.getTextureManager().bind(TALK_OUTLINE);
                Screen.blit(matrixStack, 0, 0, 0, 0, 10, 10, 16, 16);
            }
            minecraft.getTextureManager().bind(PlayerSkins.getSkin(state.getGameProfile()));
            Screen.blit(matrixStack, 1, 1, 8, 8, 8, 8, 64, 64);
            Screen.blit(matrixStack, 1, 1, 40, 8, 8, 8, 64, 64);

            if (state.isDisabled()) {
                matrixStack.pushPose();
                matrixStack.translate(10, 5, 0);
                matrixStack.scale(0.25F, 0.25F, 1F);
                minecraft.getTextureManager().bind(SPEAKER_OFF_ICON);
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

        for (PlayerState state : Main.CLIENT_VOICE_EVENTS.getPlayerStateManager().getPlayerStates()) {
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
        String group = Main.CLIENT_VOICE_EVENTS.getPlayerStateManager().getGroup();
        if (group == null) {
            return "";
        }
        return group;
    }

}
