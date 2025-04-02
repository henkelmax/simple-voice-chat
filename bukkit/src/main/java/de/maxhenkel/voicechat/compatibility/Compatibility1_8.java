package de.maxhenkel.voicechat.compatibility;

import com.mojang.brigadier.arguments.ArgumentType;
import de.maxhenkel.voicechat.BukkitVersion;
import de.maxhenkel.voicechat.util.Key;
import org.bukkit.entity.Player;
import org.json.JSONArray;
import org.json.JSONObject;

import static de.maxhenkel.voicechat.compatibility.ReflectionUtils.*;

public class Compatibility1_8 extends JsonMessageBaseCompatibility {

    public static final BukkitVersion VERSION_1_8_8 = BukkitVersion.parseBukkitVersion("1.8.8-R0.1");

    public static final Compatibility1_8 INSTANCE = new Compatibility1_8();

    @Override
    public Key createNamespacedKey(String key) {
        return Key.of(Compatibility1_12.CHANNEL, key);
    }

    @Override
    public void sendJsonMessage(Player player, String json) {
        send(player, json, (byte) 0);
    }

    @Override
    public void sendJsonStatusMessage(Player player, String json) {
        send(player, json, (byte) 2);
    }

    @Override
    public String createTranslationMessage(String key, String... args) {
        return constructTranslationMessage(key, args);
    }

    public static String constructTranslationMessage(String key, String... args) {
        JSONObject msg = new JSONObject();
        msg.put("translate", key);
        msg.put("with", args);
        return msg.toString();
    }

    @Override
    public void sendInviteMessage(Player player, Player commandSender, String groupName, String joinCommand) {
        sendJsonMessage(player, constructInviteMessage(commandSender, groupName, joinCommand));
    }

    @Override
    public void sendIncompatibleMessage(Player player, String pluginVersion, String pluginName) {
        sendJsonMessage(player, constructIncompatibleMessage(pluginVersion, pluginName));
    }

    public static String constructInviteMessage(Player commandSender, String groupName, String joinCommand) {
        JSONObject msg = new JSONObject();
        msg.put("translate", "message.voicechat.invite");

        JSONArray args = new JSONArray();

        args.put(commandSender.getName());

        JSONObject groupNameObj = new JSONObject();
        groupNameObj.put("color", "gray");
        groupNameObj.put("text", groupName);
        args.put(groupNameObj);

        JSONObject acceptInBrackets = new JSONObject();
        acceptInBrackets.put("text", "[");
        acceptInBrackets.put("color", "green");
        JSONArray extra = new JSONArray();

        JSONObject accept = new JSONObject();
        accept.put("translate", "message.voicechat.accept_invite");
        accept.put("color", "green");

        JSONObject clickEvent = new JSONObject();
        clickEvent.put("action", "run_command");
        clickEvent.put("value", joinCommand);
        accept.put("clickEvent", clickEvent);

        JSONObject hoverEvent = new JSONObject();
        hoverEvent.put("action", "show_text");
        JSONObject contents = new JSONObject();
        contents.put("translate", "message.voicechat.accept_invite.hover");
        hoverEvent.put("contents", contents);
        accept.put("hoverEvent", hoverEvent);

        extra.put(accept);
        extra.put("]");

        acceptInBrackets.put("extra", extra);
        args.put(acceptInBrackets);

        msg.put("with", args);

        return msg.toString();
    }

    public static String constructIncompatibleMessage(String pluginVersion, String pluginName) {
        JSONObject msg = new JSONObject();
        msg.put("translate", "message.voicechat.incompatible_version");
        JSONArray with = new JSONArray();

        JSONObject version = new JSONObject();
        version.put("text", pluginVersion);
        version.put("bold", true);
        with.put(version);

        JSONObject name = new JSONObject();
        name.put("text", pluginName);
        name.put("bold", true);
        with.put(name);

        msg.put("with", with);
        return msg.toString();
    }

    @Override
    public ArgumentType<?> playerArgument() {
        return null;
    }

    @Override
    public ArgumentType<?> uuidArgument() {
        return null;
    }

    private void send(Player player, String json, byte chatMessageType) {
        Object entityPlayer = callMethod(player, "getHandle");
        Object playerConnection = getField(entityPlayer, "playerConnection");
        Class<?> packet = getServerClass("Packet");
        Class<?> chatSerializer = getServerClass("IChatBaseComponent$ChatSerializer");

        Class<?> iChatBaseComponentClass = getServerClass("IChatBaseComponent");
        Object iChatBaseComponent = callMethod(chatSerializer, "a", new Class[]{String.class}, json);

        Class<?> packetPlayOutChatClass = getServerClass("PacketPlayOutChat");

        Object clientboundSystemChatPacket = callConstructor(packetPlayOutChatClass, new Class[]{iChatBaseComponentClass, byte.class}, iChatBaseComponent, chatMessageType);

        callMethod(playerConnection, "sendPacket", new Class[]{packet}, clientboundSystemChatPacket);
    }

}
