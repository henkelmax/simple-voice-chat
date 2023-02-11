package de.maxhenkel.voicechat.voice.common;

import net.minecraft.network.FriendlyByteBuf;

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
