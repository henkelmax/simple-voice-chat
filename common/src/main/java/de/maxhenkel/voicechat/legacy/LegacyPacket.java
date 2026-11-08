package de.maxhenkel.voicechat.legacy;

import de.maxhenkel.voicechat.net.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

public class LegacyPacket implements Packet<LegacyPacket> {

    private final ResourceLocation identifier;
    private final Consumer<FriendlyByteBuf> writer;

    public LegacyPacket(ResourceLocation identifier, Consumer<FriendlyByteBuf> writer) {
        this.identifier = identifier;
        this.writer = writer;
    }

    @Override
    public ResourceLocation getIdentifier() {
        return identifier;
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
