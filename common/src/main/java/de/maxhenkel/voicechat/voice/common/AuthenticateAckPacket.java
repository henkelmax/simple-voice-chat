package de.maxhenkel.voicechat.voice.common;

import net.minecraft.network.PacketBuffer;

public class AuthenticateAckPacket implements Packet<AuthenticateAckPacket> {

    public AuthenticateAckPacket() {

    }

    @Override
    public AuthenticateAckPacket fromBytes(PacketBuffer buf) {
        return new AuthenticateAckPacket();
    }

    @Override
    public void toBytes(PacketBuffer buf) {

    }
}
