package de.maxhenkel.voicechat.voice.common;

import net.minecraft.network.PacketBuffer;

public class ConnectionCheckAckPacket implements Packet<ConnectionCheckAckPacket> {

    public ConnectionCheckAckPacket() {

    }

    @Override
    public ConnectionCheckAckPacket fromBytes(PacketBuffer buf) {
        return new ConnectionCheckAckPacket();
    }

    @Override
    public void toBytes(PacketBuffer buf) {

    }
}
