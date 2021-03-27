package de.maxhenkel.voicechat.net;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public interface Packet<T extends Packet> {

    Identifier getID();

    T fromBytes(PacketByteBuf buf);

    void toBytes(PacketByteBuf buf);

}
