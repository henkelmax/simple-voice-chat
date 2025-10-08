package de.maxhenkel.voicechat.plugins.impl.audiochannel;

import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.audiochannel.StaticAudioChannel;
import de.maxhenkel.voicechat.api.packets.MicrophonePacket;
import de.maxhenkel.voicechat.plugins.impl.ServerPlayerImpl;
import de.maxhenkel.voicechat.plugins.impl.VoicechatServerApiImpl;
import de.maxhenkel.voicechat.voice.common.GroupSoundPacket;
import de.maxhenkel.voicechat.voice.server.ClientConnection;
import de.maxhenkel.voicechat.voice.server.Group;
import de.maxhenkel.voicechat.voice.server.Server;
import de.maxhenkel.voicechat.voice.server.ServerGroupManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class StaticAudioChannelImpl extends AudioChannelImpl implements StaticAudioChannel {

    protected boolean bypassGroupIsolation;
    protected final Set<UUID> targets;

    public StaticAudioChannelImpl(UUID channelId, Server server) {
        super(channelId, server);
        this.targets = new HashSet<>();
    }

    @Override
    public void send(byte[] opusData) {
        broadcast(new GroupSoundPacket(channelId, channelId, opusData, sequenceNumber.getAndIncrement(), category));
    }

    @Override
    public void send(MicrophonePacket packet) {
        send(packet.getOpusEncodedData());
    }

    @Override
    public void flush() {
        GroupSoundPacket packet = new GroupSoundPacket(channelId, channelId, new byte[0], sequenceNumber.getAndIncrement(), category);
        broadcast(packet);
    }

    private void broadcast(GroupSoundPacket packet) {
        synchronized (targets) {
            ServerGroupManager groupManager = server.getGroupManager();
            for (UUID target : targets) {
                ClientConnection connection = server.getConnection(target);
                if (connection == null) {
                    continue;
                }
                Player player = Bukkit.getPlayer(target);
                if (player == null) {
                    continue;
                }
                if (!bypassGroupIsolation) {
                    Group playerGroup = groupManager.getPlayerGroup(player);
                    if (playerGroup != null && playerGroup.isIsolated()) {
                        continue;
                    }
                }
                if (filter != null) {
                    if (!filter.test(new ServerPlayerImpl(player))) {
                        continue;
                    }
                }
                VoicechatServerApiImpl.sendPacket(player, packet);
            }
        }
    }

    @Override
    public void setBypassGroupIsolation(boolean bypassGroupIsolation) {
        this.bypassGroupIsolation = bypassGroupIsolation;
    }

    @Override
    public boolean bypassesGroupIsolation() {
        return bypassGroupIsolation;
    }

    @Override
    public void addTarget(VoicechatConnection target) {
        synchronized (targets) {
            targets.add(target.getPlayer().getUuid());
        }
    }

    @Override
    public void removeTarget(VoicechatConnection target) {
        synchronized (targets) {
            targets.remove(target.getPlayer().getUuid());
        }
    }

    @Override
    public void clearTargets() {
        synchronized (targets) {
            targets.clear();
        }
    }
}
