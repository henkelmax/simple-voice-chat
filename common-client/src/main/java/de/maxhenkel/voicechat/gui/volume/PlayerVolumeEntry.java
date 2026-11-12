package de.maxhenkel.voicechat.gui.volume;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.gui.GameProfileUtils;
import de.maxhenkel.voicechat.voice.client.TalkCache;
import de.maxhenkel.voicechat.voice.common.AudioUtils;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.client.gui.GuiComponent;

import java.util.UUID;

public class PlayerVolumeEntry extends VolumeEntry {

    protected final PlayerState state;

    public PlayerVolumeEntry(PlayerState state, AdjustVolumesScreen screen) {
        super(screen, new AdjustPlayerVolumeEntry(state.getUuid(), state.getName()), false, false);
        this.state = state;
    }

    @Override
    public void renderElement(PoseStack poseStack, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float delta, int skinX, int skinY, int textX, int textY) {
        RenderSystem.setShaderTexture(0, GameProfileUtils.getSkin(state.getUuid()));
        GuiComponent.blit(poseStack, skinX, skinY, SKIN_SIZE, SKIN_SIZE, 8, 8, 8, 8, 64, 64);
        RenderSystem.enableBlend();
        GuiComponent.blit(poseStack, skinX, skinY, SKIN_SIZE, SKIN_SIZE, 40, 8, 8, 8, 64, 64);
        RenderSystem.disableBlend();
        minecraft.font.draw(poseStack, state.getName(), (float) textX, (float) textY, PLAYER_NAME_COLOR);
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
