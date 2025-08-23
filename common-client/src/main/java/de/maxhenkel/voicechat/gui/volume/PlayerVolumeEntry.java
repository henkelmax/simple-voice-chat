package de.maxhenkel.voicechat.gui.volume;

import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.gui.GameProfileUtils;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.client.ClientVoicechat;
import de.maxhenkel.voicechat.voice.common.AudioUtils;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;

import javax.annotation.Nullable;
import java.util.UUID;

public class PlayerVolumeEntry extends VolumeEntry {

    @Nullable
    protected final PlayerState state;

    public PlayerVolumeEntry(@Nullable PlayerState state, AdjustVolumesScreen screen) {
        super(screen, new AdjustPlayerVolumeEntry(state != null ? state.getUuid() : new UUID(0L, 0L), state != null ? state.getName() : null));
        this.state = state;
    }

    @Nullable
    public PlayerState getState() {
        return state;
    }

    @Override
    public void renderElement(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks, int skinX, int skinY, int textX, int textY) {
        GlStateManager.color(1F, 1F, 1F, 1F);
        if (state != null) {
            minecraft.getTextureManager().bindTexture(GameProfileUtils.getSkin(state.getUuid()));
            Gui.drawScaledCustomSizeModalRect(skinX, skinY, 8F, 8F, 8, 8, SKIN_SIZE, SKIN_SIZE, 64F, 64F);
            GlStateManager.enableBlend();
            Gui.drawScaledCustomSizeModalRect(skinX, skinY, 40F, 8F, 8, 8, SKIN_SIZE, SKIN_SIZE, 64F, 64F);
            GlStateManager.disableBlend();
            minecraft.fontRenderer.drawString(state.getName(), textX, textY, PLAYER_NAME_COLOR);
        } else {
            minecraft.getTextureManager().bindTexture(OTHER_VOLUME_ICON);
            Gui.drawScaledCustomSizeModalRect(skinX, skinY, 16, 16, 16, 16, SKIN_SIZE, SKIN_SIZE, 16, 16);
            minecraft.fontRenderer.drawString(OTHER_VOLUME.getUnformattedComponentText(), textX, textY, PLAYER_NAME_COLOR);
            if (isSelected) {
                screen.postRender(() -> {
                    screen.drawHoveringText(OTHER_VOLUME_DESCRIPTION.getUnformattedComponentText(), mouseX, mouseY);
                });
            }
        }
    }

    public static class AdjustPlayerVolumeEntry implements AdjustVolumeSlider.AdjustVolumeEntry {

        private final UUID playerUUID;
        @Nullable
        private final String playerName;

        public AdjustPlayerVolumeEntry(UUID playerUUID, @Nullable String playerName) {
            this.playerUUID = playerUUID;
            this.playerName = playerName;
        }

        @Override
        public void save(double value) {
            VoicechatClient.PLAYER_VOLUME_CONFIG.setVolume(playerUUID, value, playerName == null ? "All other volumes" : String.format("Volume of %s", playerName));
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
