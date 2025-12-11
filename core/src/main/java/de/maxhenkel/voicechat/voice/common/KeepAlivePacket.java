package de.maxhenkel.voicechat.voice.common;

import de.maxhenkel.voicechat.api.VCByteBuf;

public class KeepAlivePacket implements Packet<KeepAlivePacket> {

    public KeepAlivePacket() {

    }

    @Override
    public KeepAlivePacket fromBytes(VCByteBuf buf) {
        return new KeepAlivePacket();
    }

    @Override
    public void toBytes(VCByteBuf buf) {

    }
}
