package de.maxhenkel.voicechat.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.net.NetManager;
import de.maxhenkel.voicechat.net.SetGroupPacket;
import de.maxhenkel.voicechat.voice.common.PingPacket;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import de.maxhenkel.voicechat.voice.server.ClientConnection;
import de.maxhenkel.voicechat.voice.server.PingManager;
import de.maxhenkel.voicechat.voice.server.Server;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;

public class VoicechatCommands {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
        LiteralArgumentBuilder<ServerCommandSource> literalBuilder = CommandManager.literal("voicechat");

        literalBuilder.then(CommandManager.literal("test").requires((commandSource) -> commandSource.hasPermissionLevel(2)).then(CommandManager.argument("target", EntityArgumentType.player()).executes((commandSource) -> {
            ServerPlayerEntity player = EntityArgumentType.getPlayer(commandSource, "target");
            Server server = Voicechat.SERVER.getServer();
            if (server == null) {
                commandSource.getSource().sendFeedback(new TranslatableText("message.voicechat.voice_chat_unavailable"), true);
                return 1;
            }
            ClientConnection clientConnection = server.getConnections().get(player.getUuid());
            if (clientConnection == null) {
                commandSource.getSource().sendFeedback(new TranslatableText("message.voicechat.client_not_connected"), true);
                return 1;
            }
            try {
                commandSource.getSource().sendFeedback(new TranslatableText("message.voicechat.sending_packet"), true);
                long timestamp = System.currentTimeMillis();
                server.getPingManager().sendPing(clientConnection, 5000, new PingManager.PingListener() {
                    @Override
                    public void onPong(PingPacket packet) {
                        commandSource.getSource().sendFeedback(new TranslatableText("message.voicechat.packet_received", (System.currentTimeMillis() - timestamp)), true);
                    }

                    @Override
                    public void onTimeout() {
                        commandSource.getSource().sendFeedback(new TranslatableText("message.voicechat.packet_timed_out"), true);
                    }
                });
                commandSource.getSource().sendFeedback(new TranslatableText("message.voicechat.packet_sent_waiting"), true);
            } catch (Exception e) {
                commandSource.getSource().sendFeedback(new TranslatableText("message.voicechat.failed_to_send_packet", e.getMessage()), true);
                e.printStackTrace();
                return 1;
            }
            return 1;
        })));

        literalBuilder.then(CommandManager.literal("invite").then(CommandManager.argument("target", EntityArgumentType.player()).executes((commandSource) -> {
            ServerPlayerEntity source = commandSource.getSource().getPlayer();

            Server server = Voicechat.SERVER.getServer();
            if (server == null) {
                commandSource.getSource().sendFeedback(new TranslatableText("message.voicechat.voice_chat_unavailable"), true);
                return 1;
            }

            PlayerState state = server.getPlayerStateManager().getState(source.getUuid());

            if (state == null || !state.hasGroup()) {
                commandSource.getSource().sendFeedback(new TranslatableText("message.voicechat.not_in_group"), true);
                return 1;
            }

            ServerPlayerEntity player = EntityArgumentType.getPlayer(commandSource, "target");

            player.sendSystemMessage(new TranslatableText("message.voicechat.invite",
                    source.getDisplayName(),
                    new LiteralText(state.getGroup()).formatted(Formatting.GRAY),
                    Texts.bracketed(new TranslatableText("message.voicechat.accept_invite").styled(style -> style
                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/voicechat join " + state.getGroup()))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableText("message.voicechat.accept_invite.hover")))))
                            .formatted(Formatting.GREEN)
            ), Util.NIL_UUID);

            return 1;
        })));

        literalBuilder.then(CommandManager.literal("join").then(CommandManager.argument("group", StringArgumentType.string()).executes((commandSource) -> {
            Server server = Voicechat.SERVER.getServer();
            if (server == null) {
                commandSource.getSource().sendFeedback(new TranslatableText("message.voicechat.voice_chat_unavailable"), true);
                return 1;
            }
            ServerPlayerEntity source = commandSource.getSource().getPlayer();
            String groupName = StringArgumentType.getString(commandSource, "group");

            if (groupName.length() > 16) {
                commandSource.getSource().sendError(new TranslatableText("message.voicechat.group_name_too_long"));
                return 1;
            }

            if (!Voicechat.GROUP_REGEX.matcher(groupName).matches()) {
                commandSource.getSource().sendError(new TranslatableText("message.voicechat.invalid_group_name"));
                return 1;
            }

            NetManager.sendToClient(source, new SetGroupPacket(groupName));
            commandSource.getSource().sendFeedback(new TranslatableText("message.voicechat.join_successful", new LiteralText(groupName).formatted(Formatting.GRAY)), true);
            return 1;
        })));

        dispatcher.register(literalBuilder);
    }

}
