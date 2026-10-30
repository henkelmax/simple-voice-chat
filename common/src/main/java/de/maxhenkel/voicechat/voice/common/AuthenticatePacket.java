package de.maxhenkel.voicechat.voice.common;

import net.minecraft.network.PacketBuffer;

public class AuthenticatePacket implements Packet<AuthenticatePacket> {

    public AuthenticatePacket() {

    }

    @Override
    public AuthenticatePacket fromBytes(PacketBuffer buf) {
        return new AuthenticatePacket();
    }

    @Override
    public void toBytes(PacketBuffer buf) {

    }
}
