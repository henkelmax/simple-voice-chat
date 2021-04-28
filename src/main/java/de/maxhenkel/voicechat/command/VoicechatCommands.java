package de.maxhenkel.voicechat.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.maxhenkel.corelib.net.NetUtils;
import de.maxhenkel.voicechat.Main;
import de.maxhenkel.voicechat.net.SetGroupMessage;
import de.maxhenkel.voicechat.voice.common.PingPacket;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import de.maxhenkel.voicechat.voice.server.ClientConnection;
import de.maxhenkel.voicechat.voice.server.PingManager;
import de.maxhenkel.voicechat.voice.server.Server;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponentUtils;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

public class VoicechatCommands {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralArgumentBuilder<CommandSource> literalBuilder = Commands.literal("voicechat");

        literalBuilder.then(Commands.literal("test").requires((commandSource) -> commandSource.hasPermission(2)).then(Commands.argument("target", EntityArgument.player()).executes((commandSource) -> {
            ServerPlayerEntity player = EntityArgument.getPlayer(commandSource, "target");
            Server server = Main.SERVER_VOICE_EVENTS.getServer();
            if (server == null) {
                commandSource.getSource().sendSuccess(new TranslationTextComponent("message.voicechat.voice_chat_unavailable"), true);
                return 1;
            }
            ClientConnection clientConnection = server.getConnections().get(player.getUUID());
            if (clientConnection == null) {
                commandSource.getSource().sendSuccess(new TranslationTextComponent("message.voicechat.client_not_connected"), true);
                return 1;
            }
            try {
                commandSource.getSource().sendSuccess(new TranslationTextComponent("message.voicechat.sending_packet"), true);
                long timestamp = System.currentTimeMillis();
                server.getPingManager().sendPing(clientConnection, 5000, new PingManager.PingListener() {
                    @Override
                    public void onPong(PingPacket packet) {
                        commandSource.getSource().sendSuccess(new TranslationTextComponent("message.voicechat.packet_received", (System.currentTimeMillis() - timestamp)), true);
                    }

                    @Override
                    public void onTimeout() {
                        commandSource.getSource().sendSuccess(new TranslationTextComponent("message.voicechat.packet_timed_out"), true);
                    }
                });
                commandSource.getSource().sendSuccess(new TranslationTextComponent("message.voicechat.packet_sent_waiting"), true);
            } catch (Exception e) {
                commandSource.getSource().sendSuccess(new TranslationTextComponent("message.voicechat.failed_to_send_packet", e.getMessage()), true);
                e.printStackTrace();
                return 1;
            }
            return 1;
        })));

        literalBuilder.then(Commands.literal("invite").then(Commands.argument("target", EntityArgument.player()).executes((commandSource) -> {
            ServerPlayerEntity source = commandSource.getSource().getPlayerOrException();

            Server server = Main.SERVER_VOICE_EVENTS.getServer();
            if (server == null) {
                commandSource.getSource().sendSuccess(new TranslationTextComponent("message.voicechat.voice_chat_unavailable"), true);
                return 1;
            }

            PlayerState state = server.getPlayerStateManager().getState(source.getUUID());

            if (state == null || !state.hasGroup()) {
                commandSource.getSource().sendSuccess(new TranslationTextComponent("message.voicechat.not_in_group"), true);
                return 1;
            }

            ServerPlayerEntity player = EntityArgument.getPlayer(commandSource, "target");

            player.sendMessage(new TranslationTextComponent("message.voicechat.invite",
                    source.getDisplayName(),
                    new StringTextComponent(state.getGroup()).withStyle(TextFormatting.GRAY),
                    TextComponentUtils.wrapInSquareBrackets(new TranslationTextComponent("message.voicechat.accept_invite").withStyle(style -> style
                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/voicechat join " + state.getGroup()))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslationTextComponent("message.voicechat.accept_invite.hover")))))
                            .withStyle(TextFormatting.GREEN)
            ), Util.NIL_UUID);

            return 1;
        })));

        literalBuilder.then(Commands.literal("join").then(Commands.argument("group", StringArgumentType.string()).executes((commandSource) -> {
            if (!Main.SERVER_CONFIG.groupsEnabled.get()) {
                commandSource.getSource().sendFailure(new TranslationTextComponent("message.voicechat.groups_disabled"));
                return 1;
            }

            Server server = Main.SERVER_VOICE_EVENTS.getServer();
            if (server == null) {
                commandSource.getSource().sendSuccess(new TranslationTextComponent("message.voicechat.voice_chat_unavailable"), true);
                return 1;
            }
            ServerPlayerEntity source = commandSource.getSource().getPlayerOrException();
            String groupName = StringArgumentType.getString(commandSource, "group");

            if (groupName.length() > 16) {
                commandSource.getSource().sendFailure(new TranslationTextComponent("message.voicechat.group_name_too_long"));
                return 1;
            }

            if (!Main.GROUP_REGEX.matcher(groupName).matches()) {
                commandSource.getSource().sendFailure(new TranslationTextComponent("message.voicechat.invalid_group_name"));
                return 1;
            }

            NetUtils.sendTo(Main.SIMPLE_CHANNEL, source, new SetGroupMessage(groupName));
            commandSource.getSource().sendSuccess(new TranslationTextComponent("message.voicechat.join_successful", new StringTextComponent(groupName).withStyle(TextFormatting.GRAY)), true);
            return 1;
        })));

        dispatcher.register(literalBuilder);
    }

}
