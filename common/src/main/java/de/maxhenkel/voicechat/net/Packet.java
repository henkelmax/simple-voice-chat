package de.maxhenkel.voicechat.net;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public interface Packet<T extends Packet<T>> extends CustomPacketPayload {

    T fromBytes(FriendlyByteBuf buf);

    void toBytes(FriendlyByteBuf buf);

    @Override
    Type<T> type();
}
