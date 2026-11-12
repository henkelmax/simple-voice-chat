package de.maxhenkel.voicechat.gui.volume;

import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.gui.GameProfileUtils;
import de.maxhenkel.voicechat.voice.client.TalkCache;
import de.maxhenkel.voicechat.voice.common.AudioUtils;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;

import java.util.UUID;

public class PlayerVolumeEntry extends VolumeEntry {

    protected final PlayerState state;

    public PlayerVolumeEntry(PlayerState state, AdjustVolumesScreen screen) {
        super(screen, new AdjustPlayerVolumeEntry(state.getUuid(), state.getName()), false, false);
        this.state = state;
    }

    @Override
    public void renderElement(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks, int skinX, int skinY, int textX, int textY) {
        GlStateManager.color(1F, 1F, 1F, 1F);
        minecraft.getTextureManager().bindTexture(GameProfileUtils.getSkin(state.getUuid()));
        Gui.drawScaledCustomSizeModalRect(skinX, skinY, 8F, 8F, 8, 8, SKIN_SIZE, SKIN_SIZE, 64F, 64F);
        GlStateManager.enableBlend();
        Gui.drawScaledCustomSizeModalRect(skinX, skinY, 40F, 8F, 8, 8, SKIN_SIZE, SKIN_SIZE, 64F, 64F);
        GlStateManager.disableBlend();
        minecraft.fontRenderer.drawString(state.getName(), textX, textY, PLAYER_NAME_COLOR);
    }

    public static class AdjustPlayerVolumeEntry implements AdjustVolumeSlider.AdjustVolumeEntry {

        private final UUID playerUUID;
        private final String playerName;

        public AdjustPlayerVolumeEntry(UUID playerUUID, String playerName) {
            this.playerUUID = playerUUID;
            this.playerName = playerName;
        }

        @Override
        public void save(double value) {
            VoicechatClient.PLAYER_VOLUME_CONFIG.setVolume(playerUUID, value, String.format("Volume of %s", playerName));
            VoicechatClient.PLAYER_VOLUME_CONFIG.save();
        }

        @Override
        public double get() {
            return VoicechatClient.PLAYER_VOLUME_CONFIG.getVolume(playerUUID);
        }

        @Override
        public double getAudioLevel(TalkCache talkCache) {
            return AudioUtils.applyVolume(talkCache.getPlayerAudioLevel(playerUUID), get());
        }
    }

}
