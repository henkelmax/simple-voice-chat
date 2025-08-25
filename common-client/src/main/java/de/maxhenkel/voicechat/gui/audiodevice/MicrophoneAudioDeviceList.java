package de.maxhenkel.voicechat.gui.audiodevice;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.gui.ScreenBase;
import de.maxhenkel.voicechat.gui.widgets.MicTestButton;
import de.maxhenkel.voicechat.voice.client.microphone.MicrophoneManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class MicrophoneAudioDeviceList extends AudioDeviceList {

    public static final ResourceLocation MICROPHONE_ICON = new ResourceLocation(Voicechat.MODID, "textures/icons/microphone.png");
    public static final ITextComponent DEFAULT_MICROPHONE = new TextComponentTranslation("message.voicechat.default_microphone");

    private final MicTestButton micTestButton;

    public MicrophoneAudioDeviceList(ScreenBase screen, int width, int height, int top) {
        super(width, height, top);
        defaultDeviceText = DEFAULT_MICROPHONE;
        icon = MICROPHONE_ICON;
        configEntry = VoicechatClient.CLIENT_CONFIG.microphone;

        micTestButton = new MicTestButton(10000, 0, 0, true);

        setAudioDevices(MicrophoneManager.deviceNames());
    }

    @Override
    public void drawScreen(int mouseXIn, int mouseYIn, float partialTicks) {
        super.drawScreen(mouseXIn, mouseYIn, partialTicks);
        micTestButton.updateLastRender();
    }

    @Override
    public AudioDeviceEntry createAudioDeviceEntry(String device, ITextComponent name, @Nullable ResourceLocation icon, Supplier<Boolean> isSelected) {
        return new MicrophoneAudioDeviceEntry(device, name, icon, isSelected, micTestButton);
    }

}
