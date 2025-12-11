package de.maxhenkel.voicechat.voice.common;

import de.maxhenkel.voicechat.api.VCByteBuf;

public class ConnectionCheckAckPacket implements Packet<ConnectionCheckAckPacket> {

    public ConnectionCheckAckPacket() {

    }

    @Override
    public ConnectionCheckAckPacket fromBytes(VCByteBuf buf) {
        return new ConnectionCheckAckPacket();
    }

    @Override
    public void toBytes(VCByteBuf buf) {

    }
}
