package de.maxhenkel.voicechat.voice.common;

import de.maxhenkel.voicechat.util.FriendlyByteBuf;

public class AuthenticateAckPacket implements Packet<AuthenticateAckPacket> {

    public AuthenticateAckPacket() {

    }

    @Override
    public AuthenticateAckPacket fromBytes(FriendlyByteBuf buf) {
        return new AuthenticateAckPacket();
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {

    }
}
