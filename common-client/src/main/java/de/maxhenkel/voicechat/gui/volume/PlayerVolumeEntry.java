package de.maxhenkel.voicechat.gui.volume;

import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.gui.GameProfileUtils;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.client.ClientVoicechat;
import de.maxhenkel.voicechat.voice.common.AudioUtils;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.PlayerSkin;

import javax.annotation.Nullable;
import java.util.UUID;

public class PlayerVolumeEntry extends VolumeEntry {

    @Nullable
    protected final PlayerState state;

    public PlayerVolumeEntry(@Nullable PlayerState state, AdjustVolumesScreen screen) {
        super(screen, new AdjustPlayerVolumeEntry(state != null ? state.getUuid() : Util.NIL_UUID));
        this.state = state;
    }

    @Nullable
    public PlayerState getState() {
        return state;
    }

    @Override
    public void renderElement(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float delta, int skinX, int skinY, int textX, int textY) {
        if (state != null) {
            PlayerSkin skin = GameProfileUtils.getSkin(state.getUuid());
            guiGraphics.blit(RenderType::guiTextured, skin.texture(), skinX, skinY, 8, 8, SKIN_SIZE, SKIN_SIZE, 8, 8, 64, 64);
            guiGraphics.blit(RenderType::guiTextured, skin.texture(), skinX, skinY, 40, 8, SKIN_SIZE, SKIN_SIZE, 8, 8, 64, 64);
            guiGraphics.drawString(minecraft.font, state.getName(), textX, textY, PLAYER_NAME_COLOR, false);
        } else {
            guiGraphics.blit(RenderType::guiTextured, OTHER_VOLUME_ICON, skinX, skinY, 16, 16, SKIN_SIZE, SKIN_SIZE, 16, 16, 16, 16);
            guiGraphics.drawString(minecraft.font, OTHER_VOLUME, textX, textY, PLAYER_NAME_COLOR, false);
            if (hovered) {
                screen.postRender(() -> {
                    guiGraphics.renderTooltip(minecraft.font, OTHER_VOLUME_DESCRIPTION, mouseX, mouseY);
                });
            }
        }
    }

    public static class AdjustPlayerVolumeEntry implements AdjustVolumeSlider.AdjustVolumeEntry {

        private final UUID playerUUID;

        public AdjustPlayerVolumeEntry(UUID playerUUID) {
            this.playerUUID = playerUUID;
        }

        @Override
        public void save(double value) {
            VoicechatClient.PLAYER_VOLUME_CONFIG.setVolume(playerUUID, value);
            VoicechatClient.PLAYER_VOLUME_CONFIG.save();
        }

        @Override
        public double get() {
            return VoicechatClient.PLAYER_VOLUME_CONFIG.getVolume(playerUUID);
        }

        @Override
        public double getAudioLevel() {
            ClientVoicechat client = ClientManager.getClient();
            if (client == null) {
                return AudioUtils.LOWEST_DB;
            }
            return client.getTalkCache().getPlayerAudioLevel(playerUUID);
        }
    }

}
