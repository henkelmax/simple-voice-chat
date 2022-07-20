package de.maxhenkel.voicechat.plugins.impl.packets;

import de.maxhenkel.voicechat.api.packets.EntitySoundPacket;
import de.maxhenkel.voicechat.voice.common.PlayerSoundPacket;
import de.maxhenkel.voicechat.voice.common.Utils;

import javax.annotation.Nullable;
import java.util.UUID;

public class EntitySoundPacketImpl extends SoundPacketImpl implements EntitySoundPacket {

    private final PlayerSoundPacket packet;

    public EntitySoundPacketImpl(PlayerSoundPacket packet) {
        super(packet);
        this.packet = packet;
    }

    @Override
    public UUID getEntityUuid() {
        return packet.getSender();
    }

    @Override
    public boolean isWhispering() {
        return packet.isWhispering();
    }

    @Override
    public float getDistance() {
        return packet.getDistance();
    }

    @Override
    public PlayerSoundPacket getPacket() {
        return packet;
    }

    public static class BuilderImpl extends SoundPacketImpl.BuilderImpl<BuilderImpl, EntitySoundPacket> implements EntitySoundPacket.Builder<BuilderImpl> {

        protected UUID entityUuid;
        protected boolean whispering;
        protected float distance;

        public BuilderImpl(SoundPacketImpl soundPacket) {
            super(soundPacket);
            if (soundPacket instanceof EntitySoundPacketImpl p) {
                entityUuid = p.getEntityUuid();
                whispering = p.isWhispering();
                distance = p.getDistance();
            } else if (soundPacket instanceof LocationalSoundPacketImpl p) {
                distance = p.getDistance();
            } else {
                distance = Utils.getDefaultDistance();
            }
        }

        public BuilderImpl(UUID sender, byte[] opusEncodedData, long sequenceNumber, @Nullable String category) {
            super(sender, opusEncodedData, sequenceNumber, category);
            this.distance = Utils.getDefaultDistance();
        }

        @Override
        public BuilderImpl entityUuid(UUID entityUuid) {
            this.entityUuid = entityUuid;
            return this;
        }

        @Override
        public BuilderImpl whispering(boolean whispering) {
            this.whispering = whispering;
            return this;
        }

        @Override
        public BuilderImpl distance(float distance) {
            this.distance = distance;
            return this;
        }

        @Override
        public EntitySoundPacket build() {
            if (entityUuid == null) {
                throw new IllegalStateException("entityUuid missing");
            }
            return new EntitySoundPacketImpl(new PlayerSoundPacket(sender, opusEncodedData, sequenceNumber, whispering, distance, category));
        }

    }
}
