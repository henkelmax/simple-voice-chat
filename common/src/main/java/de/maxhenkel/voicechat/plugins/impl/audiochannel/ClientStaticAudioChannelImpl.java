package de.maxhenkel.voicechat.plugins.impl.audiochannel;

import de.maxhenkel.voicechat.api.audiochannel.ClientStaticAudioChannel;
import de.maxhenkel.voicechat.voice.common.GroupSoundPacket;
import de.maxhenkel.voicechat.voice.common.SoundPacket;

import java.util.UUID;

public class ClientStaticAudioChannelImpl extends ClientAudioChannelImpl implements ClientStaticAudioChannel {

    public ClientStaticAudioChannelImpl(UUID id) {
        super(id);
    }

    @Override
    protected SoundPacket<?> createSoundPacket(short[] rawAudio) {
        return new GroupSoundPacket(id, rawAudio, category);
    }

}
