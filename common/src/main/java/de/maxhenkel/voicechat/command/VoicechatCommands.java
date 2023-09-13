package de.maxhenkel.voicechat.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import de.maxhenkel.voicechat.permission.Permission;
import de.maxhenkel.voicechat.permission.PermissionManager;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import de.maxhenkel.voicechat.voice.server.ClientConnection;
import de.maxhenkel.voicechat.voice.server.Group;
import de.maxhenkel.voicechat.voice.server.PingManager;
import de.maxhenkel.voicechat.voice.server.Server;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.command.arguments.UUIDArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class VoicechatCommands {

    public static final String VOICECHAT_COMMAND = "voicechat";

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralArgumentBuilder<CommandSource> literalBuilder = Commands.literal(VOICECHAT_COMMAND);

        literalBuilder.executes(commandSource -> help(dispatcher, commandSource));
        literalBuilder.then(Commands.literal("help").executes(commandSource -> help(dispatcher, commandSource)));

        literalBuilder.then(Commands.literal("test").requires((commandSource) -> checkPermission(commandSource, PermissionManager.INSTANCE.ADMIN_PERMISSION)).then(Commands.argument("target", EntityArgument.player()).executes((commandSource) -> {
            if (checkNoVoicechat(commandSource)) {
                return 0;
            }
            ServerPlayerEntity player = EntityArgument.getPlayer(commandSource, "target");
            Server server = Voicechat.SERVER.getServer();
            if (server == null) {
                commandSource.getSource().sendSuccess(new TranslationTextComponent("message.voicechat.voice_chat_unavailable"), false);
                return 1;
            }

            if (!Voicechat.SERVER.isCompatible(player)) {
                commandSource.getSource().sendSuccess(new TranslationTextComponent("message.voicechat.player_no_voicechat", player.getDisplayName(), CommonCompatibilityManager.INSTANCE.getModName()), false);
                return 1;
            }

            ClientConnection clientConnection = server.getConnections().get(player.getUUID());
            if (clientConnection == null) {
                commandSource.getSource().sendSuccess(new TranslationTextComponent("message.voicechat.client_not_connected"), false);
                return 1;
            }
            try {
                commandSource.getSource().sendSuccess(new TranslationTextComponent("message.voicechat.sending_ping"), false);

                server.getPingManager().sendPing(clientConnection, 500, 10, new PingManager.PingListener() {

                    @Override
                    public void onPong(int attempts, long pingMilliseconds) {
                        if (attempts <= 1) {
                            commandSource.getSource().sendSuccess(new TranslationTextComponent("message.voicechat.ping_received", pingMilliseconds), false);
                        } else {
                            commandSource.getSource().sendSuccess(new TranslationTextComponent("message.voicechat.ping_received_attempt", attempts, pingMilliseconds), false);
                        }
                    }

                    @Override
                    public void onFailedAttempt(int attempts) {
                        commandSource.getSource().sendSuccess(new TranslationTextComponent("message.voicechat.ping_retry"), false);
                    }

                    @Override
                    public void onTimeout(int attempts) {
                        commandSource.getSource().sendSuccess(new TranslationTextComponent("message.voicechat.ping_timed_out", attempts), false);
                    }
                });
                commandSource.getSource().sendSuccess(new TranslationTextComponent("message.voicechat.ping_sent_waiting"), false);
            } catch (Exception e) {
                commandSource.getSource().sendSuccess(new TranslationTextComponent("message.voicechat.failed_to_send_ping", e.getMessage()), false);
                e.printStackTrace();
                return 1;
            }
            return 1;
        })));

        literalBuilder.then(Commands.literal("invite").then(Commands.argument("target", EntityArgument.player()).executes((commandSource) -> {
            if (checkNoVoicechat(commandSource)) {
                return 0;
            }
            ServerPlayerEntity source = commandSource.getSource().getPlayerOrException();

            Server server = Voicechat.SERVER.getServer();
            if (server == null) {
                commandSource.getSource().sendSuccess(new TranslationTextComponent("message.voicechat.voice_chat_unavailable"), false);
                return 1;
            }

            PlayerState state = server.getPlayerStateManager().getState(source.getUUID());

            if (state == null || !state.hasGroup()) {
                commandSource.getSource().sendSuccess(new TranslationTextComponent("message.voicechat.not_in_group"), false);
                return 1;
            }

            ServerPlayerEntity player = EntityArgument.getPlayer(commandSource, "target");
            Group group = server.getGroupManager().getGroup(state.getGroup());

            if (group == null) {
                return 1;
            }

            if (!Voicechat.SERVER.isCompatible(player)) {
                commandSource.getSource().sendSuccess(new TranslationTextComponent("message.voicechat.player_no_voicechat", player.getDisplayName(), CommonCompatibilityManager.INSTANCE.getModName()), false);
                return 1;
            }

            String passwordSuffix = group.getPassword() == null ? "" : " \"" + group.getPassword() + "\"";
            player.sendMessage(new TranslationTextComponent("message.voicechat.invite", source.getDisplayName(), new StringTextComponent(group.getName()).withStyle(TextFormatting.GRAY), TextComponentUtils.wrapInSquareBrackets(new TranslationTextComponent("message.voicechat.accept_invite").withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/voicechat join " + group.getId().toString() + passwordSuffix)).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslationTextComponent("message.voicechat.accept_invite.hover"))))).withStyle(TextFormatting.GREEN)), Util.NIL_UUID);

            commandSource.getSource().sendSuccess(new TranslationTextComponent("message.voicechat.invite_successful", player.getDisplayName()), false);

            return 1;
        })));

        literalBuilder.then(Commands.literal("join").then(Commands.argument("group_id", UUIDArgument.uuid()).executes((commandSource) -> {
            if (checkNoVoicechat(commandSource)) {
                return 0;
            }
            UUID groupID = UUIDArgument.getUuid(commandSource, "group_id");
            return joinGroupById(commandSource.getSource(), groupID, null);
        })));

        literalBuilder.then(Commands.literal("join").then(Commands.argument("group_id", UUIDArgument.uuid()).then(Commands.argument("password", StringArgumentType.string()).executes((commandSource) -> {
            if (checkNoVoicechat(commandSource)) {
                return 0;
            }
            UUID groupID = UUIDArgument.getUuid(commandSource, "group_id");
            String password = StringArgumentType.getString(commandSource, "password");
            return joinGroupById(commandSource.getSource(), groupID, password.isEmpty() ? null : password);
        }))));

        literalBuilder.then(Commands.literal("join").then(Commands.argument("group_name", StringArgumentType.string()).suggests(GroupNameSuggestionProvider.INSTANCE).executes((commandSource) -> {
            if (checkNoVoicechat(commandSource)) {
                return 0;
            }
            String groupName = StringArgumentType.getString(commandSource, "group_name");
            return joinGroupByName(commandSource.getSource(), groupName, null);
        })));

        literalBuilder.then(Commands.literal("join").then(Commands.argument("group_name", StringArgumentType.string()).suggests(GroupNameSuggestionProvider.INSTANCE).then(Commands.argument("password", StringArgumentType.string()).executes((commandSource) -> {
            if (checkNoVoicechat(commandSource)) {
                return 0;
            }
            String groupName = StringArgumentType.getString(commandSource, "group_name");
            String password = StringArgumentType.getString(commandSource, "password");
            return joinGroupByName(commandSource.getSource(), groupName, password.isEmpty() ? null : password);
        }))));

        literalBuilder.then(Commands.literal("leave").executes((commandSource) -> {
            if (checkNoVoicechat(commandSource)) {
                return 0;
            }
            if (!Voicechat.SERVER_CONFIG.groupsEnabled.get()) {
                commandSource.getSource().sendFailure(new TranslationTextComponent("message.voicechat.groups_disabled"));
                return 1;
            }

            Server server = Voicechat.SERVER.getServer();
            if (server == null) {
                commandSource.getSource().sendSuccess(new TranslationTextComponent("message.voicechat.voice_chat_unavailable"), false);
                return 1;
            }
            ServerPlayerEntity source = commandSource.getSource().getPlayerOrException();

            PlayerState state = server.getPlayerStateManager().getState(source.getUUID());
            if (state == null || !state.hasGroup()) {
                commandSource.getSource().sendSuccess(new TranslationTextComponent("message.voicechat.not_in_group"), false);
                return 1;
            }

            server.getGroupManager().leaveGroup(source);
            commandSource.getSource().sendSuccess(new TranslationTextComponent("message.voicechat.leave_successful"), false);
            return 1;
        }));

        dispatcher.register(literalBuilder);
    }

    private static Server joinGroup(CommandSource source) throws CommandSyntaxException {
        if (!Voicechat.SERVER_CONFIG.groupsEnabled.get()) {
            source.sendFailure(new TranslationTextComponent("message.voicechat.groups_disabled"));
            return null;
        }

        Server server = Voicechat.SERVER.getServer();
        if (server == null) {
            source.sendSuccess(new TranslationTextComponent("message.voicechat.voice_chat_unavailable"), false);
            return null;
        }
        ServerPlayerEntity player = source.getPlayerOrException();

        if (!PermissionManager.INSTANCE.GROUPS_PERMISSION.hasPermission(player)) {
            source.sendSuccess(new TranslationTextComponent("message.voicechat.no_group_permission"), false);
            return null;
        }

        return server;
    }

    private static int joinGroupByName(CommandSourceStack source, String groupName, @Nullable String password) throws CommandSyntaxException {
        Server server = joinGroup(source);
        if (server == null) {
            return 1;
        }

        List<Group> groups = server.getGroupManager().getGroups().values().stream().filter(group -> group.getName().equals(groupName)).collect(Collectors.toList());

        if (groups.isEmpty()) {
            source.sendFailure(Component.translatable("message.voicechat.group_does_not_exist"));
            return 1;
        }

        if (groups.size() > 1) {
            source.sendFailure(Component.translatable("message.voicechat.group_name_not_unique"));
            return 1;
        }

        return joinGroup(source, server, groups.get(0).getId(), password);
    }

    private static int joinGroupById(CommandSourceStack source, UUID groupID, @Nullable String password) throws CommandSyntaxException {
        Server server = joinGroup(source);
        if (server == null) {
            return 1;
        }
        return joinGroup(source, server, groupID, password);
    }

    private static int joinGroup(CommandSourceStack source, Server server, UUID groupID, @Nullable String password) throws CommandSyntaxException {
        Group group = server.getGroupManager().getGroup(groupID);

        if (group == null) {
            source.sendFailure(new TranslationTextComponent("message.voicechat.group_does_not_exist"));
            return 1;
        }

        server.getGroupManager().joinGroup(group, source.getPlayerOrException(), password);
        source.sendSuccess(new TranslationTextComponent("message.voicechat.join_successful", new StringTextComponent(group.getName()).withStyle(TextFormatting.GRAY)), false);
        return 1;
    }

    private static int help(CommandDispatcher<CommandSource> dispatcher, CommandContext<CommandSource> commandSource) {
        if (checkNoVoicechat(commandSource)) {
            return 0;
        }
        CommandNode<CommandSource> voicechatCommand = dispatcher.getRoot().getChild(VOICECHAT_COMMAND);
        Map<CommandNode<CommandSource>, String> map = dispatcher.getSmartUsage(voicechatCommand, commandSource.getSource());
        for (Map.Entry<CommandNode<CommandSource>, String> entry : map.entrySet()) {
            commandSource.getSource().sendSuccess(new StringTextComponent("/" + VOICECHAT_COMMAND + " " + entry.getValue()), false);
        }
        return map.size();
    }

    private static boolean checkNoVoicechat(CommandContext<CommandSource> commandSource) {
        try {
            ServerPlayerEntity player = commandSource.getSource().getPlayerOrException();
            if (Voicechat.SERVER.isCompatible(player)) {
                return false;
            }
            commandSource.getSource().sendFailure(new StringTextComponent(String.format(Voicechat.TRANSLATIONS.voicechatNeededForCommandMessage.get(), CommonCompatibilityManager.INSTANCE.getModName())));
            return true;
        } catch (Exception e) {
            commandSource.getSource().sendFailure(new StringTextComponent(Voicechat.TRANSLATIONS.playerCommandMessage.get()));
            return true;
        }
    }

    private static boolean checkPermission(CommandSource stack, Permission permission) {
        try {
            return permission.hasPermission(stack.getPlayerOrException());
        } catch (CommandSyntaxException e) {
            return stack.hasPermission(stack.getServer().getOperatorUserPermissionLevel());
        }
    }

}
