package de.maxhenkel.voicechat.gui.volume;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.voice.common.AudioUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

public class OtherVolumeEntry extends VolumeEntry {

    protected static final Component OTHER_VOLUME = Component.translatable("message.voicechat.other_volume");
    protected static final Component OTHER_VOLUME_DESCRIPTION = Component.translatable("message.voicechat.other_volume.description");
    protected static final Identifier OTHER_VOLUME_ICON = Identifier.fromNamespaceAndPath(Voicechat.MODID, "icons/other_volume");

    public OtherVolumeEntry(AdjustVolumesScreen screen) {
        super(screen, new AdjustOtherVolumeEntry(), false, true);
    }

    @Override
    public void renderElement(GuiGraphicsExtractor guiGraphics, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float delta, int skinX, int skinY, int textX, int textY) {
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, OTHER_VOLUME_ICON, SKIN_SIZE, SKIN_SIZE, 0, 0, skinX, skinY, SKIN_SIZE, SKIN_SIZE);
        renderScrollingString(guiGraphics, OTHER_VOLUME, textX, textY);
        if (hovered) {
            guiGraphics.setTooltipForNextFrame(minecraft.font, OTHER_VOLUME_DESCRIPTION, mouseX, mouseY);
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
