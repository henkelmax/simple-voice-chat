package de.maxhenkel.voicechat.net;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public interface Packet<T extends Packet<T>> extends CustomPacketPayload {

    ResourceLocation getIdentifier();

    T fromBytes(FriendlyByteBuf buf);

    void toBytes(FriendlyByteBuf buf);

    @Override
    default void write(FriendlyByteBuf buf) {
        toBytes(buf);
    }

    @Override
    default ResourceLocation id() {
        return getIdentifier();
    }
}
