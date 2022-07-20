package de.maxhenkel.voicechat.gui.audiodevice;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.client.ClientVoicechat;
import de.maxhenkel.voicechat.voice.client.SoundManager;
import de.maxhenkel.voicechat.voice.client.microphone.MicrophoneManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.List;

public class SelectMicrophoneScreen extends SelectDeviceScreen {

    protected static final ResourceLocation MICROPHONE_ICON = new ResourceLocation(Voicechat.MODID, "textures/icons/microphone.png");
    protected static final Component TITLE = Component.translatable("gui.voicechat.select_microphone.title");
    protected static final Component NO_MICROPHONE = Component.translatable("message.voicechat.no_microphone").withStyle(ChatFormatting.GRAY);

    public SelectMicrophoneScreen(@Nullable Screen parent) {
        super(TITLE, parent);
    }

    @Override
    public List<String> getDevices() {
        return MicrophoneManager.deviceNames();
    }

    @Override
    public String getSelectedDevice() {
        return VoicechatClient.CLIENT_CONFIG.microphone.get();
    }

    @Override
    public ResourceLocation getIcon(String device) {
        return MICROPHONE_ICON;
    }

    @Override
    public Component getEmptyListComponent() {
        return NO_MICROPHONE;
    }

    @Override
    public String getVisibleName(String device) {
        return SoundManager.cleanDeviceName(device);
    }

    @Override
    public void onSelect(String device) {
        VoicechatClient.CLIENT_CONFIG.microphone.set(device).save();
        ClientVoicechat client = ClientManager.getClient();
        if (client != null) {
            client.reloadAudio();
        }
    }
}
