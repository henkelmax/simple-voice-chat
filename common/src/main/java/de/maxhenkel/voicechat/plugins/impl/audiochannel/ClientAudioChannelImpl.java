package de.maxhenkel.voicechat.plugins.impl.audiochannel;

import de.maxhenkel.voicechat.api.audiochannel.ClientAudioChannel;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.client.ClientVoicechat;
import de.maxhenkel.voicechat.voice.common.SoundPacket;

import java.util.UUID;

public abstract class ClientAudioChannelImpl implements ClientAudioChannel {

    protected UUID id;

    public ClientAudioChannelImpl(UUID id) {
        this.id = id;
    }

    @Override
    public UUID getId() {
        return id;
    }

    protected abstract SoundPacket<?> createSoundPacket(short[] rawAudio);

    @Override
    public void play(short[] rawAudio) {
        ClientVoicechat client = ClientManager.getClient();
        if (client != null) {
            client.processSoundPacket(createSoundPacket(rawAudio));
        }
    }
}
