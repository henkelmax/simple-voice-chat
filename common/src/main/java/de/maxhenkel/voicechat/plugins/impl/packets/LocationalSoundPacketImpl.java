package de.maxhenkel.voicechat.plugins.impl.packets;

import de.maxhenkel.voicechat.api.packets.LocationalSoundPacket;
import de.maxhenkel.voicechat.voice.common.LocationSoundPacket;
import net.minecraft.world.phys.Vec3;

public class LocationalSoundPacketImpl extends SoundPacketImpl implements LocationalSoundPacket {

    private final LocationSoundPacket packet;

    public LocationalSoundPacketImpl(LocationSoundPacket packet) {
        super(packet);
        this.packet = packet;
    }

    public Vec3 getPosition() {
        return packet.getLocation();
    }

    @Override
    public LocationSoundPacket getPacket() {
        return packet;
    }
}
