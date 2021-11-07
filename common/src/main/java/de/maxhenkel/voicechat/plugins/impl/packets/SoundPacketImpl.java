package de.maxhenkel.voicechat.plugins.impl.packets;

import de.maxhenkel.voicechat.api.packets.EntitySoundPacket;
import de.maxhenkel.voicechat.api.packets.LocationalSoundPacket;
import de.maxhenkel.voicechat.api.packets.SoundPacket;
import de.maxhenkel.voicechat.api.packets.StaticSoundPacket;
import de.maxhenkel.voicechat.voice.common.GroupSoundPacket;
import de.maxhenkel.voicechat.voice.common.LocationSoundPacket;
import de.maxhenkel.voicechat.voice.common.PlayerSoundPacket;
import net.minecraft.world.phys.Vec3;

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

    public de.maxhenkel.voicechat.voice.common.SoundPacket<?> getPacket() {
        return packet;
    }

    @Override
    public EntitySoundPacket toEntitySoundPacket(UUID entityUuid, boolean whispering) {
        return new EntitySoundPacketImpl(new PlayerSoundPacket(packet.getSender(), packet.getData(), packet.getSequenceNumber(), whispering));
    }

    @Override
    public LocationalSoundPacket toLocationalSoundPacket(Vec3 position) {
        return new LocationalSoundPacketImpl(new LocationSoundPacket(packet.getSender(), position, packet.getData(), packet.getSequenceNumber()));
    }

    @Override
    public StaticSoundPacket toStaticSoundPacket() {
        return new StaticSoundPacketImpl(new GroupSoundPacket(packet.getSender(), packet.getData(), packet.getSequenceNumber()));
    }
}
