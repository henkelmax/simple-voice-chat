package de.maxhenkel.voicechat.voice.common;

import io.netty.buffer.ByteBuf;

public class KeepAlivePacket implements Packet<KeepAlivePacket> {

    public KeepAlivePacket() {

    }

    @Override
    public KeepAlivePacket fromBytes(ByteBuf buf) {
        return new KeepAlivePacket();
    }

    @Override
    public void toBytes(ByteBuf buf) {

    }
}
