package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.voice.common.Utils;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class MicActivator {

    private boolean activating;
    private int deactivationDelay;
    @Nullable
    private short[] lastBuff;

    public MicActivator() {

    }

    public boolean push(short[] audio, Consumer<short[]> audioConsumer) {
        boolean consumedAudio = false;
        boolean aboveThreshold = Utils.isAboveThreshold(audio, VoicechatClient.CLIENT_CONFIG.voiceActivationThreshold.get());
        if (activating) {
            if (!aboveThreshold) {
                if (deactivationDelay >= VoicechatClient.CLIENT_CONFIG.deactivationDelay.get()) {
                    stopActivating();
                } else {
                    audioConsumer.accept(audio);
                    consumedAudio = true;
                    deactivationDelay++;
                }
            } else {
                audioConsumer.accept(audio);
                consumedAudio = true;
            }
        } else {
            if (aboveThreshold) {
                if (lastBuff != null) {
                    audioConsumer.accept(lastBuff);
                }
                audioConsumer.accept(audio);
                consumedAudio = true;
                activating = true;
            }
        }
        lastBuff = consumedAudio ? null : audio;
        return consumedAudio;
    }

    public void stopActivating() {
        activating = false;
        deactivationDelay = 0;
        lastBuff = null;
    }

    public boolean isActivating() {
        return activating;
    }
}
