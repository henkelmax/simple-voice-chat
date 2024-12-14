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
    protected static final int BG_FILL = FastColor.ARGB32.color(255, 74, 74, 74);
    protected static final int PLAYER_NAME_COLOR = FastColor.ARGB32.color(255, 255, 255, 255);

    protected final Minecraft minecraft;
    protected final AdjustVolumesScreen screen;
    protected final AdjustVolumeSlider volumeSlider;

    public VolumeEntry(AdjustVolumesScreen screen, AdjustVolumeSlider.VolumeConfigEntry entry) {
        this.minecraft = Minecraft.getInstance();
        this.screen = screen;
        this.volumeSlider = new AdjustVolumeSlider(0, 0, 100, 20, entry);
        this.children.add(volumeSlider);
    }

    @Override
    public void render(PoseStack poseStack, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float delta) {
        int skinX = left + PADDING;
        int skinY = top + (height - SKIN_SIZE) / 2;
        int textX = skinX + SKIN_SIZE + PADDING;
        int textY = top + (height - minecraft.font.lineHeight) / 2;

        GuiComponent.fill(poseStack, left, top, left + width, top + height, BG_FILL);

        renderElement(poseStack, index, top, left, width, height, mouseX, mouseY, hovered, delta, skinX, skinY, textX, textY);

        volumeSlider.x = left + (width - volumeSlider.getWidth() - PADDING);
        volumeSlider.y = top + (height - volumeSlider.getHeight()) / 2;
        volumeSlider.render(poseStack, mouseX, mouseY, delta);
    }

    public abstract void renderElement(PoseStack poseStack, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float delta, int skinX, int skinY, int textX, int textY);

}
