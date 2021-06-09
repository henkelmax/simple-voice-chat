package de.maxhenkel.voicechat.voice.common;

import net.minecraft.network.PacketBuffer;

public interface Packet<T extends Packet> {

    T fromBytes(PacketBuffer buf);

    void toBytes(PacketBuffer buf);

    default long getTTL() {
        return 10_000L;
    }

}
