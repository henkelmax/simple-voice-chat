package de.maxhenkel.voicechat;

import de.maxhenkel.voicechat.command.TestConnectionCommand;
import de.maxhenkel.voicechat.config.ConfigBuilder;
import de.maxhenkel.voicechat.config.ServerConfig;
import de.maxhenkel.voicechat.net.Packets;
import de.maxhenkel.voicechat.net.PlayerListPacket;
import de.maxhenkel.voicechat.voice.server.ServerVoiceEvents;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

public class Voicechat implements ModInitializer {

    public static final String MODID = "voicechat";
    public static final Logger LOGGER = LogManager.getLogger(MODID);
    public static ServerVoiceEvents SERVER;
    @Nullable
    public static ServerConfig SERVER_CONFIG;

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            if (server instanceof MinecraftDedicatedServer) {
                ConfigBuilder.create(server.getRunDirectory().toPath().resolve("config").resolve(MODID).resolve("voicechat-server.properties"), builder -> SERVER_CONFIG = new ServerConfig(builder));
            }
        });

        SERVER = new ServerVoiceEvents();

        CommandRegistrationCallback.EVENT.register(TestConnectionCommand::register);

        ServerPlayNetworking.registerGlobalReceiver(Packets.REQUEST_PLAYER_LIST, (server, player, handler, buf, responseSender) -> {
            List<PlayerInfo> players = player
                    .getServer()
                    .getPlayerManager()
                    .getPlayerList()
                    .stream()
                    .filter(p -> !p.getUuid().equals(player.getUuid()))
                    .map(playerEntity -> new PlayerInfo(playerEntity.getUuid(), playerEntity.getDisplayName()))
                    .collect(Collectors.toList());
            PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
            new PlayerListPacket(players).toBytes(buffer);
            ServerPlayNetworking.send(player, Packets.PLAYER_LIST, buffer);
        });
    }
}
