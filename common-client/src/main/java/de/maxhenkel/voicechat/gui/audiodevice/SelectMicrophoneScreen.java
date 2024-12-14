package de.maxhenkel.voicechat.gui.audiodevice;

import de.maxhenkel.configbuilder.entry.ConfigEntry;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.voice.client.microphone.MicrophoneManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.List;

public class SelectMicrophoneScreen extends SelectDeviceScreen {

    public static final ResourceLocation MICROPHONE_ICON = ResourceLocation.fromNamespaceAndPath(Voicechat.MODID, "textures/icons/microphone.png");
    public static final Component TITLE = Component.translatable("gui.voicechat.select_microphone.title");
    public static final Component NO_MICROPHONE = Component.translatable("message.voicechat.no_microphone").withStyle(ChatFormatting.GRAY);

    public SelectMicrophoneScreen(@Nullable Screen parent) {
        super(TITLE, parent);
    }

    @Override
    public List<String> getDevices() {
        return MicrophoneManager.deviceNames();
    }

    @Override
    public ResourceLocation getIcon() {
        return MICROPHONE_ICON;
    }

    @Override
    public Component getEmptyListComponent() {
        return NO_MICROPHONE;
    }

    @Override
    public ConfigEntry<String> getConfigEntry() {
        return VoicechatClient.CLIENT_CONFIG.microphone;
    }
}
