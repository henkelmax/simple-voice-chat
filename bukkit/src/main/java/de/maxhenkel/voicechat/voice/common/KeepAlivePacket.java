package de.maxhenkel.voicechat.voice.common;

import de.maxhenkel.voicechat.util.FriendlyByteBuf;

public class KeepAlivePacket implements Packet<KeepAlivePacket> {

    public KeepAlivePacket() {

    }

    @Override
    public KeepAlivePacket fromBytes(FriendlyByteBuf buf) {
        return new KeepAlivePacket();
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {

    }
}
