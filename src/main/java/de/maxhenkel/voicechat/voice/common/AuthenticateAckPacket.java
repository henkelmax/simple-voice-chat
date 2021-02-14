package de.maxhenkel.voicechat.voice.common;

import net.minecraft.network.PacketByteBuf;

public class AuthenticateAckPacket implements Packet<AuthenticateAckPacket> {

    public AuthenticateAckPacket() {

    }

    @Override
    public AuthenticateAckPacket fromBytes(PacketByteBuf buf) {
        AuthenticateAckPacket packet = new AuthenticateAckPacket();
        return packet;
    }

    @Override
    public void toBytes(PacketByteBuf buf) {

    }
}
