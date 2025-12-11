package de.maxhenkel.voicechat;

import de.maxhenkel.voicechat.api.MinecraftServer;
import de.maxhenkel.voicechat.api.ServerPlayer;
import de.maxhenkel.voicechat.api.VCByteBuf;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.voice.server.Server;
import de.maxhenkel.voicechat.voice.server.ServerVoiceEvents;
import io.netty.buffer.ByteBuf;

import java.util.Timer;
import java.util.TimerTask;

public interface OutSourcing {
    TimerTask getTimerSchedule(ServerVoiceEvents voiceEvents, Timer timer, Server server, ServerPlayer player);

    void setServerListener(ServerVoiceEvents serverVoiceEvents);

    void sendCustomPacket(ServerPlayer player, String id, VCByteBuf buffer);

    boolean hasGroupPermissions(ServerPlayer player);

    boolean hasSpeakPermissions(ServerPlayer player);

    boolean hasListenPermissions(ServerPlayer player);

    void serverExecute(MinecraftServer server, Runnable runnable);

    void registercommands();

    VoicechatServerApi getServerApi();

    VCByteBuf byteBufOf(ByteBuf byteBuf);
}
