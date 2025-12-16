package de.maxhenkel.voicechat.net;

import io.netty.buffer.ByteBuf;

public interface Packet<T extends Packet<T>> {

    String getIdentifier();

    T fromBytes(ByteBuf buf);

    void toBytes(ByteBuf buf);

}
