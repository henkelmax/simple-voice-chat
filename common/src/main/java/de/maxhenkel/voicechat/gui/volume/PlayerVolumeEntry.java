package de.maxhenkel.voicechat.gui.volume;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.gui.GameProfileUtils;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.util.ColorHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;
import java.util.UUID;

public class PlayerVolumeEntry extends VolumeEntry {

    @Nullable
    protected final PlayerState state;

    public PlayerVolumeEntry(@Nullable PlayerState state, PlayerVolumesScreen screen) {
        super(screen, new PlayerVolumeConfigEntry(state != null ? state.getUuid() : Util.NIL_UUID));
        this.state = state;
    }

    @Nullable
    public PlayerState getState() {
        return state;
    }

    @Override
    public void renderElement(MatrixStack poseStack, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float delta, int skinX, int skinY, int textX, int textY) {
        if (state != null) {
            minecraft.getTextureManager().bind(GameProfileUtils.getSkin(state.getUuid()));
            AbstractGui.blit(poseStack, skinX, skinY, SKIN_SIZE, SKIN_SIZE, 8, 8, 8, 8, 64, 64);
            RenderSystem.enableBlend();
            AbstractGui.blit(poseStack, skinX, skinY, SKIN_SIZE, SKIN_SIZE, 40, 8, 8, 8, 64, 64);
            RenderSystem.disableBlend();
            minecraft.font.draw(poseStack, state.getName(), (float) textX, (float) textY, PLAYER_NAME_COLOR);
        } else {
            minecraft.getTextureManager().bind(OTHER_VOLUME_ICON);
            AbstractGui.blit(poseStack, skinX, skinY, SKIN_SIZE, SKIN_SIZE, 16, 16, 16, 16, 16, 16);
            minecraft.font.draw(poseStack, OTHER_VOLUME, (float) textX, (float) textY, PLAYER_NAME_COLOR);
            if (hovered) {
                screen.postRender(() -> {
                    screen.renderTooltip(poseStack, OTHER_VOLUME_DESCRIPTION, mouseX, mouseY);
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
