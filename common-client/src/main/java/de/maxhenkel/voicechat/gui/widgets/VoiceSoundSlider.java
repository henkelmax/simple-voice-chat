package de.maxhenkel.voicechat.gui.widgets;

import de.maxhenkel.voicechat.VoicechatClient;
import net.minecraft.util.text.TextComponentTranslation;

public class VoiceSoundSlider extends DebouncedSlider {

    protected float maxVolume;

    public VoiceSoundSlider(int id, int x, int y, int width, int height, float maxVolume) {
        super(id, x, y, width, height, VoicechatClient.CLIENT_CONFIG.voiceChatVolume.get().floatValue() / maxVolume);
        this.maxVolume = maxVolume;
        updateMessage();
    }

    public VoiceSoundSlider(int id, int x, int y, int width, int height) {
        this(id, x, y, width, height, getMaxGain());
    }

    private static float getMaxGain() {
        /*float maxConfigValue = VoicechatClient.CLIENT_CONFIG.voiceChatVolume.getMax().floatValue();
        ClientVoicechat client = ClientManager.getClient();
        if (client == null) {
            return maxConfigValue;
        }
        SoundManager soundManager = client.getSoundManager();
        if (soundManager == null) {
            return maxConfigValue;
        }
        return Math.min(maxConfigValue, soundManager.getMaxGain());*/
        return VoicechatClient.CLIENT_CONFIG.voiceChatVolume.getMax().floatValue();
    }

    @Override
    protected void updateMessage() {
        displayString = getMsg();
    }

    public String getMsg() {
        return new TextComponentTranslation("message.voicechat.voice_chat_volume", Math.round(value * maxVolume * 100F) + "%").getUnformattedComponentText();
    }

    @Override
    public void applyDebounced() {
        VoicechatClient.CLIENT_CONFIG.voiceChatVolume.set(value * maxVolume).save();
    }
}
