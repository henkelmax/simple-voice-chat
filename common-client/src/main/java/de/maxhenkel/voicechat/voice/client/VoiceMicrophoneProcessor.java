package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.voice.common.AudioUtils;

public class VoiceMicrophoneProcessor extends MicrophoneProcessor {

    private boolean testing;

    public VoiceMicrophoneProcessor() {

    }

    @Override
    public int getDeactivationDelay() {
        return VoicechatClient.CLIENT_CONFIG.voiceDeactivationDelay.get();
    }

    @Override
    protected boolean processInternal(short[] audio, boolean testing) {
        this.testing = testing;
        if (isMuted() && !testing) {
            reset();
            return false;
        }

        return AudioUtils.isAboveThreshold(audio, VoicechatClient.CLIENT_CONFIG.voiceActivationThreshold.get());
    }

    @Override
    protected boolean shouldAdjustGain() {
        return !isMuted() || testing;
    }

    @Override
    public MicrophoneActivationType getActivationType() {
        return MicrophoneActivationType.VOICE;
    }

}
