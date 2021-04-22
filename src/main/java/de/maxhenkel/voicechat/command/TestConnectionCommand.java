package de.maxhenkel.voicechat.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.voice.common.PingPacket;
import de.maxhenkel.voicechat.voice.server.ClientConnection;
import de.maxhenkel.voicechat.voice.server.PingManager;
import de.maxhenkel.voicechat.voice.server.Server;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;

public class TestConnectionCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
        LiteralArgumentBuilder<ServerCommandSource> literalBuilder = CommandManager.literal("voicechat").requires((commandSource) -> commandSource.hasPermissionLevel(2));

        literalBuilder.then(CommandManager.literal("test").then(CommandManager.argument("target", EntityArgumentType.player()).executes((commandSource) -> {
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

        dispatcher.register(literalBuilder);
    }

}
