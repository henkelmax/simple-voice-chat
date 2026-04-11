package de.maxhenkel.voicechat.gui.volume;

import com.mojang.blaze3d.matrix.MatrixStack;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.plugins.impl.VolumeCategoryImpl;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.client.ClientVoicechat;
import de.maxhenkel.voicechat.voice.common.AudioUtils;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.util.ResourceLocation;

public class CategoryVolumeEntry extends VolumeEntry {

    protected static final ResourceLocation OTHER_VOLUME_ICON_PATH = new ResourceLocation(Voicechat.MODID, "textures/icons/other_volume.png");

    protected final VolumeCategoryImpl category;
    protected final ResourceLocation texture;

    public CategoryVolumeEntry(VolumeCategoryImpl category, AdjustVolumesScreen screen) {
        super(screen, new AdjustCategoryVolumeEntry(category.getId()));
        this.category = category;
        this.texture = ClientManager.getCategoryManager().getTexture(category.getId(), OTHER_VOLUME_ICON_PATH);
    }

    public VolumeCategoryImpl getCategory() {
        return category;
    }

    @Override
    public void renderElement(MatrixStack matrixStack, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float delta, int skinX, int skinY, int textX, int textY) {
        minecraft.getTextureManager().bind(texture);
        AbstractGui.blit(matrixStack, skinX, skinY, SKIN_SIZE, SKIN_SIZE, 16, 16, 16, 16, 16, 16);
        minecraft.font.draw(matrixStack, category.getDisplayName(), (float) textX, (float) textY, PLAYER_NAME_COLOR);
        if (hovered && category.getDescription() != null) {
            screen.postRender(() -> {
                screen.renderTooltip(matrixStack, category.getDisplayDescription(), mouseX, mouseY);
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
