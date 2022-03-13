package de.maxhenkel.voicechat.plugins.impl.audiochannel;

import de.maxhenkel.voicechat.api.Position;
import de.maxhenkel.voicechat.api.audiochannel.ClientLocationalAudioChannel;
import de.maxhenkel.voicechat.voice.common.LocationSoundPacket;
import de.maxhenkel.voicechat.voice.common.SoundPacket;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class ClientLocationalAudioChannelImpl extends ClientAudioChannelImpl implements ClientLocationalAudioChannel {

    private Position position;

    public ClientLocationalAudioChannelImpl(UUID id, Position position) {
        super(id);
        this.position = position;
    }

    @Override
    protected SoundPacket<?> createSoundPacket(short[] rawAudio) {
        return new LocationSoundPacket(id, rawAudio, new Vec3(position.getX(), position.getY(), position.getZ()));
    }

    @Override
    public void setLocation(Position position) {
        this.position = position;
    }

    @Override
    public Position getLocation() {
        return position;
    }
}
