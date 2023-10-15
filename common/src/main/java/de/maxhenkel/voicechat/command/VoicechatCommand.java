package de.maxhenkel.voicechat.command;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import de.maxhenkel.voicechat.permission.PermissionManager;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import de.maxhenkel.voicechat.voice.server.ClientConnection;
import de.maxhenkel.voicechat.voice.server.Group;
import de.maxhenkel.voicechat.voice.server.PingManager;
import de.maxhenkel.voicechat.voice.server.Server;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class VoicechatCommand extends CommandBase {

    public static final String VOICECHAT_COMMAND = "voicechat";

    @Override
    public String getName() {
        return VOICECHAT_COMMAND;
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/voicechat <help|test|invite|join|leave>";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender commandSender, String[] args) throws CommandException {
        if (checkNoVoicechat(commandSender)) {
            return;
        }
        if (!(commandSender instanceof EntityPlayerMP)) {
            return;
        }

        EntityPlayerMP sender = (EntityPlayerMP) commandSender;

        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("help")) {
                helpCommand(sender, args);
                return;
            } else if (args[0].equalsIgnoreCase("test")) {
                if (PermissionManager.INSTANCE.ADMIN_PERMISSION.hasPermission(sender)) {
                    testCommand(sender, args);
                    return;
                }
            } else if (args[0].equalsIgnoreCase("invite")) {
                inviteCommand(sender, args);
                return;
            } else if (args[0].equalsIgnoreCase("join")) {
                joinCommand(sender, args);
                return;
            } else if (args[0].equalsIgnoreCase("leave")) {
                leaveCommand(sender);
                return;
            }
        }
        helpCommand(sender, args);
    }

    private boolean helpCommand(EntityPlayerMP commandSender, String[] args) {
        commandSender.sendMessage(new TextComponentString("/voicechat [help]"));
        commandSender.sendMessage(new TextComponentString("/voicechat [test] <target>"));
        commandSender.sendMessage(new TextComponentString("/voicechat [invite] <target>"));
        commandSender.sendMessage(new TextComponentString("/voicechat [join] <group> [<password>]"));
        commandSender.sendMessage(new TextComponentString("/voicechat [leave]"));
        return true;
    }

    private boolean testCommand(EntityPlayerMP commandSender, String[] args) {
        if (args.length < 2) {
            return false;
        }

        EntityPlayerMP player = commandSender.getServer().getPlayerList().getPlayerByUsername(args[1]);

        if (player == null) {
            commandSender.sendMessage(new TextComponentTranslation("commands.generic.player.notFound", args[1]));
            return true;
        }

        if (!Voicechat.SERVER.isCompatible(player)) {
            commandSender.sendMessage(new TextComponentTranslation("message.voicechat.player_no_voicechat", player.getDisplayName(), new TextComponentString(CommonCompatibilityManager.INSTANCE.getModName())));
            return true;
        }

        ClientConnection clientConnection = Voicechat.SERVER.getServer().getConnections().get(player.getUniqueID());

        if (clientConnection == null) {
            commandSender.sendMessage(new TextComponentTranslation("message.voicechat.client_not_connected"));
            return true;
        }

        try {
            commandSender.sendMessage(new TextComponentTranslation("message.voicechat.sending_ping"));
            Voicechat.SERVER.getServer().getPingManager().sendPing(clientConnection, 500, 10, new PingManager.PingListener() {

                @Override
                public void onPong(int attempts, long pingMilliseconds) {
                    if (attempts <= 1) {
                        commandSender.sendMessage(new TextComponentTranslation("message.voicechat.ping_received", new TextComponentString(String.valueOf(pingMilliseconds))));
                    } else {
                        commandSender.sendMessage(new TextComponentTranslation("message.voicechat.ping_received_attempt", new TextComponentString(String.valueOf(attempts)), new TextComponentString(String.valueOf(pingMilliseconds))));
                    }
                }

                @Override
                public void onFailedAttempt(int attempts) {
                    commandSender.sendMessage(new TextComponentTranslation("message.voicechat.ping_retry"));
                }

                @Override
                public void onTimeout(int attempts) {
                    commandSender.sendMessage(new TextComponentTranslation("message.voicechat.ping_timed_out", new TextComponentString(String.valueOf(attempts))));
                }
            });
            commandSender.sendMessage(new TextComponentTranslation("message.voicechat.ping_sent_waiting"));
        } catch (Exception e) {
            commandSender.sendMessage(new TextComponentTranslation("message.voicechat.failed_to_send_ping", new TextComponentString(String.valueOf(e.getMessage()))));
            Voicechat.LOGGER.warn("Failed to send ping", e);
        }
        return true;
    }

    private boolean inviteCommand(EntityPlayerMP commandSender, String[] args) {
        if (args.length < 2) {
            return false;
        }

        EntityPlayerMP player = parsePlayer(commandSender, args[1]);

        if (player == null) {
            commandSender.sendMessage(new TextComponentTranslation("commands.generic.player.notFound", args[1]));
            return true;
        }

        PlayerState state = Voicechat.SERVER.getServer().getPlayerStateManager().getState(commandSender.getUniqueID());

        if (state == null || !state.hasGroup()) {
            commandSender.sendMessage(new TextComponentTranslation("message.voicechat.not_in_group"));
            return true;
        }

        Group group = Voicechat.SERVER.getServer().getGroupManager().getGroup(state.getGroup());
        if (group == null) {
            return true;
        }

        if (!Voicechat.SERVER.isCompatible(player)) {
            commandSender.sendMessage(new TextComponentTranslation("message.voicechat.player_no_voicechat", player.getDisplayName(), new TextComponentString(CommonCompatibilityManager.INSTANCE.getModName())));
            return true;
        }

        String passwordSuffix = group.getPassword() == null ? "" : " \"" + group.getPassword() + "\"";
        player.sendMessage(new TextComponentTranslation("message.voicechat.invite",
                new TextComponentString(commandSender.getName()),
                new TextComponentString(group.getName()).setStyle(new Style().setColor(TextFormatting.GRAY)),
                new TextComponentString("[").appendSibling(
                        new TextComponentTranslation("message.voicechat.accept_invite")
                                .setStyle(new Style()
                                        .setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/voicechat join " + group.getId().toString() + passwordSuffix))
                                        .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentTranslation("message.voicechat.accept_invite.hover")))
                                        .setColor(TextFormatting.GREEN)
                                ).appendText("]").setStyle(new Style().setColor(TextFormatting.GREEN))
                )));
        commandSender.sendMessage(new TextComponentTranslation("message.voicechat.invite_successful", new TextComponentString(player.getName())));

        return true;
    }

    private boolean joinCommand(EntityPlayerMP player, String[] args) {
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
            groupUUID = getGroupUUID(player, Voicechat.SERVER.getServer(), groupName);
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

        joinGroup(player, groupUUID, password);
        return true;
    }

    private UUID getGroupUUID(EntityPlayerMP player, Server server, String groupName) {
        List<Group> groups = server.getGroupManager().getGroups().values().stream().filter(group -> group.getName().equals(groupName)).collect(Collectors.toList());

        if (groups.isEmpty()) {
            player.sendMessage(new TextComponentTranslation("message.voicechat.group_does_not_exist"));
            return null;
        }

        if (groups.size() > 1) {
            player.sendMessage(new TextComponentTranslation("message.voicechat.group_name_not_unique"));
            return null;
        }

        return groups.get(0).getId();
    }

    private static void joinGroup(EntityPlayerMP player, UUID groupID, @Nullable String password) {
        if (!Voicechat.SERVER_CONFIG.groupsEnabled.get()) {
            player.sendMessage(new TextComponentTranslation("message.voicechat.groups_disabled"));
            return;
        }

        Server server = Voicechat.SERVER.getServer();

        if (!PermissionManager.INSTANCE.GROUPS_PERMISSION.hasPermission(player)) {
            player.sendMessage(new TextComponentTranslation("message.voicechat.no_group_permission"));
            return;
        }

        Group group = server.getGroupManager().getGroup(groupID);

        if (group == null) {
            player.sendMessage(new TextComponentTranslation("message.voicechat.group_does_not_exist"));
            return;
        }

        server.getGroupManager().joinGroup(group, player, password);
        player.sendMessage(new TextComponentTranslation("message.voicechat.join_successful", new TextComponentString(group.getName()).setStyle(new Style().setColor(TextFormatting.GREEN))));
    }

    private boolean leaveCommand(EntityPlayerMP player) {
        if (!Voicechat.SERVER_CONFIG.groupsEnabled.get()) {
            player.sendMessage(new TextComponentTranslation("message.voicechat.groups_disabled"));
            return true;
        }

        Server server = Voicechat.SERVER.getServer();
        PlayerState state = server.getPlayerStateManager().getState(player.getUniqueID());
        if (state == null || !state.hasGroup()) {
            player.sendMessage(new TextComponentTranslation("message.voicechat.not_in_group"));
            return true;
        }

        server.getGroupManager().leaveGroup(player);
        player.sendMessage(new TextComponentTranslation("message.voicechat.leave_successful"));
        return true;
    }

    private static boolean checkNoVoicechat(ICommandSender commandSender) {
        if (commandSender instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) commandSender;
            if (Voicechat.SERVER.isCompatible(player)) {
                return false;
            }
            commandSender.sendMessage(new TextComponentString(String.format(Voicechat.TRANSLATIONS.voicechatNeededForCommandMessage.get(), CommonCompatibilityManager.INSTANCE.getModName())));
        } else {
            commandSender.sendMessage(new TextComponentString(Voicechat.TRANSLATIONS.playerCommandMessage.get()));
        }
        return true;
    }

    @Nullable
    public static EntityPlayerMP parsePlayer(ICommandSender commandSender, String playerArg) {
        EntityPlayerMP player = commandSender.getServer().getPlayerList().getPlayerByUsername(playerArg);
        if (player != null) {
            return player;
        }
        try {
            UUID uuid = UUID.fromString(playerArg);
            return commandSender.getServer().getPlayerList().getPlayerByUUID(uuid);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

}
