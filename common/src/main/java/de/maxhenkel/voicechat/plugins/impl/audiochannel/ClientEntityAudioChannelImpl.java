package de.maxhenkel.voicechat.plugins.impl.audiochannel;

import de.maxhenkel.voicechat.api.audiochannel.ClientEntityAudioChannel;
import de.maxhenkel.voicechat.voice.common.PlayerSoundPacket;
import de.maxhenkel.voicechat.voice.common.SoundPacket;

import java.util.UUID;

public class ClientEntityAudioChannelImpl extends ClientAudioChannelImpl implements ClientEntityAudioChannel {

    private boolean whispering;

    public ClientEntityAudioChannelImpl(UUID id) {
        super(id);
        this.whispering = false;
    }

    @Override
    protected SoundPacket<?> createSoundPacket(short[] rawAudio) {
        return new PlayerSoundPacket(id, rawAudio, whispering);
    }

    @Override
    public void setWhispering(boolean whispering) {
        this.whispering = whispering;
    }

    @Override
    public boolean isWhispering() {
        return whispering;
    }

}
