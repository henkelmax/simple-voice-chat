package de.maxhenkel.voicechat.command;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.net.NetManager;
import de.maxhenkel.voicechat.permission.PermissionManager;
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
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class VoiceChatCommands implements CommandExecutor, TabCompleter {

    public static final String VOICECHAT_COMMAND = "voicechat";

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (checkNoVoicechat(commandSender)) {
            return true;
        }
        if (!(commandSender instanceof Player)) {
            return true;
        }
        Player sender = (Player) commandSender;

        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("help")) {
                return helpCommand(sender, command, label, args);
            } else if (args[0].equalsIgnoreCase("test")) {
                if (commandSender.hasPermission(PermissionManager.ADMIN_PERMISSION)) {
                    return testCommand(sender, command, label, args);
                } else {
                    return true;
                }
            } else if (args[0].equalsIgnoreCase("invite")) {
                return inviteCommand(sender, command, label, args);
            } else if (args[0].equalsIgnoreCase("join")) {
                return joinCommand(sender, command, label, args);
            } else if (args[0].equalsIgnoreCase("leave")) {
                return leaveCommand(sender, command, label, args);
            }
        }
        return helpCommand(sender, command, label, args);
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return tabCompleteList(args[0], Arrays.asList("help", "test", "invite", "join", "leave"));
        }

        if (args.length == 2) {
            String arg = args[0];
            if (arg.equals("test") || arg.equals("invite")) {
                return null;
            } else if (arg.startsWith("join")) {
                return Collections.emptyList();
            }
        }

        if (args.length == 3) {
            String arg = args[0];
            if (arg.startsWith("join")) {
                return Collections.emptyList();
            }
        }

        return Collections.emptyList();
    }

    private List<String> tabCompleteList(String arg, List<String> list) {
        return list.stream().filter(s -> s.startsWith(arg)).collect(Collectors.toList());
    }

    private boolean helpCommand(Player commandSender, Command command, String label, String[] args) {
        commandSender.sendMessage("/voicechat [help]");
        commandSender.sendMessage("/voicechat [test] <target>");
        commandSender.sendMessage("/voicechat [invite] <target>");
        commandSender.sendMessage("/voicechat [join] <group> [<password>]");
        commandSender.sendMessage("/voicechat [leave]");
        return true;
    }

    private boolean testCommand(Player commandSender, Command command, String label, String[] args) {
        if (args.length < 2) {
            return false;
        }

        Player player = parsePlayer(commandSender, args[1]);

        if (player == null) {
            NetManager.sendMessage(commandSender, Component.translatable("argument.entity.notfound.player"));
            return true;
        }

        if (!Voicechat.SERVER.isCompatible(player)) {
            NetManager.sendMessage(commandSender, Component.translatable("message.voicechat.player_no_voicechat", Component.text(player.getDisplayName()), Component.text("Simple Voice Chat")));
            return true;
        }

        ClientConnection clientConnection = Voicechat.SERVER.getServer().getConnections().get(player.getUniqueId());

        if (clientConnection == null) {
            NetManager.sendMessage(commandSender, Component.translatable("message.voicechat.client_not_connected"));
            return true;
        }

        try {
            NetManager.sendMessage(commandSender, Component.translatable("message.voicechat.sending_ping"));
            Voicechat.SERVER.getServer().getPingManager().sendPing(clientConnection, 500, 10, new PingManager.PingListener() {

                @Override
                public void onPong(int attempts, long pingMilliseconds) {
                    if (attempts <= 1) {
                        NetManager.sendMessage(commandSender, Component.translatable("message.voicechat.ping_received", Component.text(pingMilliseconds)));
                    } else {
                        NetManager.sendMessage(commandSender, Component.translatable("message.voicechat.ping_received_attempt", Component.text(pingMilliseconds), Component.text(attempts)));
                    }
                }

                @Override
                public void onFailedAttempt(int attempts) {
                    NetManager.sendMessage(commandSender, Component.translatable("message.voicechat.ping_retry"));
                }

                @Override
                public void onTimeout(int attempts) {
                    NetManager.sendMessage(commandSender, Component.translatable("message.voicechat.ping_timed_out", Component.text(attempts)));
                }
            });
            NetManager.sendMessage(commandSender, Component.translatable("message.voicechat.ping_sent_waiting"));
        } catch (Exception e) {
            NetManager.sendMessage(commandSender, Component.translatable("message.voicechat.failed_to_send_ping", Component.text(e.getMessage())));
            Voicechat.LOGGER.warn("Failed to send ping", e);
        }
        return true;
    }

    private boolean inviteCommand(Player commandSender, Command command, String label, String[] args) {
        if (args.length < 2) {
            return false;
        }

        Player player = parsePlayer(commandSender, args[1]);

        if (player == null) {
            NetManager.sendMessage(commandSender, Component.translatable("argument.entity.notfound.player"));
            return true;
        }

        PlayerState state = Voicechat.SERVER.getServer().getPlayerStateManager().getState(commandSender.getUniqueId());

        if (state == null || !state.hasGroup()) {
            NetManager.sendMessage(commandSender, Component.translatable("message.voicechat.not_in_group"));
            return true;
        }

        Group group = Voicechat.SERVER.getServer().getGroupManager().getGroup(state.getGroup());
        if (group == null) {
            return true;
        }

        if (!Voicechat.SERVER.isCompatible(player)) {
            NetManager.sendMessage(commandSender, Component.translatable("message.voicechat.player_no_voicechat", Component.text(player.getDisplayName()), Component.text("Simple Voice Chat")));
            return true;
        }

        String passwordSuffix = group.getPassword() == null ? "" : " \"" + group.getPassword() + "\"";
        NetManager.sendMessage(player, Component.translatable("message.voicechat.invite",
                Component.text(commandSender.getName()),
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
        NetManager.sendMessage(commandSender, Component.translatable("message.voicechat.invite_successful", Component.text(player.getName())));

        return true;
    }

    private boolean joinCommand(Player commandSender, Command command, String label, String[] args) {
        if (args.length < 2) {
            return false;
        }

        int argIndex = 1;
        UUID groupUUID;
        try {
            groupUUID = UUID.fromString(args[argIndex]);
        } catch (Exception e) {
            String groupName;
            if (args[argIndex].startsWith("\"")) {
                StringBuilder sb = new StringBuilder();
                for (; argIndex < args.length; argIndex++) {
                    sb.append(args[argIndex]).append(" ");
                    if (args[argIndex].endsWith("\"") && !args[argIndex].endsWith("\\\"")) {
                        break;
                    }
                }
                groupName = sb.toString().trim();
                String[] split = groupName.split("\"");
                if (split.length > 1) {
                    groupName = split[1];
                }
            } else {
                groupName = args[argIndex];
            }
            groupUUID = getGroupUUID(commandSender, Voicechat.SERVER.getServer(), groupName);
        }

        if (groupUUID == null) {
            return true;
        }

        argIndex++;

        String password = null;
        if (args.length >= argIndex + 1) {
            StringBuilder sb = new StringBuilder();
            for (; argIndex < args.length; argIndex++) {
                sb.append(args[argIndex]).append(" ");
            }
            password = sb.toString().trim();
            if (password.startsWith("\"")) {
                String[] split = password.split("\"");
                if (split.length > 1) {
                    password = split[1];
                }
            }
        }

        joinGroup(commandSender, groupUUID, password);
        return true;
    }

    private UUID getGroupUUID(Player commandSender, Server server, String groupName) {
        List<Group> groups = server.getGroupManager().getGroups().values().stream().filter(group -> group.getName().equals(groupName)).collect(Collectors.toList());

        if (groups.isEmpty()) {
            NetManager.sendMessage(commandSender, Component.translatable("message.voicechat.group_does_not_exist"));
            return null;
        }

        if (groups.size() > 1) {
            NetManager.sendMessage(commandSender, Component.translatable("message.voicechat.group_name_not_unique"));
            return null;
        }

        return groups.get(0).getId();
    }

    private static void joinGroup(Player commandSender, UUID groupID, @Nullable String password) {
        if (!Voicechat.SERVER_CONFIG.groupsEnabled.get()) {
            NetManager.sendMessage(commandSender, Component.translatable("message.voicechat.groups_disabled"));
            return;
        }

        Server server = Voicechat.SERVER.getServer();

        if (!commandSender.hasPermission(PermissionManager.GROUPS_PERMISSION)) {
            NetManager.sendMessage(commandSender, Component.translatable("message.voicechat.no_group_permission"));
            return;
        }

        Group group = server.getGroupManager().getGroup(groupID);

        if (group == null) {
            NetManager.sendMessage(commandSender, Component.translatable("message.voicechat.group_does_not_exist"));
            return;
        }

        server.getGroupManager().joinGroup(group, commandSender, password);
        NetManager.sendMessage(commandSender, Component.translatable("message.voicechat.join_successful", Component.text(group.getName()).toBuilder().color(NamedTextColor.GREEN).asComponent()));
    }

    private boolean leaveCommand(Player commandSender, Command command, String label, String[] args) {
        if (!Voicechat.SERVER_CONFIG.groupsEnabled.get()) {
            NetManager.sendMessage(commandSender, Component.translatable("message.voicechat.groups_disabled"));
            return true;
        }

        Server server = Voicechat.SERVER.getServer();
        PlayerState state = server.getPlayerStateManager().getState(commandSender.getUniqueId());
        if (state == null || !state.hasGroup()) {
            NetManager.sendMessage(commandSender, Component.translatable("message.voicechat.not_in_group"));
            return true;
        }

        server.getGroupManager().leaveGroup(commandSender);
        NetManager.sendMessage(commandSender, Component.translatable("message.voicechat.leave_successful"));
        return true;
    }

    private static boolean checkNoVoicechat(CommandSender commandSender) {
        if (commandSender instanceof Player) {
            if (Voicechat.SERVER.isCompatible((Player) commandSender)) {
                return false;
            }
            commandSender.sendMessage(String.format(Voicechat.TRANSLATIONS.voicechatNeededForCommandMessage.get(), "Simple Voice Chat"));
        } else {
            commandSender.sendMessage(Voicechat.TRANSLATIONS.playerCommandMessage.get());
        }
        return true;
    }

    @Nullable
    public static Player parsePlayer(CommandSender commandSender, String playerArg) {
        Player player = commandSender.getServer().getPlayer(playerArg);
        if (player != null) {
            return player;
        }
        try {
            UUID uuid = UUID.fromString(playerArg);
            return commandSender.getServer().getPlayer(uuid);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}