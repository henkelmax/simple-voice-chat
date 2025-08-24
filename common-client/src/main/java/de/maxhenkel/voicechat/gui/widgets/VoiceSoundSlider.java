package de.maxhenkel.voicechat.gui.widgets;

import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.client.ClientVoicechat;
import de.maxhenkel.voicechat.voice.client.SoundManager;
import net.minecraft.network.chat.Component;

public class VoiceSoundSlider extends DebouncedSlider {

    protected float maxVolume;

    public VoiceSoundSlider(int x, int y, int width, int height, float maxVolume) {
        super(x, y, width, height, Component.empty(), VoicechatClient.CLIENT_CONFIG.voiceChatVolume.get().floatValue() / maxVolume);
        this.maxVolume = maxVolume;
        updateMessage();
    }

    public VoiceSoundSlider(int x, int y, int width, int height) {
        this(x, y, width, height, getMaxGain());
    }

    private static float getMaxGain() {
        float maxConfigValue = VoicechatClient.CLIENT_CONFIG.voiceChatVolume.getMax().floatValue();
        ClientVoicechat client = ClientManager.getClient();
        if (client == null) {
            return maxConfigValue;
        }
        SoundManager soundManager = client.getSoundManager();
        if (soundManager == null) {
            return maxConfigValue;
        }
        return Math.min(maxConfigValue, soundManager.getMaxGain());
    }

    @Override
    protected void updateMessage() {
        setMessage(getMsg());
    }

    public Component getMsg() {
        return Component.translatable("message.voicechat.voice_chat_volume", Math.round(value * maxVolume * 100F) + "%");
    }

    @Override
    public void applyDebounced() {
        VoicechatClient.CLIENT_CONFIG.voiceChatVolume.set(value * maxVolume).save();
    }
}
