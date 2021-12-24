package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.util.FriendlyByteBuf;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

public interface Packet<T extends Packet> {

    NamespacedKey getID();

    T fromBytes(FriendlyByteBuf buf);

    void toBytes(FriendlyByteBuf buf);

    default void onPacket(Player player) {

    }

}
