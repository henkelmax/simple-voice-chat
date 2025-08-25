package de.maxhenkel.voicechat.gui.audiodevice;

import com.mojang.blaze3d.matrix.MatrixStack;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.gui.tooltips.TestSpeakerSupplier;
import de.maxhenkel.voicechat.gui.widgets.ImageButton;
import de.maxhenkel.voicechat.voice.client.TestSoundPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class SpeakerAudioDeviceEntry extends AudioDeviceEntry {

    public static final ResourceLocation SPEAKER_ICON = new ResourceLocation(Voicechat.MODID, "textures/icons/test_speaker.png");

    private ImageButton testButton;

    public SpeakerAudioDeviceEntry(String device, ITextComponent name, @Nullable ResourceLocation icon, Supplier<Boolean> isSelected) {
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
    public void render(MatrixStack poseStack, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float delta) {
        super.render(poseStack, index, top, left, width, height, mouseX, mouseY, hovered, delta);
        boolean selected = isSelected.get();
        if (selected && hovered) {
            testButton.visible = true;
            testButton.x = left + (width - testButton.getWidth() - PADDING);
            testButton.y = top + (height - testButton.getHeight()) / 2;
            testButton.render(poseStack, mouseX, mouseY, delta);
        } else {
            testButton.visible = false;
        }
    }
}
