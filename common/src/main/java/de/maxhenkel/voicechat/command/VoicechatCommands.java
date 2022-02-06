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
import de.maxhenkel.voicechat.voice.common.PingPacket;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import de.maxhenkel.voicechat.voice.server.ClientConnection;
import de.maxhenkel.voicechat.voice.server.Group;
import de.maxhenkel.voicechat.voice.server.PingManager;
import de.maxhenkel.voicechat.voice.server.Server;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.network.chat.*;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;

public class VoicechatCommands {

    public static final String VOICECHAT_COMMAND = "voicechat";

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> literalBuilder = Commands.literal(VOICECHAT_COMMAND);

        literalBuilder.executes(commandSource -> help(dispatcher, commandSource));
        literalBuilder.then(Commands.literal("help").executes(commandSource -> help(dispatcher, commandSource)));

        literalBuilder.then(Commands.literal("test").requires((commandSource) -> checkPermission(commandSource, PermissionManager.INSTANCE.ADMIN_PERMISSION)).then(Commands.argument("target", EntityArgument.player()).executes((commandSource) -> {
            if (checkNoVoicechat(commandSource)) {
                return 0;
            }
            ServerPlayer player = EntityArgument.getPlayer(commandSource, "target");
            Server server = Voicechat.SERVER.getServer();
            if (server == null) {
                commandSource.getSource().sendSuccess(new TranslatableComponent("message.voicechat.voice_chat_unavailable"), false);
                return 1;
            }

            if (!Voicechat.SERVER.isCompatible(player)) {
                commandSource.getSource().sendSuccess(new TranslatableComponent("message.voicechat.player_no_voicechat", player.getDisplayName(), CommonCompatibilityManager.INSTANCE.getModName()), false);
                return 1;
            }

            ClientConnection clientConnection = server.getConnections().get(player.getUUID());
            if (clientConnection == null) {
                commandSource.getSource().sendSuccess(new TranslatableComponent("message.voicechat.client_not_connected"), false);
                return 1;
            }
            try {
                commandSource.getSource().sendSuccess(new TranslatableComponent("message.voicechat.sending_packet"), false);
                long timestamp = System.currentTimeMillis();
                server.getPingManager().sendPing(clientConnection, 5000, new PingManager.PingListener() {
                    @Override
                    public void onPong(PingPacket packet) {
                        commandSource.getSource().sendSuccess(new TranslatableComponent("message.voicechat.packet_received", (System.currentTimeMillis() - timestamp)), false);
                    }

                    @Override
                    public void onTimeout() {
                        commandSource.getSource().sendSuccess(new TranslatableComponent("message.voicechat.packet_timed_out"), false);
                    }
                });
                commandSource.getSource().sendSuccess(new TranslatableComponent("message.voicechat.packet_sent_waiting"), false);
            } catch (Exception e) {
                commandSource.getSource().sendSuccess(new TranslatableComponent("message.voicechat.failed_to_send_packet", e.getMessage()), false);
                e.printStackTrace();
                return 1;
            }
            return 1;
        })));

        literalBuilder.then(Commands.literal("invite").then(Commands.argument("target", EntityArgument.player()).executes((commandSource) -> {
            if (checkNoVoicechat(commandSource)) {
                return 0;
            }
            ServerPlayer source = commandSource.getSource().getPlayerOrException();

            Server server = Voicechat.SERVER.getServer();
            if (server == null) {
                commandSource.getSource().sendSuccess(new TranslatableComponent("message.voicechat.voice_chat_unavailable"), false);
                return 1;
            }

            PlayerState state = server.getPlayerStateManager().getState(source.getUUID());

            if (state == null || !state.hasGroup()) {
                commandSource.getSource().sendSuccess(new TranslatableComponent("message.voicechat.not_in_group"), false);
                return 1;
            }

            ServerPlayer player = EntityArgument.getPlayer(commandSource, "target");
            Group group = server.getGroupManager().getGroup(state.getGroup().getId());

            if (group == null) {
                return 1;
            }

            String passwordSuffix = group.getPassword() == null ? "" : " \"" + group.getPassword() + "\"";
            player.sendMessage(new TranslatableComponent("message.voicechat.invite", source.getDisplayName(), new TextComponent(group.getName()).withStyle(ChatFormatting.GRAY), ComponentUtils.wrapInSquareBrackets(new TranslatableComponent("message.voicechat.accept_invite").withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/voicechat join " + group.getId().toString() + passwordSuffix)).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableComponent("message.voicechat.accept_invite.hover"))))).withStyle(ChatFormatting.GREEN)), Util.NIL_UUID);

            commandSource.getSource().sendSuccess(new TranslatableComponent("message.voicechat.invite_successful", player.getDisplayName()), false);

            return 1;
        })));

        literalBuilder.then(Commands.literal("join").then(Commands.argument("group", UuidArgument.uuid()).executes((commandSource) -> {
            if (checkNoVoicechat(commandSource)) {
                return 0;
            }
            UUID groupID = UuidArgument.getUuid(commandSource, "group");
            return joinGroup(commandSource.getSource(), groupID, null);
        })));

        literalBuilder.then(Commands.literal("join").then(Commands.argument("group", UuidArgument.uuid()).then(Commands.argument("password", StringArgumentType.string()).executes((commandSource) -> {
            if (checkNoVoicechat(commandSource)) {
                return 0;
            }
            UUID groupID = UuidArgument.getUuid(commandSource, "group");
            String password = StringArgumentType.getString(commandSource, "password");
            return joinGroup(commandSource.getSource(), groupID, password.isEmpty() ? null : password);
        }))));

        literalBuilder.then(Commands.literal("leave").executes((commandSource) -> {
            if (checkNoVoicechat(commandSource)) {
                return 0;
            }
            if (!Voicechat.SERVER_CONFIG.groupsEnabled.get()) {
                commandSource.getSource().sendFailure(new TranslatableComponent("message.voicechat.groups_disabled"));
                return 1;
            }

            Server server = Voicechat.SERVER.getServer();
            if (server == null) {
                commandSource.getSource().sendSuccess(new TranslatableComponent("message.voicechat.voice_chat_unavailable"), false);
                return 1;
            }
            ServerPlayer source = commandSource.getSource().getPlayerOrException();

            PlayerState state = server.getPlayerStateManager().getState(source.getUUID());
            if (state == null || !state.hasGroup()) {
                commandSource.getSource().sendSuccess(new TranslatableComponent("message.voicechat.not_in_group"), false);
                return 1;
            }

            server.getGroupManager().leaveGroup(source);
            commandSource.getSource().sendSuccess(new TranslatableComponent("message.voicechat.leave_successful"), false);
            return 1;
        }));

        dispatcher.register(literalBuilder);
    }

    private static int joinGroup(CommandSourceStack source, UUID groupID, @Nullable String password) throws CommandSyntaxException {
        if (!Voicechat.SERVER_CONFIG.groupsEnabled.get()) {
            source.sendFailure(new TranslatableComponent("message.voicechat.groups_disabled"));
            return 1;
        }

        Server server = Voicechat.SERVER.getServer();
        if (server == null) {
            source.sendSuccess(new TranslatableComponent("message.voicechat.voice_chat_unavailable"), false);
            return 1;
        }
        ServerPlayer player = source.getPlayerOrException();

        if (!PermissionManager.INSTANCE.GROUPS_PERMISSION.hasPermission(player)) {
            source.sendSuccess(new TranslatableComponent("message.voicechat.no_group_permission"), false);
            return 1;
        }

        Group group = server.getGroupManager().getGroup(groupID);

        if (group == null) {
            source.sendFailure(new TranslatableComponent("message.voicechat.group_does_not_exist"));
            return 1;
        }

        server.getGroupManager().joinGroup(group, player, password);
        source.sendSuccess(new TranslatableComponent("message.voicechat.join_successful", new TextComponent(group.getName()).withStyle(ChatFormatting.GRAY)), false);
        return 1;
    }

    private static int help(CommandDispatcher<CommandSourceStack> dispatcher, CommandContext<CommandSourceStack> commandSource) {
        if (checkNoVoicechat(commandSource)) {
            return 0;
        }
        CommandNode<CommandSourceStack> voicechatCommand = dispatcher.getRoot().getChild(VOICECHAT_COMMAND);
        Map<CommandNode<CommandSourceStack>, String> map = dispatcher.getSmartUsage(voicechatCommand, commandSource.getSource());
        for (Map.Entry<CommandNode<CommandSourceStack>, String> entry : map.entrySet()) {
            commandSource.getSource().sendSuccess(new TextComponent("/%s %s".formatted(VOICECHAT_COMMAND, entry.getValue())), false);
        }
        return map.size();
    }

    private static boolean checkNoVoicechat(CommandContext<CommandSourceStack> commandSource) {
        try {
            ServerPlayer player = commandSource.getSource().getPlayerOrException();
            if (Voicechat.SERVER.isCompatible(player)) {
                return false;
            }
            commandSource.getSource().sendFailure(new TextComponent("You need to have %s installed to use this command".formatted(CommonCompatibilityManager.INSTANCE.getModName())));
            return true;
        } catch (Exception e) {
            commandSource.getSource().sendFailure(new TextComponent("This command can only be executed as a player"));
            return true;
        }
    }

    private static boolean checkPermission(CommandSourceStack stack, Permission permission) {
        try {
            return permission.hasPermission(stack.getPlayerOrException());
        } catch (CommandSyntaxException e) {
            return stack.hasPermission(stack.getServer().getOperatorUserPermissionLevel());
        }
    }

}
