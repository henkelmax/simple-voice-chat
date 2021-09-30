package de.maxhenkel.voicechat.net;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public interface Packet<T extends Packet<T>> {

    ResourceLocation getIdentifier();

    int getID();

    T fromBytes(FriendlyByteBuf buf);

    void toBytes(FriendlyByteBuf buf);

}
