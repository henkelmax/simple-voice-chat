package de.maxhenkel.voicechat.net;

import io.netty.buffer.ByteBuf;
import de.maxhenkel.voicechat.voice.common.ResourceLocation;

public interface Packet<T extends Packet<T>> {

    ResourceLocation getIdentifier();

    T fromBytes(ByteBuf buf);

    void toBytes(ByteBuf buf);

}
