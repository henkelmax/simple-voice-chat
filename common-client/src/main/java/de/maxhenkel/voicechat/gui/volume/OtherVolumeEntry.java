package de.maxhenkel.voicechat.gui.volume;

import com.mojang.blaze3d.matrix.MatrixStack;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.config.CategoryVolumeConfig;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class OtherVolumeEntry extends VolumeEntry {

    protected static final ITextComponent OTHER_VOLUME = new TranslationTextComponent("message.voicechat.other_volume");
    protected static final ITextComponent OTHER_VOLUME_DESCRIPTION = new TranslationTextComponent("message.voicechat.other_volume.description");
    protected static final ResourceLocation OTHER_VOLUME_ICON = new ResourceLocation(Voicechat.MODID, "textures/icons/other_volume.png");

    public OtherVolumeEntry(AdjustVolumesScreen screen) {
        super(screen, new CategoryVolumeEntry.AdjustCategoryVolumeEntry(CategoryVolumeConfig.OTHER_CATEGORY), false, true);
    }

    @Override
    public void renderElement(MatrixStack poseStack, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float delta, int skinX, int skinY, int textX, int textY) {
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
