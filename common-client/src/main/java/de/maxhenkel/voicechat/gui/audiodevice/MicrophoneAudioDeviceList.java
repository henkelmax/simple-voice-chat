package de.maxhenkel.voicechat.gui.audiodevice;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.gui.widgets.MicTestButton;
import de.maxhenkel.voicechat.voice.client.microphone.MicrophoneManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

public class MicrophoneAudioDeviceList extends AudioDeviceList {

    public static final Identifier MICROPHONE_ICON = Identifier.fromNamespaceAndPath(Voicechat.MODID, "textures/icons/microphone.png");
    public static final Component DEFAULT_MICROPHONE = Component.translatable("message.voicechat.default_microphone");

    private final MicTestButton micTestButton;

    public MicrophoneAudioDeviceList(Screen screen, int width, int height, int top) {
        super(width, height, top);
        defaultDeviceText = DEFAULT_MICROPHONE;
        icon = MICROPHONE_ICON;
        configEntry = VoicechatClient.CLIENT_CONFIG.microphone;

        micTestButton = new MicTestButton(0, 0, true);
        ((List<GuiEventListener>) screen.children()).add(micTestButton);

        setAudioDevices(MicrophoneManager.deviceNames());
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
        super.renderWidget(guiGraphics, i, j, f);
        micTestButton.updateLastRender();
    }

    @Override
    public AudioDeviceEntry createAudioDeviceEntry(String device, Component name, @Nullable Identifier icon, Supplier<Boolean> isSelected) {
        return new MicrophoneAudioDeviceEntry(device, name, icon, isSelected, micTestButton);
    }

}
