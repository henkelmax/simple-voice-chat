package de.maxhenkel.voicechat.net;

import com.comphenix.protocol.wrappers.MinecraftKey;
import de.maxhenkel.voicechat.util.FriendlyByteBuf;

public interface Packet<T extends Packet> {

    MinecraftKey getID();

    T fromBytes(FriendlyByteBuf buf);

    void toBytes(FriendlyByteBuf buf);

}
