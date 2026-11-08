package de.maxhenkel.voicechat.legacy;

import de.maxhenkel.voicechat.net.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

import java.util.function.Consumer;

public class LegacyPacket implements Packet<LegacyPacket> {

    private final ResourceLocation identifier;
    private final Consumer<PacketBuffer> writer;

    public LegacyPacket(ResourceLocation identifier, Consumer<PacketBuffer> writer) {
        this.identifier = identifier;
        this.writer = writer;
    }

    @Override
    public ResourceLocation getIdentifier() {
        return identifier;
    }

    @Override
    public LegacyPacket fromBytes(PacketBuffer buf) {
        throw new UnsupportedOperationException("Legacy packets can only be sent");
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        writer.accept(buf);
    }

}
