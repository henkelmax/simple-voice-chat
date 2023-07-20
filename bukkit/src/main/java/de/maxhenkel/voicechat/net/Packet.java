package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.util.FriendlyByteBuf;
import net.kyori.adventure.key.Key;
import org.bukkit.entity.Player;

public interface Packet<T extends Packet> {

    Key getID();

    T fromBytes(FriendlyByteBuf buf);

    void toBytes(FriendlyByteBuf buf);

    default void onPacket(Player player) {

    }

}
