package de.maxhenkel.voicechat.gui.volume;

import com.mojang.blaze3d.vertex.PoseStack;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.gui.widgets.ListScreenEntryBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;

public abstract class VolumeEntry extends ListScreenEntryBase<VolumeEntry> {

    protected static final Component OTHER_VOLUME = Component.translatable("message.voicechat.other_volume");
    protected static final Component OTHER_VOLUME_DESCRIPTION = Component.translatable("message.voicechat.other_volume.description");
    protected static final ResourceLocation OTHER_VOLUME_ICON = new ResourceLocation(Voicechat.MODID, "textures/icons/other_volume.png");

    protected static final int SKIN_SIZE = 24;
    protected static final int PADDING = 4;
    protected static final int SLIDER_WIDTH = 90;
    protected static final int ARROW_SIZE = 6;
    protected static final int ARROW_SPACING = 1;
    protected static final int ARROW_COLUMN_WIDTH = ARROW_SPACING + ARROW_SIZE + ARROW_SPACING;
    protected static final int BG_FILL = FastColor.ARGB32.color(255, 74, 74, 74);
    protected static final int PLAYER_NAME_COLOR = FastColor.ARGB32.color(255, 255, 255, 255);

    protected final Minecraft minecraft;
    protected final AdjustVolumesScreen screen;
    protected final AdjustVolumeSlider volumeSlider;
    protected final boolean indented;
    protected final boolean offset;

    public VolumeEntry(AdjustVolumesScreen screen, AdjustVolumeSlider.AdjustVolumeEntry entry, boolean indented, boolean offset) {
        this.minecraft = Minecraft.getInstance();
        this.screen = screen;
        this.indented = indented;
        this.offset = offset;
        this.volumeSlider = new AdjustVolumeSlider(0, 0, SLIDER_WIDTH, 20, entry);
        this.children.add(volumeSlider);
    }

    @Override
    public void render(PoseStack poseStack, int index, int top, int rowLeft, int rowWidth, int height, int mouseX, int mouseY, boolean hovered, float delta) {
        int indent = indented ? ARROW_COLUMN_WIDTH - PADDING : 0;
        int left = rowLeft + indent;
        int width = rowWidth - indent;
        int skinX = left + (offset ? ARROW_COLUMN_WIDTH : PADDING);
        int skinY = top + (height - SKIN_SIZE) / 2;
        int textX = skinX + SKIN_SIZE + PADDING;
        int textY = top + (height - minecraft.font.lineHeight) / 2;

        GuiComponent.fill(poseStack, left, top, left + width, top + height, BG_FILL);

        volumeSlider.x = left + width - volumeSlider.getWidth() - PADDING;
        volumeSlider.y = top + (height - volumeSlider.getHeight()) / 2;

        renderElement(poseStack, index, top, left, width, height, mouseX, mouseY, hovered, delta, skinX, skinY, textX, textY);

        volumeSlider.render(poseStack, mouseX, mouseY, delta);
    }

    public abstract void renderElement(PoseStack poseStack, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float delta, int skinX, int skinY, int textX, int textY);

}
