package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.util.FriendlyByteBuf;
import de.maxhenkel.voicechat.util.ResourceLocation;
import org.bukkit.entity.Player;

public interface Packet<T extends Packet> {

    ResourceLocation getID();

    T fromBytes(FriendlyByteBuf buf);

    void toBytes(FriendlyByteBuf buf);

    default void onPacket(Player player) {

    }

}
