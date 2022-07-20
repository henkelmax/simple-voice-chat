package de.maxhenkel.voicechat.plugins.impl.audiochannel;

import de.maxhenkel.voicechat.api.audiochannel.ClientEntityAudioChannel;
import de.maxhenkel.voicechat.voice.common.PlayerSoundPacket;
import de.maxhenkel.voicechat.voice.common.SoundPacket;
import de.maxhenkel.voicechat.voice.common.Utils;

import java.util.UUID;

public class ClientEntityAudioChannelImpl extends ClientAudioChannelImpl implements ClientEntityAudioChannel {

    private boolean whispering;
    private float distance;

    public ClientEntityAudioChannelImpl(UUID id) {
        super(id);
        this.whispering = false;
        this.distance = Utils.getDefaultDistance();
    }

    @Override
    protected SoundPacket<?> createSoundPacket(short[] rawAudio) {
        return new PlayerSoundPacket(id, rawAudio, whispering, distance, category);
    }

    @Override
    public void setWhispering(boolean whispering) {
        this.whispering = whispering;
    }

    @Override
    public boolean isWhispering() {
        return whispering;
    }

    @Override
    public float getDistance() {
        return distance;
    }

    @Override
    public void setDistance(float distance) {
        this.distance = distance;
    }

}
