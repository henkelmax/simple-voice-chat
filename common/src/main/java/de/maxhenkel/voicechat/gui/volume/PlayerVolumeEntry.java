package de.maxhenkel.voicechat.gui.volume;

import com.mojang.blaze3d.systems.RenderSystem;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.gui.GameProfileUtils;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

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
        if (state != null) {
            ResourceLocation skin = GameProfileUtils.getSkin(state.getUuid());
            guiGraphics.blit(skin, skinX, skinY, SKIN_SIZE, SKIN_SIZE, 8, 8, 8, 8, 64, 64);
            RenderSystem.enableBlend();
            guiGraphics.blit(skin, skinX, skinY, SKIN_SIZE, SKIN_SIZE, 40, 8, 8, 8, 64, 64);
            RenderSystem.disableBlend();
            guiGraphics.drawString(minecraft.font, state.getName(), textX, textY, PLAYER_NAME_COLOR, false);
        } else {
            guiGraphics.blit(OTHER_VOLUME_ICON, skinX, skinY, SKIN_SIZE, SKIN_SIZE, 16, 16, 16, 16, 16, 16);
            guiGraphics.drawString(minecraft.font, OTHER_VOLUME, textX, textY, PLAYER_NAME_COLOR, false);
            if (hovered) {
                screen.postRender(() -> {
                    guiGraphics.renderTooltip(minecraft.font, OTHER_VOLUME_DESCRIPTION, mouseX, mouseY);
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
