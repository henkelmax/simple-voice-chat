package de.maxhenkel.voicechat.voice.common;

import net.minecraft.network.FriendlyByteBuf;

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
