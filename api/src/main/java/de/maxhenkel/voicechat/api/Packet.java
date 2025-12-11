package de.maxhenkel.voicechat.api;

public interface Packet<T extends Packet<T>> {

    String getID();

    T fromBytes(VCByteBuf buf);

    void toBytes(VCByteBuf buf);

}
