package de.maxhenkel.voicechat.plugins.impl.packets;

import de.maxhenkel.voicechat.api.Position;
import de.maxhenkel.voicechat.api.packets.EntitySoundPacket;
import de.maxhenkel.voicechat.api.packets.LocationalSoundPacket;
import de.maxhenkel.voicechat.api.packets.SoundPacket;
import de.maxhenkel.voicechat.api.packets.StaticSoundPacket;
import de.maxhenkel.voicechat.plugins.impl.PositionImpl;
import de.maxhenkel.voicechat.voice.common.GroupSoundPacket;
import de.maxhenkel.voicechat.voice.common.LocationSoundPacket;
import de.maxhenkel.voicechat.voice.common.PlayerSoundPacket;
import de.maxhenkel.voicechat.voice.common.Utils;

import javax.annotation.Nullable;
import java.util.UUID;

public class SoundPacketImpl implements SoundPacket {

    private final de.maxhenkel.voicechat.voice.common.SoundPacket<?> packet;

    public SoundPacketImpl(de.maxhenkel.voicechat.voice.common.SoundPacket<?> packet) {
        this.packet = packet;
    }

    @Override
    public UUID getSender() {
        return packet.getSender();
    }

    @Override
    public byte[] getOpusEncodedData() {
        return packet.getData();
    }

    @Override
    public long getSequenceNumber() {
        return packet.getSequenceNumber();
    }

    @Nullable
    @Override
    public String getCategory() {
        return packet.getCategory();
    }

    public de.maxhenkel.voicechat.voice.common.SoundPacket<?> getPacket() {
        return packet;
    }

    @Override
    public EntitySoundPacket.Builder<?> entitySoundPacketBuilder() {
        return new EntitySoundPacketImpl.BuilderImpl(this);
    }

    @Override
    public LocationalSoundPacket.Builder<?> locationalSoundPacketBuilder() {
        return new LocationalSoundPacketImpl.BuilderImpl(this);
    }

    @Override
    public StaticSoundPacket.Builder<?> staticSoundPacketBuilder() {
        return new StaticSoundPacketImpl.BuilderImpl(this);
    }

    @Override
    public EntitySoundPacket toEntitySoundPacket(UUID entityUuid, boolean whispering) {
        return new EntitySoundPacketImpl(new PlayerSoundPacket(packet.getSender(), packet.getData(), packet.getSequenceNumber(), whispering, getDistance(), null));
    }

    @Override
    public LocationalSoundPacket toLocationalSoundPacket(Position position) {
        if (position instanceof PositionImpl p) {
            return new LocationalSoundPacketImpl(new LocationSoundPacket(packet.getSender(), p.getPosition(), packet.getData(), packet.getSequenceNumber(), getDistance(), null));
        } else {
            throw new IllegalArgumentException("position is not an instance of PositionImpl");
        }
    }

    private float getDistance() {
        if (this instanceof EntitySoundPacket p) {
            return p.getDistance();
        } else if (this instanceof LocationalSoundPacket p) {
            return p.getDistance();
        }
        return Utils.getDefaultDistance();
    }

    @Override
    public StaticSoundPacket toStaticSoundPacket() {
        return new StaticSoundPacketImpl(new GroupSoundPacket(packet.getSender(), packet.getData(), packet.getSequenceNumber(), null));
    }

    public abstract static class BuilderImpl<T extends BuilderImpl<T, P>, P extends SoundPacket> implements Builder<T, P> {

        protected UUID sender;
        protected byte[] opusEncodedData;
        protected long sequenceNumber;
        @Nullable
        protected String category;

        public BuilderImpl(SoundPacketImpl soundPacket) {
            this.sender = soundPacket.getSender();
            this.opusEncodedData = soundPacket.getOpusEncodedData();
            this.sequenceNumber = soundPacket.getSequenceNumber();
            this.category = soundPacket.getCategory();
        }

        public BuilderImpl(UUID sender, byte[] opusEncodedData, long sequenceNumber, @Nullable String category) {
            this.sender = sender;
            this.opusEncodedData = opusEncodedData;
            this.sequenceNumber = sequenceNumber;
            this.category = category;
        }

        @Override
        public T opusEncodedData(byte[] data) {
            this.opusEncodedData = data;
            return (T) this;
        }

        @Override
        public T category(@Nullable String category) {
            this.category = category;
            return (T) this;
        }
    }

}
