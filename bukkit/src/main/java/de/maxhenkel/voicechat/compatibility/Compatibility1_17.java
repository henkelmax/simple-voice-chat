package de.maxhenkel.voicechat.compatibility;

import de.maxhenkel.voicechat.BukkitVersion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import java.util.UUID;

public class Compatibility1_17 extends BaseCompatibility {

    public static final BukkitVersion VERSION_1_17_1 = BukkitVersion.parseBukkitVersion("1.17.1-R0.1");
    public static final BukkitVersion VERSION_1_17 = BukkitVersion.parseBukkitVersion("1.17-R0.1");

    public static final Compatibility1_17 INSTANCE = new Compatibility1_17();

    @Override
    public String getServerIp(Server server) throws Exception {
        return Compatibility1_19.INSTANCE.getServerIp(server);
    }

    @Override
    public void sendMessage(Player player, Component component) {
        Class<?> chatMessageTypeClass = getClass("net.minecraft.network.chat.ChatMessageType");
        Object b = getField(chatMessageTypeClass, "b");
        send(player, component, b);
    }

    @Override
    public void sendStatusMessage(Player player, Component component) {
        Class<?> chatMessageTypeClass = getClass("net.minecraft.network.chat.ChatMessageType");
        Object b = getField(chatMessageTypeClass, "c");
        send(player, component, b);
    }

    private static final UUID NUL_UUID = new UUID(0L, 0L);

    private void send(Player player, Component component, Object chatMessageType) {
        String json = GsonComponentSerializer.gson().serialize(component);

        Object entityPlayer = callMethod(player, "getHandle");
        Object playerConnection = getField(entityPlayer, "b");
        Class<?> packet = getClass("net.minecraft.network.protocol.Packet");
        Class<?> craftChatMessage = getClass(
                "org.bukkit.craftbukkit.v1_17_R1.util.CraftChatMessage"
        );

        Class<?> iChatBaseComponentClass = getClass("net.minecraft.network.chat.IChatBaseComponent");
        Object iChatBaseComponent = callMethod(craftChatMessage, "fromJSON", new Class[]{String.class}, json);

        Class<?> packetPlayOutChatClass = getClass("net.minecraft.network.protocol.game.PacketPlayOutChat");

        Class<?> chatMessageTypeClass = getClass("net.minecraft.network.chat.ChatMessageType");

        Object clientboundSystemChatPacket = callConstructor(packetPlayOutChatClass, new Class[]{iChatBaseComponentClass, chatMessageTypeClass, UUID.class}, iChatBaseComponent, chatMessageType, NUL_UUID);

        callMethod(playerConnection, "sendPacket", new Class[]{packet}, clientboundSystemChatPacket);
    }

}
