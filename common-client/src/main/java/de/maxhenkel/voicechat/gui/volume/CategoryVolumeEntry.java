package de.maxhenkel.voicechat.gui.volume;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.plugins.impl.VolumeCategoryImpl;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.client.TalkCache;
import de.maxhenkel.voicechat.voice.common.AudioUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

import java.util.List;

public class CategoryVolumeEntry extends VolumeEntry {

    protected static final Identifier OTHER_VOLUME_ICON_PATH = Identifier.fromNamespaceAndPath(Voicechat.MODID, "textures/icons/other_volume.png");

    protected final VolumeCategoryImpl category;
    protected final Identifier texture;

    public CategoryVolumeEntry(VolumeCategoryImpl category, List<VolumeCategoryImpl> children, AdjustVolumesScreen screen, boolean indented, boolean offset) {
        super(screen, new AdjustCategoryVolumeEntry(category.getId(), children), indented, offset);
        this.category = category;
        this.texture = ClientManager.getCategoryManager().getTexture(category.getId(), OTHER_VOLUME_ICON_PATH);
    }

    @Override
    public void renderElement(GuiGraphicsExtractor guiGraphics, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float delta, int skinX, int skinY, int textX, int textY) {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, texture, skinX, skinY, 16, 16, SKIN_SIZE, SKIN_SIZE, 16, 16, 16, 16);
        renderScrollingString(guiGraphics, category.getDisplayName(), textX, textY);
        if (hovered && category.getDescription() != null) {
            guiGraphics.setTooltipForNextFrame(minecraft.font, category.getDisplayDescription(), mouseX, mouseY);
        }
    }

    public static class AdjustCategoryVolumeEntry implements AdjustVolumeSlider.AdjustVolumeEntry {

        private final String category;
        private final List<VolumeCategoryImpl> children;

        public AdjustCategoryVolumeEntry(String category) {
            this(category, List.of());
        }

        public AdjustCategoryVolumeEntry(String category, List<VolumeCategoryImpl> children) {
            this.category = category;
            this.children = children;
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
        public double getAudioLevel(TalkCache talkCache) {
            double audioLevel = getHeardAudioLevel(talkCache, category);
            for (VolumeCategoryImpl child : children) {
                audioLevel = Math.max(audioLevel, getHeardAudioLevel(talkCache, child.getId()));
            }
            return audioLevel;
        }

        private static double getHeardAudioLevel(TalkCache talkCache, String category) {
            return AudioUtils.applyVolume(talkCache.getCategoryAudioLevel(category), ClientManager.getCategoryManager().getEffectiveVolume(category));
        }
    }

}
