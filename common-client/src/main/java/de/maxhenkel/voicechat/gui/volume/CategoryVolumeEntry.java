package de.maxhenkel.voicechat.gui.volume;

import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.plugins.impl.VolumeCategoryImpl;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.client.ClientVoicechat;
import de.maxhenkel.voicechat.voice.common.AudioUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public class CategoryVolumeEntry extends VolumeEntry {

    protected final VolumeCategoryImpl category;
    protected final Identifier texture;

    public CategoryVolumeEntry(VolumeCategoryImpl category, AdjustVolumesScreen screen) {
        super(screen, new AdjustCategoryVolumeEntry(category.getId()));
        this.category = category;
        this.texture = ClientManager.getCategoryManager().getTexture(category.getId(), OTHER_VOLUME_ICON);
    }

    public VolumeCategoryImpl getCategory() {
        return category;
    }

    @Override
    public void renderElement(GuiGraphics guiGraphics, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float delta, int skinX, int skinY, int textX, int textY) {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, texture, skinX, skinY, 16, 16, SKIN_SIZE, SKIN_SIZE, 16, 16, 16, 16);
        renderScrollingString(guiGraphics, category.getDisplayName());
        if (hovered && category.getDescription() != null) {
            screen.postRender(() -> {
                guiGraphics.setTooltipForNextFrame(minecraft.font, category.getDisplayDescription(), mouseX, mouseY);
            });
        }
    }

    private static class AdjustCategoryVolumeEntry implements AdjustVolumeSlider.AdjustVolumeEntry {

        private final String category;

        public AdjustCategoryVolumeEntry(String category) {
            this.category = category;
        }

        @Override
        public void save(double value) {
            VoicechatClient.CATEGORY_VOLUME_CONFIG.setVolume(category, value);
            VoicechatClient.CATEGORY_VOLUME_CONFIG.save();
        }

        @Override
        public double get() {
            return VoicechatClient.CATEGORY_VOLUME_CONFIG.getVolume(category);
        }

        @Override
        public double getAudioLevel() {
            ClientVoicechat client = ClientManager.getClient();
            if (client == null) {
                return AudioUtils.LOWEST_DB;
            }
            return client.getTalkCache().getCategoryAudioLevel(category);
        }
    }

}
