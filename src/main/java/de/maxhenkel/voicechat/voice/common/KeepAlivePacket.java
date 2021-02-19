package de.maxhenkel.voicechat.voice.common;

import net.minecraft.network.PacketByteBuf;

public class KeepAlivePacket implements Packet<KeepAlivePacket> {

    public KeepAlivePacket() {

    }

    @Override
    public KeepAlivePacket fromBytes(PacketByteBuf buf) {
        return new KeepAlivePacket();
    }

    @Override
    public void toBytes(PacketByteBuf buf) {

    }
}
