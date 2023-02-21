package de.maxhenkel.voicechat.voice.common;

import net.minecraft.network.PacketBuffer;

public class ConnectionCheckPacket implements Packet<ConnectionCheckPacket> {

    public ConnectionCheckPacket() {

    }

    @Override
    public ConnectionCheckPacket fromBytes(PacketBuffer buf) {
        return new ConnectionCheckPacket();
    }

    @Override
    public void toBytes(PacketBuffer buf) {

    }
}
