package de.maxhenkel.voicechat.gui.volume;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.voice.common.AudioUtils;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.UUID;

public class OtherVolumeEntry extends VolumeEntry {

    protected static final ITextComponent OTHER_VOLUME = new TextComponentTranslation("message.voicechat.other_volume");
    protected static final ITextComponent OTHER_VOLUME_DESCRIPTION = new TextComponentTranslation("message.voicechat.other_volume.description");
    protected static final ResourceLocation OTHER_VOLUME_ICON = new ResourceLocation(Voicechat.MODID, "textures/icons/other_volume.png");

    public OtherVolumeEntry(AdjustVolumesScreen screen) {
        super(screen, new AdjustOtherVolumeEntry(), false, true);
    }

    @Override
    public void renderElement(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks, int skinX, int skinY, int textX, int textY) {
        GlStateManager.color(1F, 1F, 1F, 1F);
        minecraft.getTextureManager().bindTexture(OTHER_VOLUME_ICON);
        Gui.drawScaledCustomSizeModalRect(skinX, skinY, 16, 16, 16, 16, SKIN_SIZE, SKIN_SIZE, 16, 16);
        minecraft.fontRenderer.drawString(OTHER_VOLUME.getUnformattedComponentText(), textX, textY, PLAYER_NAME_COLOR);
        if (isSelected) {
            screen.postRender(() -> {
                screen.drawHoveringText(OTHER_VOLUME_DESCRIPTION.getUnformattedComponentText(), mouseX, mouseY);
            });
        }
    }

    private static class AdjustOtherVolumeEntry implements AdjustVolumeSlider.AdjustVolumeEntry {

        @Override
        public void save(double value) {
            VoicechatClient.PLAYER_VOLUME_CONFIG.setVolume(new UUID(0L, 0L), value, "All other volumes");
            VoicechatClient.PLAYER_VOLUME_CONFIG.save();
        }

        @Override
        public double get() {
            return VoicechatClient.PLAYER_VOLUME_CONFIG.getVolume(new UUID(0L, 0L));
        }

        @Override
        public double getAudioLevel() {
            return AudioUtils.LOWEST_DB;
        }
    }

}
