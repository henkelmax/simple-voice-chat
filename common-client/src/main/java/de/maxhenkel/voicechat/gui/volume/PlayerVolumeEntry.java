package de.maxhenkel.voicechat.gui.volume;

import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.gui.GameProfileUtils;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.PlayerSkin;

import javax.annotation.Nullable;
import java.util.UUID;

public class PlayerVolumeEntry extends VolumeEntry {

    @Nullable
    protected final PlayerState state;

    public PlayerVolumeEntry(@Nullable PlayerState state, AdjustVolumesScreen screen) {
        super(screen, new PlayerVolumeConfigEntry(state != null ? state.getUuid() : Util.NIL_UUID));
        this.state = state;
    }

    @Nullable
    public PlayerState getState() {
        return state;
    }

    @Override
    public void renderElement(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float delta, int skinX, int skinY, int textX, int textY) {
        guiGraphics.depthTreeUp();
        if (state != null) {
            PlayerSkin skin = GameProfileUtils.getSkin(state.getUuid());
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, skin.texture(), skinX, skinY, 8, 8, SKIN_SIZE, SKIN_SIZE, 8, 8, 64, 64);
            guiGraphics.depthTreeUp();
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, skin.texture(), skinX, skinY, 40, 8, SKIN_SIZE, SKIN_SIZE, 8, 8, 64, 64);
            guiGraphics.depthTreeBack();
            guiGraphics.drawString(minecraft.font, state.getName(), textX, textY, PLAYER_NAME_COLOR, false);
        } else {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, OTHER_VOLUME_ICON, skinX, skinY, 16, 16, SKIN_SIZE, SKIN_SIZE, 16, 16, 16, 16);
            guiGraphics.drawString(minecraft.font, OTHER_VOLUME, textX, textY, PLAYER_NAME_COLOR, false);
            if (hovered) {
                screen.postRender(() -> {
                    guiGraphics.setTooltipForNextFrame(minecraft.font, OTHER_VOLUME_DESCRIPTION, mouseX, mouseY);
                });
            }
        }
        guiGraphics.depthTreeBack();
    }

    public static class PlayerVolumeConfigEntry implements AdjustVolumeSlider.VolumeConfigEntry {

        private final UUID playerUUID;

        public PlayerVolumeConfigEntry(UUID playerUUID) {
            this.playerUUID = playerUUID;
        }

        @Override
        public void save(double value) {
            VoicechatClient.VOLUME_CONFIG.setPlayerVolume(playerUUID, value);
            VoicechatClient.VOLUME_CONFIG.save();
        }

        @Override
        public double get() {
            return VoicechatClient.VOLUME_CONFIG.getPlayerVolume(playerUUID);
        }
    }

}
