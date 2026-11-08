package de.maxhenkel.voicechat.legacy;

import de.maxhenkel.voicechat.net.Packet;
import de.maxhenkel.voicechat.util.FriendlyByteBuf;
import de.maxhenkel.voicechat.util.Key;

import java.util.function.Consumer;

public class LegacyPacket implements Packet<LegacyPacket> {

    private final Key id;
    private final Consumer<FriendlyByteBuf> writer;

    public LegacyPacket(Key id, Consumer<FriendlyByteBuf> writer) {
        this.id = id;
        this.writer = writer;
    }

    @Override
    public Key getID() {
        return id;
    }

    @Override
    public LegacyPacket fromBytes(FriendlyByteBuf buf) {
        throw new UnsupportedOperationException("Legacy packets can only be sent");
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        writer.accept(buf);
    }

}
