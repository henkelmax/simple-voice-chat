package de.maxhenkel.voicechat.plugins.impl.packets;

import de.maxhenkel.voicechat.api.Position;
import de.maxhenkel.voicechat.api.packets.EntitySoundPacket;
import de.maxhenkel.voicechat.api.packets.LocationalSoundPacket;
import de.maxhenkel.voicechat.api.packets.MicrophonePacket;
import de.maxhenkel.voicechat.api.packets.StaticSoundPacket;
import de.maxhenkel.voicechat.plugins.impl.PositionImpl;
import de.maxhenkel.voicechat.voice.common.*;

import java.util.UUID;

public class MicrophonePacketImpl implements MicrophonePacket {

    private final MicPacket packet;
    private final UUID sender;

    public MicrophonePacketImpl(MicPacket packet, UUID sender) {
        this.packet = packet;
        this.sender = sender;
    }

    @Override
    public boolean isWhispering() {
        return packet.isWhispering();
    }

    @Override
    public byte[] getOpusEncodedData() {
        return packet.getData();
    }

    @Override
    public EntitySoundPacket.Builder<?> entitySoundPacketBuilder() {
        return new EntitySoundPacketImpl.BuilderImpl(sender, sender, packet.getData(), packet.getSequenceNumber(), null);
    }

    @Override
    public LocationalSoundPacket.Builder<?> locationalSoundPacketBuilder() {
        return new LocationalSoundPacketImpl.BuilderImpl(sender, sender, packet.getData(), packet.getSequenceNumber(), null);
    }

    @Override
    public StaticSoundPacket.Builder<?> staticSoundPacketBuilder() {
        return new StaticSoundPacketImpl.BuilderImpl(sender, sender, packet.getData(), packet.getSequenceNumber(), null);
    }

    @Override
    @Deprecated
    public EntitySoundPacket toEntitySoundPacket(UUID entityUuid, boolean whispering) {
        return new EntitySoundPacketImpl(new PlayerSoundPacket(sender, sender, packet.getData(), packet.getSequenceNumber(), whispering, Utils.getDefaultDistance(), null));
    }

    @Override
    @Deprecated
    public LocationalSoundPacket toLocationalSoundPacket(Position position) {
        if (position instanceof PositionImpl p) {
            return new LocationalSoundPacketImpl(new LocationSoundPacket(sender, sender, p.getPosition(), packet.getData(), packet.getSequenceNumber(), Utils.getDefaultDistance(), null));
        } else {
            throw new IllegalArgumentException("position is not an instance of PositionImpl");
        }
    }

    @Override
    @Deprecated
    public StaticSoundPacket toStaticSoundPacket() {
        return new StaticSoundPacketImpl(new GroupSoundPacket(sender, sender, packet.getData(), packet.getSequenceNumber(), null));
    }

}
