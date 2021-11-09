package de.maxhenkel.voicechat.plugins.impl.packets;

import de.maxhenkel.voicechat.api.Position;
import de.maxhenkel.voicechat.api.packets.LocationalSoundPacket;
import de.maxhenkel.voicechat.plugins.impl.PositionImpl;
import de.maxhenkel.voicechat.voice.common.LocationSoundPacket;

public class LocationalSoundPacketImpl extends SoundPacketImpl implements LocationalSoundPacket {

    private final LocationSoundPacket packet;
    private final Position position;

    public LocationalSoundPacketImpl(LocationSoundPacket packet) {
        super(packet);
        this.packet = packet;
        this.position = new PositionImpl(packet.getLocation());
    }

    public Position getPosition() {
        return position;
    }

    @Override
    public LocationSoundPacket getPacket() {
        return packet;
    }
}
