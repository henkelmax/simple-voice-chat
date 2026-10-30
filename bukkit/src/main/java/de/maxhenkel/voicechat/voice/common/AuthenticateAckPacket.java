package de.maxhenkel.voicechat.voice.common;

import de.maxhenkel.voicechat.util.FriendlyByteBuf;

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
    public AuthenticateAckPacket fromBytes(FriendlyByteBuf buf) {
        AuthenticateAckPacket packet = new AuthenticateAckPacket();
        packet.nonce = new byte[Secret.NONCE_SIZE_BYTES];
        buf.readBytes(packet.nonce);
        return packet;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBytes(nonce);
    }
}
