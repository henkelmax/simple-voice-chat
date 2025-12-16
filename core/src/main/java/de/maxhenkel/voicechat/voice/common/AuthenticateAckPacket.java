package de.maxhenkel.voicechat.voice.common;

import io.netty.buffer.ByteBuf;

public class AuthenticateAckPacket implements Packet<AuthenticateAckPacket> {

    public AuthenticateAckPacket() {

    }

    @Override
    public AuthenticateAckPacket fromBytes(ByteBuf buf) {
        return new AuthenticateAckPacket();
    }

    @Override
    public void toBytes(ByteBuf buf) {

    }
}
