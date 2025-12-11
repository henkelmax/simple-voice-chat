package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.api.VCByteBuf;

public interface Packet<T extends Packet<T>> {

    String getID();

    T fromBytes(VCByteBuf buf);

    void toBytes(VCByteBuf buf);

}
