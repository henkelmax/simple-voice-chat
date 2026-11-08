package de.maxhenkel.voicechat.legacy;

import de.maxhenkel.voicechat.net.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.function.Consumer;

public class LegacyPacket implements Packet<LegacyPacket> {

    private final Type<LegacyPacket> type;
    private final Consumer<FriendlyByteBuf> writer;

    public LegacyPacket(CustomPacketPayload.Type<?> type, Consumer<FriendlyByteBuf> writer) {
        this.type = new Type<>(type.id());
        this.writer = writer;
    }

    @Override
    public LegacyPacket fromBytes(FriendlyByteBuf buf) {
        throw new UnsupportedOperationException("Legacy packets can only be sent");
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        writer.accept(buf);
    }

    @Override
    public Type<LegacyPacket> type() {
        return type;
    }

}
