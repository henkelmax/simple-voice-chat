package de.maxhenkel.voicechat.plugins.impl.packets;

import de.maxhenkel.voicechat.api.Position;
import de.maxhenkel.voicechat.api.packets.LocationalSoundPacket;
import de.maxhenkel.voicechat.voice.common.LocationSoundPacket;
import de.maxhenkel.voicechat.voice.common.Utils;

import javax.annotation.Nullable;
import java.util.UUID;

public class LocationalSoundPacketImpl extends SoundPacketImpl implements LocationalSoundPacket {

    private final LocationSoundPacket packet;
    private final Position position;

    public LocationalSoundPacketImpl(LocationSoundPacket packet) {
        super(packet);
        this.packet = packet;
        this.position = packet.getLocation();
    }

    public Position getPosition() {
        return position;
    }

    @Override
    public float getDistance() {
        return packet.getDistance();
    }

    @Override
    public LocationSoundPacket getPacket() {
        return packet;
    }

    public static class BuilderImpl extends SoundPacketImpl.BuilderImpl<BuilderImpl, LocationalSoundPacket> implements LocationalSoundPacket.Builder<BuilderImpl> {

        protected Position position;
        protected float distance;

        public BuilderImpl(SoundPacketImpl soundPacket) {
            super(soundPacket);
            if (soundPacket instanceof LocationalSoundPacketImpl p) {
                position = p.position;
                distance = p.getDistance();
            } else if (soundPacket instanceof EntitySoundPacketImpl p) {
                distance = p.getDistance();
            } else {
                distance = Utils.getDefaultDistanceServer();
            }
        }

        public BuilderImpl(UUID channelId, UUID sender, byte[] opusEncodedData, long sequenceNumber, @Nullable String category) {
            super(channelId, sender, opusEncodedData, sequenceNumber, category);
            distance = Utils.getDefaultDistanceServer();
        }

        @Override
        public BuilderImpl position(Position position) {
            this.position = position;
            return this;
        }

        @Override
        public LocationalSoundPacketImpl.BuilderImpl distance(float distance) {
            this.distance = distance;
            return this;
        }

        @Override
        public LocationalSoundPacket build() {
            if (position == null) {
                throw new IllegalStateException("position missing");
            }
            return new LocationalSoundPacketImpl(new LocationSoundPacket(channelId, sender, position, opusEncodedData, sequenceNumber, distance, category));
        }

    }

}
