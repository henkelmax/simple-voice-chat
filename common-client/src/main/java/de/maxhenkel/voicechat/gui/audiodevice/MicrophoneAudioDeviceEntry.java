package de.maxhenkel.voicechat.gui.audiodevice;

import de.maxhenkel.voicechat.gui.widgets.MicTestButton;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class MicrophoneAudioDeviceEntry extends AudioDeviceEntry {

    private final MicTestButton testButton;

    public MicrophoneAudioDeviceEntry(String device, Component name, @Nullable Identifier icon, Supplier<Boolean> isSelected, MicTestButton testButton) {
        super(device, name, icon, isSelected);
        this.testButton = testButton;
    }

    @Override
    public void extractContent(GuiGraphicsExtractor guiGraphics, boolean hovered) {
        super.extractContent(guiGraphics, hovered);
        boolean selected = isSelected.get();
        if (selected && (hovered || testButton.isMicActive())) {
            testButton.setPosition(getContentX() + (getContentWidth() - testButton.getWidth() - PADDING), getContentY() + (getContentHeight() - testButton.getHeight()) / 2);
            testButton.extractRenderState(guiGraphics);
        }
    }

}
