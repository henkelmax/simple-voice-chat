package de.maxhenkel.voicechat.voice.common;

import io.netty.buffer.ByteBuf;

public class ConnectionCheckAckPacket implements Packet<ConnectionCheckAckPacket> {

    public ConnectionCheckAckPacket() {

    }

    @Override
    public ConnectionCheckAckPacket fromBytes(ByteBuf buf) {
        return new ConnectionCheckAckPacket();
    }

    @Override
    public void toBytes(ByteBuf buf) {

    }
}
