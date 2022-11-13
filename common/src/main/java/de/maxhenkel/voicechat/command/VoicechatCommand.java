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
import java.util.UUID;

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
                helpCommand(server, sender, args);
                return;
            } else if (args[0].equalsIgnoreCase("test")) {
                if (PermissionManager.INSTANCE.ADMIN_PERMISSION.hasPermission(sender)) {
                    testCommand(server, sender, args);
                    return;
                }
            } else if (args[0].equalsIgnoreCase("invite")) {
                inviteCommand(server, sender, args);
                return;
            } else if (args[0].equalsIgnoreCase("join")) {
                joinCommand(server, sender, args);
                return;
            } else if (args[0].equalsIgnoreCase("leave")) {
                leaveCommand(server, sender, args);
                return;
            }
        }
        helpCommand(server, sender, args);
    }

    private boolean helpCommand(MinecraftServer server, EntityPlayerMP commandSender, String[] args) {
        commandSender.sendMessage(new TextComponentString("/voicechat [help]"));
        commandSender.sendMessage(new TextComponentString("/voicechat [test] <target>"));
        commandSender.sendMessage(new TextComponentString("/voicechat [invite] <target>"));
        commandSender.sendMessage(new TextComponentString("/voicechat [join] <group> [<password>]"));
        commandSender.sendMessage(new TextComponentString("/voicechat [leave]"));
        return true;
    }

    private boolean testCommand(MinecraftServer server, EntityPlayerMP commandSender, String[] args) {
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
            e.printStackTrace();
        }
        return true;
    }

    private boolean inviteCommand(MinecraftServer server, EntityPlayerMP commandSender, String[] args) {
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

        Group group = Voicechat.SERVER.getServer().getGroupManager().getGroup(state.getGroup().getId());
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

    private boolean joinCommand(MinecraftServer server, EntityPlayerMP player, String[] args) {
        if (args.length < 2) {
            return false;
        }

        UUID groupUUID;
        try {
            groupUUID = UUID.fromString(args[1]);
        } catch (Exception e) {
            player.sendMessage(new TextComponentTranslation("message.voicechat.group_does_not_exist"));
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

    private boolean leaveCommand(MinecraftServer mcServer, EntityPlayerMP player, String[] args) {
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
            commandSender.sendMessage(new TextComponentString("You need to have " + CommonCompatibilityManager.INSTANCE.getModName() + " installed on your client to use this command"));
        } else {
            commandSender.sendMessage(new TextComponentString("This command can only be executed as a player"));
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
