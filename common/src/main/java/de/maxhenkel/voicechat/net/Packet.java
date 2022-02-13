package de.maxhenkel.voicechat.net;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

public interface Packet<T extends Packet<T>> {

    ResourceLocation getIdentifier();

    T fromBytes(PacketBuffer buf);

    void toBytes(PacketBuffer buf);

}
