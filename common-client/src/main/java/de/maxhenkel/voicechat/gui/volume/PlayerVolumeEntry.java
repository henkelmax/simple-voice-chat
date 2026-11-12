package de.maxhenkel.voicechat.gui.volume;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.gui.GameProfileUtils;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.client.ClientVoicechat;
import de.maxhenkel.voicechat.voice.common.AudioUtils;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.client.gui.AbstractGui;

import java.util.UUID;

public class PlayerVolumeEntry extends VolumeEntry {

    protected final PlayerState state;

    public PlayerVolumeEntry(PlayerState state, AdjustVolumesScreen screen) {
        super(screen, new AdjustPlayerVolumeEntry(state.getUuid(), state.getName()), false, false);
        this.state = state;
    }

    @Override
    public void renderElement(MatrixStack poseStack, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float delta, int skinX, int skinY, int textX, int textY) {
        minecraft.getTextureManager().bind(GameProfileUtils.getSkin(state.getUuid()));
        AbstractGui.blit(poseStack, skinX, skinY, SKIN_SIZE, SKIN_SIZE, 8, 8, 8, 8, 64, 64);
        RenderSystem.enableBlend();
        AbstractGui.blit(poseStack, skinX, skinY, SKIN_SIZE, SKIN_SIZE, 40, 8, 8, 8, 64, 64);
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
        public double getAudioLevel() {
            ClientVoicechat client = ClientManager.getClient();
            if (client == null) {
                return AudioUtils.LOWEST_DB;
            }
            return client.getTalkCache().getPlayerAudioLevel(playerUUID);
        }
    }

}
