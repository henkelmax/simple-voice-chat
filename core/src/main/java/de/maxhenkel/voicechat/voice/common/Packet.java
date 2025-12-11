package de.maxhenkel.voicechat.voice.common;

import de.maxhenkel.voicechat.api.VCByteBuf;

public interface Packet<T extends Packet> {

    T fromBytes(VCByteBuf buf);

    void toBytes(VCByteBuf buf);

    default long getTTL() {
        return 10_000L;
    }

}
