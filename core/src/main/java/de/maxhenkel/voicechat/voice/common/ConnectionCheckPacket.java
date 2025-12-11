package de.maxhenkel.voicechat.voice.common;

import de.maxhenkel.voicechat.api.VCByteBuf;

public class ConnectionCheckPacket implements Packet<ConnectionCheckPacket> {

    public ConnectionCheckPacket() {

    }

    @Override
    public ConnectionCheckPacket fromBytes(VCByteBuf buf) {
        return new ConnectionCheckPacket();
    }

    @Override
    public void toBytes(VCByteBuf buf) {

    }
}
