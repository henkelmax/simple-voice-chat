package de.maxhenkel.voicechat.gui.audiodevice;

import de.maxhenkel.voicechat.gui.widgets.MicTestButton;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class MicrophoneAudioDeviceEntry extends AudioDeviceEntry {

    private final MicTestButton testButton;

    public MicrophoneAudioDeviceEntry(String device, ITextComponent name, @Nullable ResourceLocation icon, Supplier<Boolean> isSelected, MicTestButton testButton) {
        super(device, name, icon, isSelected);
        this.testButton = testButton;
    }

    @Override
    public void drawEntry(int slotIndex, int left, int top, int width, int height, int mouseX, int mouseY, boolean hovered, float partialTicks) {
        super.drawEntry(slotIndex, left, top, width, height, mouseX, mouseY, hovered, partialTicks);
        boolean selected = isSelected.get();
        if (selected && (hovered || testButton.isMicActive())) {
            testButton.x = left + (width - testButton.width - PADDING);
            testButton.y = top + (height - testButton.height) / 2;
            testButton.drawButton(Minecraft.getMinecraft(), mouseX, mouseY, partialTicks);
        }
    }

    @Override
    public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY) {
        if (testButton.mousePressed(minecraft, mouseX, mouseY)) {
            testButton.onPress();
            return true;
        }
        return super.mousePressed(slotIndex, mouseX, mouseY, mouseEvent, relativeX, relativeY);
    }

}
