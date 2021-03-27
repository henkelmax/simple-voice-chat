package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.Voicechat;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class RequestPlayerListPacket implements Packet<RequestPlayerListPacket> {

    public static final Identifier REQUEST_PLAYER_LIST = new Identifier(Voicechat.MODID, "request_player_list");

    public RequestPlayerListPacket() {

    }

    @Override
    public Identifier getID() {
        return REQUEST_PLAYER_LIST;
    }

    @Override
    public RequestPlayerListPacket fromBytes(PacketByteBuf buf) {

        return this;
    }

    @Override
    public void toBytes(PacketByteBuf buf) {

    }
}
