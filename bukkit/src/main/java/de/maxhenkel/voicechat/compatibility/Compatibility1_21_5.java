package de.maxhenkel.voicechat.compatibility;

import de.maxhenkel.voicechat.BukkitVersion;
import org.bukkit.entity.Player;
import org.json.JSONArray;
import org.json.JSONObject;

public class Compatibility1_21_5 extends Compatibility1_20_3 {

    public static final BukkitVersion VERSION_1_21_5 = BukkitVersion.parseBukkitVersion("1.21.5-R0.1");
    public static final BukkitVersion VERSION_1_21_6 = BukkitVersion.parseBukkitVersion("1.21.6-R0.1");
    public static final BukkitVersion VERSION_1_21_7 = BukkitVersion.parseBukkitVersion("1.21.7-R0.1");
    public static final BukkitVersion VERSION_1_21_8 = BukkitVersion.parseBukkitVersion("1.21.8-R0.1");
    public static final BukkitVersion VERSION_1_21_9 = BukkitVersion.parseBukkitVersion("1.21.9-R0.1");
    public static final BukkitVersion VERSION_1_21_10 = BukkitVersion.parseBukkitVersion("1.21.10-R0.1");
    public static final BukkitVersion VERSION_1_21_11 = BukkitVersion.parseBukkitVersion("1.21.11-R0.1");

    public static final Compatibility1_21_5 INSTANCE = new Compatibility1_21_5();

    @Override
    public void sendInviteMessage(Player player, Player commandSender, String groupName, String joinCommand) {
        sendJsonMessage(player, constructInviteMessage(commandSender, groupName, joinCommand));
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
        acceptInBrackets.put("translate", "chat.square_brackets");
        acceptInBrackets.put("color", "green");
        JSONArray withBrackets = new JSONArray();

        JSONObject accept = new JSONObject();
        accept.put("translate", "message.voicechat.accept_invite");

        JSONObject clickEvent = new JSONObject();
        clickEvent.put("action", "run_command");
        clickEvent.put("command", joinCommand);
        accept.put("click_event", clickEvent);

        JSONObject hoverEvent = new JSONObject();
        hoverEvent.put("action", "show_text");
        JSONObject contents = new JSONObject();
        contents.put("translate", "message.voicechat.accept_invite.hover");
        hoverEvent.put("value", contents);
        accept.put("hover_event", hoverEvent);

        withBrackets.put(accept);

        acceptInBrackets.put("with", withBrackets);
        args.put(acceptInBrackets);

        msg.put("with", args);

        return msg.toString();
    }

}
