package de.maxhenkel.voicechat.gui.audiodevice;

import com.mojang.blaze3d.vertex.PoseStack;
import de.maxhenkel.voicechat.gui.widgets.MicTestButton;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class MicrophoneAudioDeviceEntry extends AudioDeviceEntry {

    private final MicTestButton testButton;

    public MicrophoneAudioDeviceEntry(String device, Component name, @Nullable ResourceLocation icon, Supplier<Boolean> isSelected, MicTestButton testButton) {
        super(device, name, icon, isSelected);
        this.testButton = testButton;
    }

    @Override
    public void render(PoseStack guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float delta) {
        super.render(guiGraphics, index, top, left, width, height, mouseX, mouseY, hovered, delta);
        boolean selected = isSelected.get();
        if (selected && (hovered || testButton.isMicActive())) {
            testButton.x = left + (width - testButton.getWidth() - PADDING);
            testButton.y = top + (height - testButton.getHeight()) / 2;
            testButton.render(guiGraphics, mouseX, mouseY, delta);
        }
    }

}
