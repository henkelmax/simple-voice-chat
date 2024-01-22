package de.maxhenkel.voicechat.net;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public interface Packet<T extends Packet<T>> extends CustomPacketPayload {

    T fromBytes(RegistryFriendlyByteBuf buf);

    void toBytes(RegistryFriendlyByteBuf buf);

    @Override
    Type<T> type();
}
