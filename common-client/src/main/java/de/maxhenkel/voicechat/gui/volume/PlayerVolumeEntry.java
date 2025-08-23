package de.maxhenkel.voicechat.gui.volume;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.gui.GameProfileUtils;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.client.ClientVoicechat;
import de.maxhenkel.voicechat.voice.common.AudioUtils;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiComponent;

import javax.annotation.Nullable;
import java.util.UUID;

public class PlayerVolumeEntry extends VolumeEntry {

    @Nullable
    protected final PlayerState state;

    public PlayerVolumeEntry(@Nullable PlayerState state, AdjustVolumesScreen screen) {
        super(screen, new AdjustPlayerVolumeEntry(state != null ? state.getUuid() : Util.NIL_UUID, state != null ? state.getName() : null));
        this.state = state;
    }

    @Nullable
    public PlayerState getState() {
        return state;
    }

    @Override
    public void renderElement(PoseStack poseStack, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float delta, int skinX, int skinY, int textX, int textY) {
        if (state != null) {
            RenderSystem.setShaderTexture(0, GameProfileUtils.getSkin(state.getUuid()));
            GuiComponent.blit(poseStack, skinX, skinY, SKIN_SIZE, SKIN_SIZE, 8, 8, 8, 8, 64, 64);
            RenderSystem.enableBlend();
            GuiComponent.blit(poseStack, skinX, skinY, SKIN_SIZE, SKIN_SIZE, 40, 8, 8, 8, 64, 64);
            RenderSystem.disableBlend();
            minecraft.font.draw(poseStack, state.getName(), (float) textX, (float) textY, PLAYER_NAME_COLOR);
        } else {
            RenderSystem.setShaderTexture(0, OTHER_VOLUME_ICON);
            GuiComponent.blit(poseStack, skinX, skinY, SKIN_SIZE, SKIN_SIZE, 16, 16, 16, 16, 16, 16);
            minecraft.font.draw(poseStack, OTHER_VOLUME, (float) textX, (float) textY, PLAYER_NAME_COLOR);
            if (hovered) {
                screen.postRender(() -> {
                    screen.renderTooltip(poseStack, OTHER_VOLUME_DESCRIPTION, mouseX, mouseY);
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
