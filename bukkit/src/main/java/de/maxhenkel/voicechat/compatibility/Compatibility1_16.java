package de.maxhenkel.voicechat.compatibility;

import de.maxhenkel.voicechat.BukkitVersion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import java.util.UUID;

public class Compatibility1_16 extends BaseCompatibility {

    public static final BukkitVersion VERSION_1_16_5 = BukkitVersion.parseBukkitVersion("1.16.5-R0.1");
    public static final BukkitVersion VERSION_1_16_4 = BukkitVersion.parseBukkitVersion("1.16.4-R0.1");
    public static final BukkitVersion VERSION_1_16_3 = BukkitVersion.parseBukkitVersion("1.16.3-R0.1");
    public static final BukkitVersion VERSION_1_16_2 = BukkitVersion.parseBukkitVersion("1.16.2-R0.1");
    public static final BukkitVersion VERSION_1_16_1 = BukkitVersion.parseBukkitVersion("1.16.1-R0.1");
    public static final BukkitVersion VERSION_1_16 = BukkitVersion.parseBukkitVersion("1.16-R0.1");

    public static final Compatibility1_16 INSTANCE = new Compatibility1_16();

    @Override
    public String getServerIp(Server server) throws Exception {
        Object dedicatedServer = callMethod(server, "getServer");
        Object dedicatedServerProperties = callMethod(dedicatedServer, "getDedicatedServerProperties");
        return getField(dedicatedServerProperties, "serverIp");
    }

    @Override
    public void sendMessage(Player player, Component component) {
        Class<?> chatMessageTypeClass = getClass(
                "net.minecraft.server.v1_16_R3.ChatMessageType",
                "net.minecraft.server.v1_16_R2.ChatMessageType",
                "net.minecraft.server.v1_16_R1.ChatMessageType"
        );
        Object b = getField(chatMessageTypeClass, "CHAT");
        send(player, component, b);
    }

    @Override
    public void sendStatusMessage(Player player, Component component) {
        Class<?> chatMessageTypeClass = getClass(
                "net.minecraft.server.v1_16_R3.ChatMessageType",
                "net.minecraft.server.v1_16_R2.ChatMessageType",
                "net.minecraft.server.v1_16_R1.ChatMessageType"
        );
        Object b = getField(chatMessageTypeClass, "GAME_INFO");
        send(player, component, b);
    }

    private static final UUID NUL_UUID = new UUID(0L, 0L);

    private void send(Player player, Component component, Object chatMessageType) {
        String json = GsonComponentSerializer.gson().serialize(component);

        Object entityPlayer = callMethod(player, "getHandle");
        Object playerConnection = getField(entityPlayer, "playerConnection");
        Class<?> packet = getClass(
                "net.minecraft.server.v1_16_R3.Packet",
                "net.minecraft.server.v1_16_R2.Packet",
                "net.minecraft.server.v1_16_R1.Packet"
        );
        Class<?> craftChatMessage = getClass(
                "org.bukkit.craftbukkit.v1_16_R3.util.CraftChatMessage",
                "org.bukkit.craftbukkit.v1_16_R2.util.CraftChatMessage",
                "org.bukkit.craftbukkit.v1_16_R1.util.CraftChatMessage"
        );

        Class<?> iChatBaseComponentClass = getClass(
                "net.minecraft.server.v1_16_R3.IChatBaseComponent",
                "net.minecraft.server.v1_16_R2.IChatBaseComponent",
                "net.minecraft.server.v1_16_R1.IChatBaseComponent"
        );
        Object iChatBaseComponent = callMethod(craftChatMessage, "fromJSON", new Class[]{String.class}, json);

        Class<?> packetPlayOutChatClass = getClass(
                "net.minecraft.server.v1_16_R3.PacketPlayOutChat",
                "net.minecraft.server.v1_16_R2.PacketPlayOutChat",
                "net.minecraft.server.v1_16_R1.PacketPlayOutChat"
        );

        Class<?> chatMessageTypeClass = getClass(
                "net.minecraft.server.v1_16_R3.ChatMessageType",
                "net.minecraft.server.v1_16_R2.ChatMessageType",
                "net.minecraft.server.v1_16_R1.ChatMessageType"
        );

        Object clientboundSystemChatPacket = callConstructor(packetPlayOutChatClass, new Class[]{iChatBaseComponentClass, chatMessageTypeClass, UUID.class}, iChatBaseComponent, chatMessageType, NUL_UUID);

        callMethod(playerConnection, "sendPacket", new Class[]{packet}, clientboundSystemChatPacket);
    }

}
