package de.maxhenkel.voicechat.gui.volume;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.voice.common.AudioUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.Util;

public class OtherVolumeEntry extends VolumeEntry {

    protected static final Component OTHER_VOLUME = Component.translatable("message.voicechat.other_volume");
    protected static final Component OTHER_VOLUME_DESCRIPTION = Component.translatable("message.voicechat.other_volume.description");
    protected static final ResourceLocation OTHER_VOLUME_ICON = ResourceLocation.fromNamespaceAndPath(Voicechat.MODID, "icons/other_volume");

    public OtherVolumeEntry(AdjustVolumesScreen screen) {
        super(screen, new AdjustOtherVolumeEntry(), false, true);
    }

    @Override
    public void renderElement(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float delta, int skinX, int skinY, int textX, int textY) {
        guiGraphics.blitSprite(RenderType::guiTextured, OTHER_VOLUME_ICON, SKIN_SIZE, SKIN_SIZE, 0, 0, skinX, skinY, SKIN_SIZE, SKIN_SIZE);
        renderScrollingString(guiGraphics, OTHER_VOLUME, textX, textY, PLAYER_NAME_COLOR);
        if (hovered) {
            screen.postRender(() -> {
                guiGraphics.renderTooltip(minecraft.font, OTHER_VOLUME_DESCRIPTION, mouseX, mouseY);
            });
        }
    }

    private static class AdjustOtherVolumeEntry implements AdjustVolumeSlider.AdjustVolumeEntry {

        @Override
        public void save(double value) {
            VoicechatClient.PLAYER_VOLUME_CONFIG.setVolume(Util.NIL_UUID, value, "All other volumes");
            VoicechatClient.PLAYER_VOLUME_CONFIG.save();
        }

        @Override
        public double get() {
            return VoicechatClient.PLAYER_VOLUME_CONFIG.getVolume(Util.NIL_UUID);
        }

        @Override
        public double getAudioLevel() {
            return AudioUtils.LOWEST_DB;
        }
    }

}
