package de.maxhenkel.voicechat.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import de.maxhenkel.voicechat.net.SetGroupPacket;
import de.maxhenkel.voicechat.voice.common.PingPacket;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import de.maxhenkel.voicechat.voice.server.ClientConnection;
import de.maxhenkel.voicechat.voice.server.PingManager;
import de.maxhenkel.voicechat.voice.server.Server;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.*;
import net.minecraft.server.level.ServerPlayer;

public class VoicechatCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> literalBuilder = Commands.literal("voicechat");

        literalBuilder.then(Commands.literal("test").requires((commandSource) -> commandSource.hasPermission(2)).then(Commands.argument("target", EntityArgument.player()).executes((commandSource) -> {
            ServerPlayer player = EntityArgument.getPlayer(commandSource, "target");
            Server server = Voicechat.SERVER.getServer();
            if (server == null) {
                commandSource.getSource().sendSuccess(new TranslatableComponent("message.voicechat.voice_chat_unavailable"), false);
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

            player.sendMessage(new TranslatableComponent("message.voicechat.invite",
                    source.getDisplayName(),
                    new TextComponent(state.getGroup()).withStyle(ChatFormatting.GRAY),
                    ComponentUtils.wrapInSquareBrackets(new TranslatableComponent("message.voicechat.accept_invite").withStyle(style -> style
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/voicechat join \"" + state.getGroup() + "\""))
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableComponent("message.voicechat.accept_invite.hover")))))
                            .withStyle(ChatFormatting.GREEN)
            ), Util.NIL_UUID);

            commandSource.getSource().sendSuccess(new TranslatableComponent("message.voicechat.invite_successful", player.getDisplayName()), false);

            return 1;
        })));

        literalBuilder.then(Commands.literal("join").then(Commands.argument("group", StringArgumentType.string()).executes((commandSource) -> {
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
            String groupName = StringArgumentType.getString(commandSource, "group");

            if (groupName.length() > 16) {
                commandSource.getSource().sendFailure(new TranslatableComponent("message.voicechat.group_name_too_long"));
                return 1;
            }

            if (!Voicechat.GROUP_REGEX.matcher(groupName).matches()) {
                commandSource.getSource().sendFailure(new TranslatableComponent("message.voicechat.invalid_group_name"));
                return 1;
            }

            CommonCompatibilityManager.INSTANCE.getNetManager().sendToClient(source, new SetGroupPacket(groupName));
            commandSource.getSource().sendSuccess(new TranslatableComponent("message.voicechat.join_successful", new TextComponent(groupName).withStyle(ChatFormatting.GRAY)), false);
            return 1;
        })));

        literalBuilder.then(Commands.literal("leave").executes((commandSource) -> {
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

            CommonCompatibilityManager.INSTANCE.getNetManager().sendToClient(source, new SetGroupPacket(""));
            commandSource.getSource().sendSuccess(new TranslatableComponent("message.voicechat.leave_successful"), false);
            return 1;
        }));

        dispatcher.register(literalBuilder);
    }

}
