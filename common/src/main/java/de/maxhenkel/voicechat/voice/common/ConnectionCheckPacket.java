package de.maxhenkel.voicechat.voice.common;

import net.minecraft.network.FriendlyByteBuf;

public class ConnectionCheckPacket implements Packet<ConnectionCheckPacket> {

    private byte[] nonce;

    public ConnectionCheckPacket(byte[] nonce) {
        this.nonce = nonce;
    }

    public ConnectionCheckPacket() {

    }

    public byte[] getNonce() {
        return nonce;
    }

    @Override
    public ConnectionCheckPacket fromBytes(FriendlyByteBuf buf) {
        ConnectionCheckPacket packet = new ConnectionCheckPacket();
        packet.nonce = new byte[Secret.NONCE_SIZE_BYTES];
        buf.readBytes(packet.nonce);
        return packet;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBytes(nonce);
    }
}
