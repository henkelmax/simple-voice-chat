package de.maxhenkel.voicechat.voice.common;

import net.minecraft.network.PacketByteBuf;

public interface Packet<T extends Packet> {

    T fromBytes(PacketByteBuf buf);

    void toBytes(PacketByteBuf buf);

}
