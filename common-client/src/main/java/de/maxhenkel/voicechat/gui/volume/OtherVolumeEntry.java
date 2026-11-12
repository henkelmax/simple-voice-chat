package de.maxhenkel.voicechat.gui.volume;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.config.CategoryVolumeConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class OtherVolumeEntry extends VolumeEntry {

    protected static final Component OTHER_VOLUME = Component.translatable("message.voicechat.other_volume");
    protected static final Component OTHER_VOLUME_DESCRIPTION = Component.translatable("message.voicechat.other_volume.description");
    protected static final ResourceLocation OTHER_VOLUME_ICON = new ResourceLocation(Voicechat.MODID, "textures/icons/other_volume.png");

    public OtherVolumeEntry(AdjustVolumesScreen screen) {
        super(screen, new CategoryVolumeEntry.AdjustCategoryVolumeEntry(CategoryVolumeConfig.OTHER_CATEGORY), false, true);
    }

    @Override
    public void renderElement(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float delta, int skinX, int skinY, int textX, int textY) {
        guiGraphics.blit(OTHER_VOLUME_ICON, skinX, skinY, SKIN_SIZE, SKIN_SIZE, 16, 16, 16, 16, 16, 16);
        renderScrollingString(guiGraphics, OTHER_VOLUME, textX, textY, PLAYER_NAME_COLOR);
        if (hovered) {
            screen.postRender(() -> {
                guiGraphics.renderTooltip(minecraft.font, OTHER_VOLUME_DESCRIPTION, mouseX, mouseY);
            });
        }
    }

}
