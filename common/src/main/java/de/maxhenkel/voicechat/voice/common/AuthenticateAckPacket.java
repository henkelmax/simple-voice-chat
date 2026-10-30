package de.maxhenkel.voicechat.voice.common;

import net.minecraft.network.PacketBuffer;

public class AuthenticateAckPacket implements Packet<AuthenticateAckPacket> {

    private byte[] nonce;

    public AuthenticateAckPacket(byte[] nonce) {
        this.nonce = nonce;
    }

    public AuthenticateAckPacket() {

    }

    public byte[] getNonce() {
        return nonce;
    }

    @Override
    public AuthenticateAckPacket fromBytes(PacketBuffer buf) {
        AuthenticateAckPacket packet = new AuthenticateAckPacket();
        packet.nonce = new byte[NetworkMessage.NONCE_SIZE_BYTES];
        buf.readBytes(packet.nonce);
        return packet;
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeBytes(nonce);
    }
}
