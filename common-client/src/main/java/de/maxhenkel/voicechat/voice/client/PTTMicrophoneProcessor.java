package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.VoicechatClient;

public class PTTMicrophoneProcessor extends MicrophoneProcessor {

    private boolean transmitting;

    public PTTMicrophoneProcessor() {

    }

    @Override
    public int getDeactivationDelay() {
        return VoicechatClient.CLIENT_CONFIG.pttDeactivationDelay.get();
    }

    @Override
    protected boolean processInternal(short[] audio, boolean testing) {
        transmitting = isPttButtonDown() || testing;
        return transmitting;
    }

    @Override
    protected boolean shouldAdjustGain() {
        return transmitting;
    }

    @Override
    public MicrophoneActivationType getActivationType() {
        return MicrophoneActivationType.PTT;
    }

}
