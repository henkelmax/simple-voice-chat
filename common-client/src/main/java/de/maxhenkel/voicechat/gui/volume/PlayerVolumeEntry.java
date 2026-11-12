package de.maxhenkel.voicechat.gui.volume;

import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.gui.GameProfileUtils;
import de.maxhenkel.voicechat.voice.client.TalkCache;
import de.maxhenkel.voicechat.voice.common.AudioUtils;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.PlayerSkin;

import java.util.UUID;

public class PlayerVolumeEntry extends VolumeEntry {

    protected final PlayerState state;

    public PlayerVolumeEntry(PlayerState state, AdjustVolumesScreen screen) {
        super(screen, new AdjustPlayerVolumeEntry(state.getUuid(), state.getName()), false, false);
        this.state = state;
    }

    @Override
    public void renderElement(GuiGraphicsExtractor guiGraphics, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float delta, int skinX, int skinY, int textX, int textY) {
        PlayerSkin skin = GameProfileUtils.getSkin(state.getUuid());
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, skin.body().texturePath(), skinX, skinY, 8, 8, SKIN_SIZE, SKIN_SIZE, 8, 8, 64, 64);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, skin.body().texturePath(), skinX, skinY, 40, 8, SKIN_SIZE, SKIN_SIZE, 8, 8, 64, 64);
        renderScrollingString(guiGraphics, Component.literal(state.getName()), textX, textY);
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
