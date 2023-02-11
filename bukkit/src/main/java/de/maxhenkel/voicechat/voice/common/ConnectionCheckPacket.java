package de.maxhenkel.voicechat.voice.common;

import de.maxhenkel.voicechat.util.FriendlyByteBuf;

public class ConnectionCheckPacket implements Packet<ConnectionCheckPacket> {

    public ConnectionCheckPacket() {

    }

    @Override
    public ConnectionCheckPacket fromBytes(FriendlyByteBuf buf) {
        return new ConnectionCheckPacket();
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {

    }
}
