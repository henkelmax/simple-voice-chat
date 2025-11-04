package de.maxhenkel.voicechat.gui.audiodevice;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.gui.tooltips.TestSpeakerSupplier;
import de.maxhenkel.voicechat.gui.widgets.ImageButton;
import de.maxhenkel.voicechat.voice.client.TestSoundPlayer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class SpeakerAudioDeviceEntry extends AudioDeviceEntry {

    public static final Identifier SPEAKER_ICON = Identifier.fromNamespaceAndPath(Voicechat.MODID, "textures/icons/test_speaker.png");

    private ImageButton testButton;

    public SpeakerAudioDeviceEntry(String device, Component name, @Nullable Identifier icon, Supplier<Boolean> isSelected) {
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
    public void renderContent(GuiGraphics guiGraphics, int mouseX, int mouseY, boolean hovered, float delta) {
        super.renderContent(guiGraphics, mouseX, mouseY, hovered, delta);
        boolean selected = isSelected.get();
        if (selected && hovered) {
            testButton.visible = true;
            testButton.setPosition(getContentX() + (getContentWidth() - testButton.getWidth() - PADDING), getContentY() + (getContentHeight() - testButton.getHeight()) / 2);
            testButton.render(guiGraphics, mouseX, mouseY, delta);
        } else {
            testButton.visible = false;
        }
    }

}
