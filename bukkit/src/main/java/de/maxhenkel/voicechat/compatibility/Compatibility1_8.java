package de.maxhenkel.voicechat.compatibility;

import com.mojang.brigadier.arguments.ArgumentType;
import de.maxhenkel.voicechat.BukkitVersion;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Server;
import org.bukkit.entity.Player;

public class Compatibility1_8 extends BaseCompatibility {

    public static final BukkitVersion VERSION_1_8_8 = BukkitVersion.parseBukkitVersion("1.8.8-R0.1");

    public static final Compatibility1_8 INSTANCE = new Compatibility1_8();

    @Override
    public String getServerIp(Server server) throws Exception {
        Object minecraftServer = callMethod(server, "getServer");
        Class<?> minecraftServerClass = getClass("net.minecraft.server.v1_8_R3.MinecraftServer");
        return callMethod(minecraftServerClass, minecraftServer, "getServerIp");
    }

    @Override
    public Key createNamespacedKey(String key) {
        return Key.key(Compatibility1_12.CHANNEL, key);
    }

    @Override
    public void sendMessage(Player player, Component component) {
        send(player, component, (byte) 0);
    }

    @Override
    public void sendStatusMessage(Player player, Component component) {
        send(player, component, (byte) 2);
    }

    @Override
    public ArgumentType<?> playerArgument() {
        return null;
    }

    @Override
    public ArgumentType<?> uuidArgument() {
        return null;
    }

    private void send(Player player, Component component, byte chatMessageType) {
        String json = GsonComponentSerializer.gson().serialize(component);

        Object entityPlayer = callMethod(player, "getHandle");
        Object playerConnection = getField(entityPlayer, "playerConnection");
        Class<?> packet = getClass("net.minecraft.server.v1_8_R3.Packet");
        Class<?> chatSerializer = getClass("net.minecraft.server.v1_8_R3.IChatBaseComponent$ChatSerializer");

        Class<?> iChatBaseComponentClass = getClass("net.minecraft.server.v1_8_R3.IChatBaseComponent");
        Object iChatBaseComponent = callMethod(chatSerializer, "a", new Class[]{String.class}, json);

        Class<?> packetPlayOutChatClass = getClass("net.minecraft.server.v1_8_R3.PacketPlayOutChat");

        Object clientboundSystemChatPacket = callConstructor(packetPlayOutChatClass, new Class[]{iChatBaseComponentClass, byte.class}, iChatBaseComponent, chatMessageType);

        callMethod(playerConnection, "sendPacket", new Class[]{packet}, clientboundSystemChatPacket);
    }

}
