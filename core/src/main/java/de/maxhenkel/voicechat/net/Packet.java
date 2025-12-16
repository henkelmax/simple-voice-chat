package de.maxhenkel.voicechat.net;

import io.netty.buffer.ByteBuf;

public interface Packet<T extends Packet<T>> {

    String getID();

    T fromBytes(ByteBuf buf);

    void toBytes(ByteBuf buf);

}
