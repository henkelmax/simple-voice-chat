package de.maxhenkel.voicechat.command;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.net.NetManager;
import de.maxhenkel.voicechat.net.SetGroupPacket;
import de.maxhenkel.voicechat.voice.common.PingPacket;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import de.maxhenkel.voicechat.voice.server.ClientConnection;
import de.maxhenkel.voicechat.voice.server.PingManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class VoiceChatCommands implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        System.out.println(Arrays.toString(args));
        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("test")) {
                return testCommand(commandSender, command, label, args);
            } else if (args[0].equalsIgnoreCase("invite")) {
                return inviteCommand(commandSender, command, label, args);
            } else if (args[0].equalsIgnoreCase("join")) {
                return joinCommand(commandSender, command, label, args);
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
            commandSender.sendMessage(Component.translatable("argument.entity.notfound.player"));
            return true;
        }

        ClientConnection clientConnection = Voicechat.SERVER.getServer().getConnections().get(player.getUniqueId());

        if (clientConnection == null) {
            commandSender.sendMessage(Component.text("Client not connected to voice chat"));
            return true;
        }

        try {
            commandSender.sendMessage(Component.text("Client not connected to voice chat"));
            long timestamp = System.currentTimeMillis();
            Voicechat.SERVER.getServer().getPingManager().sendPing(clientConnection, 5000, new PingManager.PingListener() {
                @Override
                public void onPong(PingPacket packet) {
                    commandSender.sendMessage(Component.text("Received packet in " + (System.currentTimeMillis() - timestamp) + "ms"));
                }

                @Override
                public void onTimeout() {
                    commandSender.sendMessage(Component.text("Request timed out"));
                }
            });
            commandSender.sendMessage(Component.text("Packet sent. Waiting for response..."));
        } catch (Exception e) {
            commandSender.sendMessage(Component.text("Failed to send packet: " + e.getMessage()));
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
            commandSender.sendMessage(Component.translatable("argument.entity.notfound.player"));
            return true;
        }

        PlayerState state = Voicechat.SERVER.getServer().getPlayerStateManager().getState(player.getUniqueId());
        PlayerState otherState = Voicechat.SERVER.getServer().getPlayerStateManager().getState(otherPlayer.getUniqueId());

        if (otherState == null) {
            commandSender.sendMessage(Component.text("Player not connected to voice chat"));
            return true;
        }
        if (state == null) {
            commandSender.sendMessage(Component.text("You are not connected to the voice chat"));
        }
        if (!state.hasGroup()) {
            commandSender.sendMessage(Component.translatable("message.voicechat.not_in_group"));
            return true;
        }
        otherPlayer.sendMessage(Component.translatable("message.voicechat.invite",
                Component.text(player.getName()),
                Component.text(state.getGroup()).toBuilder().color(NamedTextColor.GRAY).asComponent(),
                Component.text("[").toBuilder().append(
                        Component.translatable("message.voicechat.accept_invite")
                                .toBuilder()
                                .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/voicechat join \"" + state.getGroup() + "\""))
                                .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("message.voicechat.accept_invite.hover")))
                                .color(NamedTextColor.GREEN)
                                .build()
                ).append(Component.text("]")).color(NamedTextColor.GREEN).asComponent()
        ));
        player.sendMessage(Component.translatable("message.voicechat.invite_successful", Component.text(otherPlayer.getName())));

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
            commandSender.sendMessage(Component.text("You are not connected to the voice chat"));
            return true;
        }

        if (!Voicechat.SERVER_CONFIG.groupsEnabled.get()) {
            commandSender.sendMessage(Component.translatable("message.voicechat.groups_disabled"));
            return true;
        }

        String groupName = args[1];

        if (groupName.startsWith("\"")) {
            groupName = groupName.replaceFirst("\"", "");
            for (int i = 2; i < args.length; i++) {
                String str = args[i];
                if (str.contains("\"")) {
                    groupName += " " + str.split("\"")[0];
                    break;
                } else {
                    groupName += " " + str;
                }
            }
        }

        if (groupName.length() > 16) {
            commandSender.sendMessage(Component.translatable("message.voicechat.group_name_too_long"));
            return true;
        }

        if (!Voicechat.GROUP_REGEX.matcher(groupName).matches()) {
            commandSender.sendMessage(Component.translatable("message.voicechat.invalid_group_name"));
            return true;
        }

        NetManager.sendToClient(player, new SetGroupPacket(groupName));
        commandSender.sendMessage(Component.translatable("message.voicechat.join_successful", Component.text(groupName).toBuilder().color(NamedTextColor.GREEN).asComponent()));
        return true;
    }
}