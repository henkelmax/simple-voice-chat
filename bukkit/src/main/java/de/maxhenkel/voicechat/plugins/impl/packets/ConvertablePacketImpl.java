package de.maxhenkel.voicechat.plugins.impl.packets;

import de.maxhenkel.voicechat.api.Position;
import de.maxhenkel.voicechat.api.packets.*;
import de.maxhenkel.voicechat.plugins.impl.PositionImpl;
import de.maxhenkel.voicechat.voice.common.GroupSoundPacket;
import de.maxhenkel.voicechat.voice.common.LocationSoundPacket;
import de.maxhenkel.voicechat.voice.common.PlayerSoundPacket;
import de.maxhenkel.voicechat.voice.common.Utils;

import javax.annotation.Nullable;
import java.util.UUID;

public class ConvertablePacketImpl implements ConvertablePacket {
    @Override
    public EntitySoundPacket.Builder<?> entitySoundPacketBuilder() {
        return new GenericEntitySoundPacketBuilder();
    }

    @Override
    public LocationalSoundPacket.Builder<?> locationalSoundPacketBuilder() {
        return new GenericLocationalSoundPacketBuilder();
    }

    @Override
    public StaticSoundPacket.Builder<?> staticSoundPacketBuilder() {
        return new GenericStaticSoundPacketBuilder();
    }

    @Override
    public EntitySoundPacket toEntitySoundPacket(UUID entityUuid, boolean whispering) {
        throw new UnsupportedOperationException();
    }

    @Override
    public LocationalSoundPacket toLocationalSoundPacket(Position position) {
        throw new UnsupportedOperationException();
    }

    @Override
    public StaticSoundPacket toStaticSoundPacket() {
        throw new UnsupportedOperationException();
    }

    public abstract static class GenericSoundPacketBuilder<T extends SoundPacket.Builder<T, P>, P extends SoundPacket> implements SoundPacket.Builder<T, P> {

        @Nullable
        protected UUID channelId;
        @Nullable
        protected UUID sender;
        @Nullable
        protected byte[] opusEncodedData;
        @Nullable
        protected Long sequenceNumber;
        @Nullable
        protected String category;

        public GenericSoundPacketBuilder() {

        }

        @Override
        public T channelId(UUID channelId) {
            this.channelId = channelId;
            return (T) this;
        }

        @Override
        public T sender(UUID sender) {
            this.sender = sender;
            return (T) this;
        }

        @Override
        public T opusEncodedData(byte[] data) {
            this.opusEncodedData = data;
            return (T) this;
        }

        @Override
        public T sequenceNumber(long sequenceNumber) {
            this.sequenceNumber = sequenceNumber;
            return (T) this;
        }

        @Override
        public T category(@Nullable String category) {
            this.category = category;
            return (T) this;
        }

        public void checkRequiredFields() {
            if (channelId == null) {
                throw new IllegalStateException("Channel id must be set");
            }
            if (sequenceNumber == null) {
                throw new IllegalStateException("Sequence number must be set");
            }
            if (opusEncodedData == null) {
                throw new IllegalStateException("Opus encoded data must be set");
            }
        }
    }

    public static class GenericEntitySoundPacketBuilder extends GenericSoundPacketBuilder<GenericEntitySoundPacketBuilder, EntitySoundPacket> implements EntitySoundPacket.Builder<GenericEntitySoundPacketBuilder> {

        protected UUID entityUuid;
        protected boolean whispering;
        protected Float distance;

        public GenericEntitySoundPacketBuilder() {

        }

        @Override
        public GenericEntitySoundPacketBuilder entityUuid(UUID entityUuid) {
            this.entityUuid = entityUuid;
            return this;
        }

        @Override
        public GenericEntitySoundPacketBuilder whispering(boolean whispering) {
            this.whispering = whispering;
            return this;
        }

        @Override
        public GenericEntitySoundPacketBuilder distance(float distance) {
            this.distance = distance;
            return this;
        }

        @Override
        public EntitySoundPacket build() {
            checkRequiredFields();
            if (entityUuid == null) {
                throw new IllegalStateException("Entity UUID must be set");
            }
            if (distance == null) {
                distance = Utils.getDefaultDistance();
            }
            return new EntitySoundPacketImpl(new PlayerSoundPacket(channelId, sender, opusEncodedData, sequenceNumber, whispering, distance, category));
        }
    }

    public static class GenericLocationalSoundPacketBuilder extends GenericSoundPacketBuilder<GenericLocationalSoundPacketBuilder, LocationalSoundPacket> implements LocationalSoundPacket.Builder<GenericLocationalSoundPacketBuilder> {

        protected PositionImpl position;
        protected Float distance;

        @Override
        public GenericLocationalSoundPacketBuilder position(Position position) {
            this.position = (PositionImpl) position;
            return this;
        }

        @Override
        public GenericLocationalSoundPacketBuilder distance(float distance) {
            this.distance = distance;
            return this;
        }

        @Override
        public LocationalSoundPacket build() {
            checkRequiredFields();
            if (position == null) {
                throw new IllegalStateException("Position must be set");
            }
            if (distance == null) {
                distance = Utils.getDefaultDistance();
            }
            return new LocationalSoundPacketImpl(new LocationSoundPacket(channelId, sender, position.getPosition(), opusEncodedData, sequenceNumber, distance, category));
        }
    }

    public static class GenericStaticSoundPacketBuilder extends GenericSoundPacketBuilder<GenericStaticSoundPacketBuilder, StaticSoundPacket> implements StaticSoundPacket.Builder<GenericStaticSoundPacketBuilder> {
        @Override
        public StaticSoundPacket build() {
            return new StaticSoundPacketImpl(new GroupSoundPacket(channelId, sender, opusEncodedData, sequenceNumber, category));
        }
    }

}
