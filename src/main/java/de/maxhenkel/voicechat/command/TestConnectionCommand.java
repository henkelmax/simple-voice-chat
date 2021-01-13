package de.maxhenkel.voicechat.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.maxhenkel.voicechat.Main;
import de.maxhenkel.voicechat.voice.common.PingPacket;
import de.maxhenkel.voicechat.voice.server.ClientConnection;
import de.maxhenkel.voicechat.voice.server.PingManager;
import de.maxhenkel.voicechat.voice.server.Server;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.TranslationTextComponent;

import java.io.IOException;

public class TestConnectionCommand {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralArgumentBuilder<CommandSource> literalBuilder = Commands.literal("voicechat").requires((commandSource) -> commandSource.hasPermissionLevel(2));

        literalBuilder.then(Commands.literal("test").then(Commands.argument("target", EntityArgument.player()).executes((commandSource) -> {
            ServerPlayerEntity player = EntityArgument.getPlayer(commandSource, "target");
            Server server = Main.SERVER_VOICE_EVENTS.getServer();
            if (server == null) {
                commandSource.getSource().sendFeedback(new TranslationTextComponent("message.voicechat.voice_chat_unavailable"), true);
                return 1;
            }
            ClientConnection clientConnection = server.getConnections().get(player.getUniqueID());
            if (clientConnection == null) {
                commandSource.getSource().sendFeedback(new TranslationTextComponent("message.voicechat.client_not_connected"), true);
                return 1;
            }
            try {
                commandSource.getSource().sendFeedback(new TranslationTextComponent("message.voicechat.sending_packet"), true);
                long timestamp = System.currentTimeMillis();
                server.getPingManager().sendPing(clientConnection, 1000, new PingManager.PingListener() {
                    @Override
                    public void onPong(PingPacket packet) {
                        commandSource.getSource().sendFeedback(new TranslationTextComponent("message.voicechat.packet_received", (System.currentTimeMillis() - timestamp)), true);
                    }

                    @Override
                    public void onTimeout() {
                        commandSource.getSource().sendFeedback(new TranslationTextComponent("message.voicechat.packet_timed_out"), true);
                    }
                });
                commandSource.getSource().sendFeedback(new TranslationTextComponent("message.voicechat.packet_sent_waiting"), true);
            } catch (IOException e) {
                commandSource.getSource().sendFeedback(new TranslationTextComponent("message.voicechat.failed_to_send_packet", e.getMessage()), true);
                e.printStackTrace();
                return 1;
            }
            return 1;
        })));

        dispatcher.register(literalBuilder);
    }

}
