package de.maxhenkel.voicechat.compatibility;

import com.mojang.brigadier.arguments.ArgumentType;
import de.maxhenkel.voicechat.BukkitVersion;
import de.maxhenkel.voicechat.util.Key;
import org.bukkit.entity.Player;

import static de.maxhenkel.voicechat.compatibility.ReflectionUtils.*;

public class Compatibility1_12 extends JsonMessageBaseCompatibility {

    public static final String CHANNEL = "vc";

    public static final BukkitVersion VERSION_1_12_2 = BukkitVersion.parseBukkitVersion("1.12.2-R0.1");
    public static final BukkitVersion VERSION_1_12_1 = BukkitVersion.parseBukkitVersion("1.12.1-R0.1");
    public static final BukkitVersion VERSION_1_12 = BukkitVersion.parseBukkitVersion("1.12-R0.1");

    public static final Compatibility1_12 INSTANCE = new Compatibility1_12();

    @Override
    public Key createNamespacedKey(String key) {
        return Key.of(CHANNEL, key);
    }

    @Override
    public void sendJsonMessage(Player player, String json) {
        Class<?> chatMessageTypeClass = getServerClass("ChatMessageType");
        Object b = getField(chatMessageTypeClass, "CHAT");
        send(player, json, b);
    }

    @Override
    public void sendJsonStatusMessage(Player player, String json) {
        Class<?> chatMessageTypeClass = getServerClass("ChatMessageType");
        Object b = getField(chatMessageTypeClass, "GAME_INFO");
        send(player, json, b);
    }

    @Override
    public String createTranslationMessage(String key, String... args) {
        return Compatibility1_8.constructTranslationMessage(key, args);
    }

    @Override
    public void sendInviteMessage(Player player, Player commandSender, String groupName, String joinCommand) {
        sendJsonMessage(player, Compatibility1_8.constructInviteMessage(commandSender, groupName, joinCommand));
    }

    @Override
    public void sendIncompatibleMessage(Player player, String pluginVersion, String pluginName) {
        sendJsonMessage(player, Compatibility1_8.constructIncompatibleMessage(pluginVersion, pluginName));
    }

    @Override
    public ArgumentType<?> playerArgument() {
        return null;
    }

    @Override
    public ArgumentType<?> uuidArgument() {
        return null;
    }

    private void send(Player player, String json, Object chatMessageType) {
        Object entityPlayer = callMethod(player, "getHandle");
        Object playerConnection = getField(entityPlayer, "playerConnection");
        Class<?> packet = getServerClass("Packet");
        Class<?> chatSerializer = getServerClass("IChatBaseComponent$ChatSerializer");

        Class<?> iChatBaseComponentClass = getServerClass("IChatBaseComponent");
        Object iChatBaseComponent = callMethod(chatSerializer, "a", new Class[]{String.class}, json);

        Class<?> packetPlayOutChatClass = getServerClass("PacketPlayOutChat");

        Class<?> chatMessageTypeClass = getServerClass("ChatMessageType");

        Object clientboundSystemChatPacket = callConstructor(packetPlayOutChatClass, new Class[]{iChatBaseComponentClass, chatMessageTypeClass}, iChatBaseComponent, chatMessageType);

        callMethod(playerConnection, "sendPacket", new Class[]{packet}, clientboundSystemChatPacket);
    }

}
