package de.maxhenkel.voicechat.gui.audiodevice;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.gui.tooltips.TestSpeakerSupplier;
import de.maxhenkel.voicechat.gui.widgets.ImageButton;
import de.maxhenkel.voicechat.voice.client.TestSoundPlayer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class SpeakerAudioDeviceEntry extends AudioDeviceEntry {

    public static final ResourceLocation SPEAKER_ICON = new ResourceLocation(Voicechat.MODID, "textures/icons/test_speaker.png");

    private ImageButton testButton;

    public SpeakerAudioDeviceEntry(String device, Component name, @Nullable ResourceLocation icon, Supplier<Boolean> isSelected) {
        super(device, name, icon, isSelected);

        testButton = new ImageButton(0, 0, SPEAKER_ICON, button -> {
            testButton.active = false;
            TestSoundPlayer.playTestSound(() -> {
                testButton.active = true;
            });
        }, new TestSpeakerSupplier());
        children.add(testButton);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float delta) {
        super.render(guiGraphics, index, top, left, width, height, mouseX, mouseY, hovered, delta);
        boolean selected = isSelected.get();
        if (selected && hovered) {
            testButton.visible = true;
            testButton.setPosition(left + (width - testButton.getWidth() - PADDING), top + (height - testButton.getHeight()) / 2);
            testButton.render(guiGraphics, mouseX, mouseY, delta);
        } else {
            testButton.visible = false;
        }
    }
}
