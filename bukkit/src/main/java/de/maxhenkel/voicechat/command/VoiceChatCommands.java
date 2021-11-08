package de.maxhenkel.voicechat.command;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.net.NetManager;
import de.maxhenkel.voicechat.voice.common.PingPacket;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import de.maxhenkel.voicechat.voice.server.ClientConnection;
import de.maxhenkel.voicechat.voice.server.Group;
import de.maxhenkel.voicechat.voice.server.PingManager;
import de.maxhenkel.voicechat.voice.server.Server;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import javax.annotation.Nullable;
import java.util.UUID;

public class VoiceChatCommands implements CommandExecutor {

    public static Permission CONNECT_PERMISSION = new Permission("voicechat.connect", PermissionDefault.TRUE);
    public static Permission SPEAK_PERMISSION = new Permission("voicechat.speak", PermissionDefault.TRUE);
    public static Permission GROUPS_PERMISSION = new Permission("voicechat.groups", PermissionDefault.TRUE);
    public static Permission ADMIN_PERMISSION = new Permission("voicechat.admin", PermissionDefault.OP);

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("test")) {
                if (commandSender.hasPermission(ADMIN_PERMISSION)) {
                    return testCommand(commandSender, command, label, args);
                }
            } else if (args[0].equalsIgnoreCase("invite")) {
                if (commandSender.hasPermission(GROUPS_PERMISSION)) {
                    return inviteCommand(commandSender, command, label, args);
                }
            } else if (args[0].equalsIgnoreCase("join")) {
                if (commandSender.hasPermission(GROUPS_PERMISSION)) {
                    return joinCommand(commandSender, command, label, args);
                }
            } else if (args[0].equalsIgnoreCase("leave")) {
                if (commandSender.hasPermission(GROUPS_PERMISSION)) {
                    return leaveCommand(commandSender, command, label, args);
                }
            }
        }
        return false;
    }

    private boolean testCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (args.length < 2) {
            return false;
        }

        Player player = commandSender.getServer().getPlayer(args[1]);

        if (player == null) {
            commandSender.sendMessage(Voicechat.translate("player_not_found"));
            return true;
        }

        ClientConnection clientConnection = Voicechat.SERVER.getServer().getConnections().get(player.getUniqueId());

        if (clientConnection == null) {
            commandSender.sendMessage(Voicechat.translate("client_not_connected"));
            return true;
        }

        try {
            commandSender.sendMessage(Voicechat.translate("sending_packet"));
            long timestamp = System.currentTimeMillis();
            Voicechat.SERVER.getServer().getPingManager().sendPing(clientConnection, 5000, new PingManager.PingListener() {
                @Override
                public void onPong(PingPacket packet) {
                    commandSender.sendMessage(String.format(Voicechat.translate("received_packet"), (System.currentTimeMillis() - timestamp)));
                }

                @Override
                public void onTimeout() {
                    commandSender.sendMessage(Voicechat.translate("request_timed_out"));
                }
            });
            commandSender.sendMessage(Voicechat.translate("packet_sent"));
        } catch (Exception e) {
            commandSender.sendMessage(String.format(Voicechat.translate("failed_to_send_packet"), e.getMessage()));
            e.printStackTrace();
        }
        return true;
    }

    private boolean inviteCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (!(commandSender instanceof Player)) {
            return false;
        }

        if (args.length < 2) {
            return false;
        }

        Player player = (Player) commandSender;

        Player otherPlayer = commandSender.getServer().getPlayer(args[1]);

        if (otherPlayer == null) {
            commandSender.sendMessage(Voicechat.translate("player_not_found"));
            return true;
        }

        PlayerState state = Voicechat.SERVER.getServer().getPlayerStateManager().getState(player.getUniqueId());
        PlayerState otherState = Voicechat.SERVER.getServer().getPlayerStateManager().getState(otherPlayer.getUniqueId());

        if (otherState == null) {
            commandSender.sendMessage(Voicechat.translate("player_not_connected"));
            return true;
        }
        if (state == null) {
            commandSender.sendMessage(Voicechat.translate("not_connected"));
            return true;
        }
        if (!state.hasGroup()) {
            NetManager.sendMessage(player, Component.translatable("message.voicechat.not_in_group"));
            return true;
        }
        Group group = Voicechat.SERVER.getServer().getGroupManager().getGroup(state.getGroup().getId());
        if (group == null) {
            return true;
        }
        String passwordSuffix = group.getPassword() == null ? "" : " \"" + group.getPassword() + "\"";
        NetManager.sendMessage(otherPlayer, Component.translatable("message.voicechat.invite",
                Component.text(player.getName()),
                Component.text(group.getName()).toBuilder().color(NamedTextColor.GRAY).asComponent(),
                Component.text("[").toBuilder().append(
                        Component.translatable("message.voicechat.accept_invite")
                                .toBuilder()
                                .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/voicechat join " + group.getId().toString() + passwordSuffix))
                                .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("message.voicechat.accept_invite.hover")))
                                .color(NamedTextColor.GREEN)
                                .build()
                ).append(Component.text("]")).color(NamedTextColor.GREEN).asComponent()
        ));
        NetManager.sendMessage(player, Component.translatable("message.voicechat.invite_successful", Component.text(otherPlayer.getName())));

        return true;
    }

    private boolean joinCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (!(commandSender instanceof Player)) {
            return false;
        }

        if (args.length < 2) {
            return false;
        }

        Player player = (Player) commandSender;
        PlayerState state = Voicechat.SERVER.getServer().getPlayerStateManager().getState(player.getUniqueId());

        if (state == null) {
            commandSender.sendMessage(Voicechat.translate("not_connected"));
            return true;
        }

        if (!Voicechat.SERVER_CONFIG.groupsEnabled.get()) {
            NetManager.sendMessage(player, Component.translatable("message.voicechat.groups_disabled"));
            return true;
        }

        UUID groupUUID;
        try {
            groupUUID = UUID.fromString(args[1]);
        } catch (Exception e) {
            NetManager.sendMessage(player, Component.translatable("message.voicechat.group_does_not_exist"));
            return true;
        }

        String password = null;
        if (args.length >= 3) {
            StringBuilder sb = new StringBuilder();
            for (int i = 2; i < args.length; i++) {
                sb.append(args[i]);
                sb.append(" ");
            }
            password = sb.toString().trim();
            if (password.startsWith("\"")) {
                String[] split = password.split("\"");
                if (split.length > 1) {
                    password = split[1];
                }
            }
        }

        joinGroup(player, groupUUID, password);
        return true;
    }

    private static int joinGroup(Player commandSender, UUID groupID, @Nullable String password) {
        if (!Voicechat.SERVER_CONFIG.groupsEnabled.get()) {
            NetManager.sendMessage(commandSender, Component.translatable("message.voicechat.groups_disabled"));
            return 1;
        }

        Server server = Voicechat.SERVER.getServer();

        Group group = server.getGroupManager().getGroup(groupID);

        if (group == null) {
            NetManager.sendMessage(commandSender, Component.translatable("message.voicechat.group_does_not_exist"));
            return 1;
        }

        server.getGroupManager().joinGroup(group, commandSender, password);
        NetManager.sendMessage(commandSender, Component.translatable("message.voicechat.join_successful", Component.text(group.getName()).toBuilder().color(NamedTextColor.GREEN).asComponent()));
        return 1;
    }

    private boolean leaveCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (!(commandSender instanceof Player)) {
            return false;
        }

        Player player = (Player) commandSender;
        PlayerState state = Voicechat.SERVER.getServer().getPlayerStateManager().getState(player.getUniqueId());

        if (state == null) {
            commandSender.sendMessage(Voicechat.translate("not_connected"));
            return true;
        }

        if (!Voicechat.SERVER_CONFIG.groupsEnabled.get()) {
            NetManager.sendMessage(player, Component.translatable("message.voicechat.groups_disabled"));
            return true;
        }

        if (!state.hasGroup()) {
            NetManager.sendMessage(player, Component.translatable("message.voicechat.not_in_group"));
            return true;
        }
        Server server = Voicechat.SERVER.getServer();
        server.getGroupManager().leaveGroup(player);
        NetManager.sendMessage(player, Component.translatable("message.voicechat.leave_successful"));
        return true;
    }
}