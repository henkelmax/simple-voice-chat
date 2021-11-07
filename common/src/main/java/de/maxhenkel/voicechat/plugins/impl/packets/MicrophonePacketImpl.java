package de.maxhenkel.voicechat.plugins.impl.packets;

import de.maxhenkel.voicechat.api.packets.EntitySoundPacket;
import de.maxhenkel.voicechat.api.packets.LocationalSoundPacket;
import de.maxhenkel.voicechat.api.packets.MicrophonePacket;
import de.maxhenkel.voicechat.api.packets.StaticSoundPacket;
import de.maxhenkel.voicechat.voice.common.*;
import net.minecraft.world.phys.Vec3;

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
    public EntitySoundPacket toEntitySoundPacket(UUID entityUuid, boolean whispering) {
        return new EntitySoundPacketImpl(new PlayerSoundPacket(sender, packet.getData(), packet.getSequenceNumber(), whispering));
    }

    @Override
    public LocationalSoundPacket toLocationalSoundPacket(Vec3 position) {
        return new LocationalSoundPacketImpl(new LocationSoundPacket(sender, position, packet.getData(), packet.getSequenceNumber()));
    }

    @Override
    public StaticSoundPacket toStaticSoundPacket() {
        return new StaticSoundPacketImpl(new GroupSoundPacket(sender, packet.getData(), packet.getSequenceNumber()));
    }

}
