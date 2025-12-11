package de.maxhenkel.voicechat.voice.common;

import de.maxhenkel.voicechat.api.VCByteBuf;

public class AuthenticateAckPacket implements Packet<AuthenticateAckPacket> {

    public AuthenticateAckPacket() {

    }

    @Override
    public AuthenticateAckPacket fromBytes(VCByteBuf buf) {
        return new AuthenticateAckPacket();
    }

    @Override
    public void toBytes(VCByteBuf buf) {

    }
}
