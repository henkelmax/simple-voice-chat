package de.maxhenkel.voicechat.compatibility;

import com.mojang.brigadier.arguments.ArgumentType;
import de.maxhenkel.voicechat.BukkitVersion;
import org.bukkit.entity.Player;

import java.util.UUID;

import static de.maxhenkel.voicechat.compatibility.ReflectionUtils.*;

public class Compatibility1_18 extends JsonMessageBaseCompatibility {

    public static final BukkitVersion VERSION_1_18_2 = BukkitVersion.parseBukkitVersion("1.18.2-R0.1");
    public static final BukkitVersion VERSION_1_18_1 = BukkitVersion.parseBukkitVersion("1.18.1-R0.1");
    public static final BukkitVersion VERSION_1_18 = BukkitVersion.parseBukkitVersion("1.18-R0.1");

    public static final Compatibility1_18 INSTANCE = new Compatibility1_18();

    @Override
    public void sendJsonMessage(Player player, String json) {
        Class<?> chatMessageTypeClass = getClazz("net.minecraft.network.chat.ChatMessageType");
        Object b = getField(chatMessageTypeClass, "b");
        send(player, json, b);
    }

    @Override
    public void sendJsonStatusMessage(Player player, String json) {
        Class<?> chatMessageTypeClass = getClazz("net.minecraft.network.chat.ChatMessageType");
        Object b = getField(chatMessageTypeClass, "c");
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
        return Compatibility1_19.INSTANCE.playerArgument();
    }

    @Override
    public ArgumentType<?> uuidArgument() {
        return Compatibility1_19.INSTANCE.uuidArgument();
    }

    private static final UUID NUL_UUID = new UUID(0L, 0L);

    private void send(Player player, String json, Object chatMessageType) {
        Object entityPlayer = callMethod(player, "getHandle");
        Object playerConnection = getField(entityPlayer, "b");
        Class<?> packet = getClazz("net.minecraft.network.protocol.Packet");
        Class<?> craftChatMessage = getBukkitClass("util.CraftChatMessage");

        Class<?> iChatBaseComponentClass = getClazz("net.minecraft.network.chat.IChatBaseComponent");
        Object iChatBaseComponent = callMethod(craftChatMessage, "fromJSON", new Class[]{String.class}, json);

        Class<?> packetPlayOutChatClass = getClazz("net.minecraft.network.protocol.game.PacketPlayOutChat");

        Class<?> chatMessageTypeClass = getClazz("net.minecraft.network.chat.ChatMessageType");

        Object clientboundSystemChatPacket = callConstructor(packetPlayOutChatClass, new Class[]{iChatBaseComponentClass, chatMessageTypeClass, UUID.class}, iChatBaseComponent, chatMessageType, NUL_UUID);

        callMethod(playerConnection, "a", new Class[]{packet}, clientboundSystemChatPacket);
    }

}
