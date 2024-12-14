package de.maxhenkel.voicechat.plugins.impl.events;

import de.maxhenkel.voicechat.api.events.ClientSoundEvent;

public class ClientSoundEventImpl extends ClientEventImpl implements ClientSoundEvent {

    private short[] rawAudio;
    private boolean whispering;

    public ClientSoundEventImpl(short[] rawAudio, boolean whispering) {
        this.rawAudio = rawAudio;
        this.whispering = whispering;
    }

    @Override
    public short[] getRawAudio() {
        return rawAudio;
    }

    @Override
    public void setRawAudio(short[] rawAudio) {
        this.rawAudio = rawAudio;
    }

    @Override
    public boolean isWhispering() {
        return whispering;
    }
}
