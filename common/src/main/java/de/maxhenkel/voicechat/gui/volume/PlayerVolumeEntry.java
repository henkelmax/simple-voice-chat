package de.maxhenkel.voicechat.gui.volume;

import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.gui.GameProfileUtils;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;

import javax.annotation.Nullable;
import java.util.UUID;

public class PlayerVolumeEntry extends VolumeEntry {

    @Nullable
    protected final PlayerState state;

    public PlayerVolumeEntry(@Nullable PlayerState state, AdjustVolumesScreen screen) {
        super(screen, new PlayerVolumeConfigEntry(state != null ? state.getUuid() : new UUID(0L, 0L)));
        this.state = state;
    }

    @Nullable
    public PlayerState getState() {
        return state;
    }

    @Override
    public void renderElement(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks, int skinX, int skinY, int textX, int textY) {
        if (state != null) {
            minecraft.getTextureManager().bindTexture(GameProfileUtils.getSkin(state.getUuid()));
            GuiScreen.drawScaledCustomSizeModalRect(skinX, skinY, SKIN_SIZE, SKIN_SIZE, 8, 8, 8, 8, 64, 64);
            GlStateManager.enableBlend();
            GuiScreen.drawScaledCustomSizeModalRect(skinX, skinY, SKIN_SIZE, SKIN_SIZE, 40, 8, 8, 8, 64, 64);
            GlStateManager.disableBlend();
            minecraft.fontRenderer.drawString(state.getName(), textX, textY, PLAYER_NAME_COLOR);
        } else {
            minecraft.getTextureManager().bindTexture(OTHER_VOLUME_ICON);
            GuiScreen.drawScaledCustomSizeModalRect(skinX, skinY, SKIN_SIZE, SKIN_SIZE, 16, 16, 16, 16, 16, 16);
            minecraft.fontRenderer.drawString(OTHER_VOLUME.getUnformattedComponentText(), textX, textY, PLAYER_NAME_COLOR);
            if (isSelected) {
                screen.postRender(() -> {
                    screen.drawHoveringText(OTHER_VOLUME_DESCRIPTION.getUnformattedComponentText(), mouseX, mouseY);
                });
            }
        }
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
