package de.maxhenkel.voicechat.plugins.impl.events;

import de.maxhenkel.voicechat.api.VoicechatClientApi;
import de.maxhenkel.voicechat.api.events.ClientSoundEvent;

public class ClientSoundEventImpl extends ClientEventImpl implements ClientSoundEvent {

    private final short[] rawAudio;

    public ClientSoundEventImpl(VoicechatClientApi api, short[] rawAudio) {
        super(api);
        this.rawAudio = rawAudio;
    }

    @Override
    public short[] getRawAudio() {
        return rawAudio;
    }
}
