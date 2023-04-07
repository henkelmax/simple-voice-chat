package de.maxhenkel.voicechat.compatibility;

import com.mojang.brigadier.arguments.ArgumentType;
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
        Object chat = getField(chatMessageTypeClass, "CHAT");
        send(player, component, chat);
    }

    @Override
    public void sendStatusMessage(Player player, Component component) {
        Class<?> chatMessageTypeClass = getClass(
                "net.minecraft.server.v1_16_R3.ChatMessageType",
                "net.minecraft.server.v1_16_R2.ChatMessageType",
                "net.minecraft.server.v1_16_R1.ChatMessageType"
        );
        Object gameInfo = getField(chatMessageTypeClass, "GAME_INFO");
        send(player, component, gameInfo);
    }

    @Override
    public ArgumentType<?> playerArgument() {
        Class<?> argumentEntity = getClass(
                "net.minecraft.server.v1_16_R3.ArgumentEntity",
                "net.minecraft.server.v1_16_R2.ArgumentEntity",
                "net.minecraft.server.v1_16_R1.ArgumentEntity"
        );
        return callMethod(argumentEntity, "c");
    }

    @Override
    public ArgumentType<?> uuidArgument() {
        Class<?> argumentEntity = getClass(
                "net.minecraft.server.v1_16_R3.ArgumentUUID",
                "net.minecraft.server.v1_16_R2.ArgumentUUID",
                "net.minecraft.server.v1_16_R1.ArgumentUUID"
        );
        return callMethod(argumentEntity, "a");
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
        Class<?> chatSerializer = getClass(
                "net.minecraft.server.v1_16_R3.IChatBaseComponent$ChatSerializer",
                "net.minecraft.server.v1_16_R2.IChatBaseComponent$ChatSerializer",
                "net.minecraft.server.v1_16_R1.IChatBaseComponent$ChatSerializer"
        );

        Class<?> iChatBaseComponentClass = getClass(
                "net.minecraft.server.v1_16_R3.IChatBaseComponent",
                "net.minecraft.server.v1_16_R2.IChatBaseComponent",
                "net.minecraft.server.v1_16_R1.IChatBaseComponent"
        );

        Object iChatBaseComponent = callMethod(chatSerializer, "a", new Class[]{String.class}, json);

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
