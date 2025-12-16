package de.maxhenkel.voicechat.voice.common;

import io.netty.buffer.ByteBuf;

public class ConnectionCheckPacket implements Packet<ConnectionCheckPacket> {

    public ConnectionCheckPacket() {

    }

    @Override
    public ConnectionCheckPacket fromBytes(ByteBuf buf) {
        return new ConnectionCheckPacket();
    }

    @Override
    public void toBytes(ByteBuf buf) {

    }
}
