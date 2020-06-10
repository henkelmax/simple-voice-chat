package de.maxhenkel.voicechat.voice.common;

import net.minecraft.network.PacketBuffer;

public class KeepAlivePacket implements Packet<KeepAlivePacket> {

    public KeepAlivePacket() {

    }

    @Override
    public KeepAlivePacket fromBytes(PacketBuffer buf) {
        return new KeepAlivePacket();
    }

    @Override
    public void toBytes(PacketBuffer buf) {

    }
}
