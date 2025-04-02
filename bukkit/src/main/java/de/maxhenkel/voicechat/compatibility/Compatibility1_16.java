package de.maxhenkel.voicechat.compatibility;

import com.mojang.brigadier.arguments.ArgumentType;
import de.maxhenkel.voicechat.BukkitVersion;
import org.bukkit.entity.Player;

import java.util.UUID;

import static de.maxhenkel.voicechat.compatibility.ReflectionUtils.*;

public class Compatibility1_16 extends JsonMessageBaseCompatibility {

    public static final BukkitVersion VERSION_1_16_5 = BukkitVersion.parseBukkitVersion("1.16.5-R0.1");
    public static final BukkitVersion VERSION_1_16_4 = BukkitVersion.parseBukkitVersion("1.16.4-R0.1");
    public static final BukkitVersion VERSION_1_16_3 = BukkitVersion.parseBukkitVersion("1.16.3-R0.1");
    public static final BukkitVersion VERSION_1_16_2 = BukkitVersion.parseBukkitVersion("1.16.2-R0.1");
    public static final BukkitVersion VERSION_1_16_1 = BukkitVersion.parseBukkitVersion("1.16.1-R0.1");
    public static final BukkitVersion VERSION_1_16 = BukkitVersion.parseBukkitVersion("1.16-R0.1");

    public static final Compatibility1_16 INSTANCE = new Compatibility1_16();

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
        Class<?> argumentEntity = getServerClass("ArgumentEntity");
        return callMethod(argumentEntity, "c");
    }

    @Override
    public ArgumentType<?> uuidArgument() {
        Class<?> argumentEntity = getServerClass("ArgumentUUID");
        return callMethod(argumentEntity, "a");
    }

    private static final UUID NUL_UUID = new UUID(0L, 0L);

    private void send(Player player, String json, Object chatMessageType) {
        Object entityPlayer = callMethod(player, "getHandle");
        Object playerConnection = getField(entityPlayer, "playerConnection");
        Class<?> packet = getServerClass("Packet");
        Class<?> chatSerializer = getServerClass("IChatBaseComponent$ChatSerializer");

        Class<?> iChatBaseComponentClass = getServerClass("IChatBaseComponent");

        Object iChatBaseComponent = callMethod(chatSerializer, "a", new Class[]{String.class}, json);

        Class<?> packetPlayOutChatClass = getServerClass("PacketPlayOutChat");

        Class<?> chatMessageTypeClass = getServerClass("ChatMessageType");

        Object clientboundSystemChatPacket = callConstructor(packetPlayOutChatClass, new Class[]{iChatBaseComponentClass, chatMessageTypeClass, UUID.class}, iChatBaseComponent, chatMessageType, NUL_UUID);

        callMethod(playerConnection, "sendPacket", new Class[]{packet}, clientboundSystemChatPacket);
    }

}
