package de.maxhenkel.voicechat.gui.audiodevice;

import com.mojang.blaze3d.matrix.MatrixStack;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.gui.widgets.ListScreenEntryBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.util.ColorHelper;
import net.minecraft.util.ResourceLocation;

public class AudioDeviceEntry extends ListScreenEntryBase<AudioDeviceEntry> {

    protected static final ResourceLocation SELECTED = new ResourceLocation(Voicechat.MODID, "textures/icons/device_selected.png");

    protected static final int PADDING = 4;
    protected static final int BG_FILL = ColorHelper.PackedColor.color(255, 74, 74, 74);
    protected static final int BG_FILL_HOVERED = ColorHelper.PackedColor.color(255, 90, 90, 90);
    protected static final int BG_FILL_SELECTED = ColorHelper.PackedColor.color(255, 40, 40, 40);
    protected static final int DEVICE_NAME_COLOR = ColorHelper.PackedColor.color(255, 255, 255, 255);

    protected final Minecraft minecraft;
    protected final String device;
    protected final String visibleDeviceName;
    protected final SelectDeviceScreen parent;

    public AudioDeviceEntry(SelectDeviceScreen parent, String device) {
        this.parent = parent;
        this.device = device;
        this.visibleDeviceName = parent.getVisibleName(device);
        this.minecraft = Minecraft.getInstance();
    }

    @Override
    public void render(MatrixStack poseStack, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float delta) {
        boolean selected = parent.getSelectedDevice().equals(device);
        if (selected) {
            AbstractGui.fill(poseStack, left, top, left + width, top + height, BG_FILL_SELECTED);
        } else if (hovered) {
            AbstractGui.fill(poseStack, left, top, left + width, top + height, BG_FILL_HOVERED);
        } else {
            AbstractGui.fill(poseStack, left, top, left + width, top + height, BG_FILL);
        }

        minecraft.getTextureManager().bind(parent.getIcon(device));
        AbstractGui.blit(poseStack, left + PADDING, top + height / 2 - 8, 16, 16, 16, 16, 16, 16);
        if (selected) {
            minecraft.getTextureManager().bind(SELECTED);
            AbstractGui.blit(poseStack, left + PADDING, top + height / 2 - 8, 16, 16, 16, 16, 16, 16);
        }

        float deviceWidth = minecraft.font.width(visibleDeviceName);
        float space = width - PADDING - 16 - PADDING - PADDING;
        float scale = Math.min(space / deviceWidth, 1F);

        poseStack.pushPose();
        poseStack.translate(left + PADDING + 16 + PADDING, top + height / 2 - (minecraft.font.lineHeight * scale) / 2, 0D);
        poseStack.scale(scale, scale, 1F);

        minecraft.font.draw(poseStack, visibleDeviceName, 0, 0, DEVICE_NAME_COLOR);
        poseStack.popPose();
    }

    public String getDevice() {
        return device;
    }
}
