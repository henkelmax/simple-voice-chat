package de.maxhenkel.voicechat.voice.common;

import de.maxhenkel.voicechat.util.FriendlyByteBuf;

public class AuthenticatePacket implements Packet<AuthenticatePacket> {

    public AuthenticatePacket() {

    }

    @Override
    public AuthenticatePacket fromBytes(FriendlyByteBuf buf) {
        return new AuthenticatePacket();
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {

    }
}
