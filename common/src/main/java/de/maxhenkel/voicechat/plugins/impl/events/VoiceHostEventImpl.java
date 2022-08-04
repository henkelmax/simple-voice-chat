package de.maxhenkel.voicechat.plugins.impl.events;

import de.maxhenkel.voicechat.api.events.VoiceHostEvent;

public class VoiceHostEventImpl extends ServerEventImpl implements VoiceHostEvent {

    private String voiceHost;

    public VoiceHostEventImpl(String voiceHost) {
        this.voiceHost = voiceHost;
    }

    @Override
    public String getVoiceHost() {
        return voiceHost;
    }

    @Override
    public void setVoiceHost(String voiceHost) {
        this.voiceHost = voiceHost;
    }
}
