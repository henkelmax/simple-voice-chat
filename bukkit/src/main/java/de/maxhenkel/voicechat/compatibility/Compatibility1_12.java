package de.maxhenkel.voicechat.compatibility;

import de.maxhenkel.voicechat.BukkitVersion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.entity.Player;

public class Compatibility1_12 extends BaseCompatibility {

    public static final String CHANNEL = "vc";

    public static final BukkitVersion VERSION_1_12_2 = BukkitVersion.parseBukkitVersion("1.12.2-R0.1");
    public static final BukkitVersion VERSION_1_12_1 = BukkitVersion.parseBukkitVersion("1.12.1-R0.1");
    public static final BukkitVersion VERSION_1_12 = BukkitVersion.parseBukkitVersion("1.12-R0.1");

    public static final Compatibility1_12 INSTANCE = new Compatibility1_12();

    @Override
    public String getServerIp(Server server) throws Exception {
        Object minecraftServer = callMethod(server, "getServer");
        Class<?> minecraftServerClass = getClass("net.minecraft.server.v1_12_R1.MinecraftServer");
        return callMethod(minecraftServerClass, minecraftServer, "getServerIp");
    }

    @Override
    public NamespacedKey createNamespacedKey(String key) {
        return new NamespacedKey(CHANNEL, key);
    }

    @Override
    public void sendMessage(Player player, Component component) {
        Class<?> chatMessageTypeClass = getClass("net.minecraft.server.v1_12_R1.ChatMessageType");
        Object b = getField(chatMessageTypeClass, "CHAT");
        send(player, component, b);
    }

    @Override
    public void sendStatusMessage(Player player, Component component) {
        Class<?> chatMessageTypeClass = getClass("net.minecraft.server.v1_12_R1.ChatMessageType");
        Object b = getField(chatMessageTypeClass, "GAME_INFO");
        send(player, component, b);
    }

    private void send(Player player, Component component, Object chatMessageType) {
        String json = GsonComponentSerializer.gson().serialize(component);

        Object entityPlayer = callMethod(player, "getHandle");
        Object playerConnection = getField(entityPlayer, "playerConnection");
        Class<?> packet = getClass("net.minecraft.server.v1_12_R1.Packet");
        Class<?> chatSerializer = getClass("net.minecraft.server.v1_12_R1.IChatBaseComponent$ChatSerializer");

        Class<?> iChatBaseComponentClass = getClass("net.minecraft.server.v1_12_R1.IChatBaseComponent");
        Object iChatBaseComponent = callMethod(chatSerializer, "a", new Class[]{String.class}, json);

        Class<?> packetPlayOutChatClass = getClass("net.minecraft.server.v1_12_R1.PacketPlayOutChat");

        Class<?> chatMessageTypeClass = getClass("net.minecraft.server.v1_12_R1.ChatMessageType");

        Object clientboundSystemChatPacket = callConstructor(packetPlayOutChatClass, new Class[]{iChatBaseComponentClass, chatMessageTypeClass}, iChatBaseComponent, chatMessageType);

        callMethod(playerConnection, "sendPacket", new Class[]{packet}, clientboundSystemChatPacket);
    }

}
