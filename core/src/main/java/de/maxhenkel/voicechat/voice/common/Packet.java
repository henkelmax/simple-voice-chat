package de.maxhenkel.voicechat.voice.common;

import io.netty.buffer.ByteBuf;

public interface Packet<T extends Packet> {

    T fromBytes(ByteBuf buf);

    void toBytes(ByteBuf buf);

    default long getTTL() {
        return 10_000L;
    }

}
