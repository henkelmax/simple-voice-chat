package de.maxhenkel.voicechat.voice.common;

import net.minecraft.network.PacketBuffer;

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
    public ConnectionCheckPacket fromBytes(PacketBuffer buf) {
        ConnectionCheckPacket packet = new ConnectionCheckPacket();
        packet.nonce = new byte[NetworkMessage.NONCE_SIZE_BYTES];
        buf.readBytes(packet.nonce);
        return packet;
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeBytes(nonce);
    }
}
