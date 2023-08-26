package de.maxhenkel.voicechat.plugins.impl;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.api.RawUdpPacket;
import de.maxhenkel.voicechat.api.VoicechatSocket;
import de.maxhenkel.voicechat.net.NetManager;
import de.maxhenkel.voicechat.net.UDPWrapperPacket;
import de.maxhenkel.voicechat.voice.server.ClientConnection;
import de.maxhenkel.voicechat.voice.server.Server;
import net.minecraft.server.level.ServerPlayer;

import java.net.SocketAddress;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class IntegratedVoicechatSocketImpl implements VoicechatSocket {

    private boolean open;
    private final Timer sendScheduleTimer;
    private final BlockingQueue<RawUdpPacket> incomingQueue;
    private final BlockingQueue<RawUdpPacket> outgoingQueue;

    public IntegratedVoicechatSocketImpl() {
        incomingQueue = new LinkedBlockingQueue<>();
        outgoingQueue = new LinkedBlockingQueue<>();
        sendScheduleTimer = new Timer();
    }

    public void receivePacket(RawUdpPacket packet) {
        incomingQueue.add(packet);
    }

    @Override
    public void open(int port, String bindAddress) throws Exception {
        Voicechat.LOGGER.info("Using integrated networking for voice chat");
        sendScheduleTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                onTimer();
            }
        }, 40, 40);
        open = true;
    }

    @Override
    public RawUdpPacket read() throws Exception {
        return incomingQueue.take();
    }

    @Override
    public void send(byte[] data, SocketAddress address) throws Exception {
        outgoingQueue.put(new RawUdpPacketImpl(data, address, System.currentTimeMillis()));
    }

    private void onTimer() {
        if (outgoingQueue.isEmpty()) {
            return;
        }
        Map<SocketAddress, List<RawUdpPacket>> packetsToSend = new HashMap<>();
        while (true) {
            RawUdpPacket packet = outgoingQueue.poll();
            if (packet == null) {
                break;
            }
            packetsToSend.computeIfAbsent(packet.getSocketAddress(), k -> new ArrayList<>()).add(packet);
        }
        if (packetsToSend.isEmpty()) {
            return;
        }
        Server server = Voicechat.SERVER.getServer();
        if (server == null) {
            return;
        }
        for (Map.Entry<SocketAddress, List<RawUdpPacket>> entry : packetsToSend.entrySet()) {
            ClientConnection connection = server.getSender(entry.getKey());
            if (connection == null) {
                connection = server.getUnconnectedSender(entry.getKey());
                if (connection == null) {
                    Voicechat.logDebug("No connection found for {}", entry.getKey());
                    continue;
                }
            }
            ServerPlayer player = server.getServer().getPlayerList().getPlayer(connection.getPlayerUUID());
            if (player == null) {
                Voicechat.logDebug("Player with UUID {} not found", connection.getPlayerUUID());
                continue;
            }
            NetManager.sendToClient(player, new UDPWrapperPacket(entry.getValue()));
        }
    }

    @Override
    public int getLocalPort() {
        return -1;
    }

    @Override
    public void close() {
        open = false;
        sendScheduleTimer.cancel();
    }

    @Override
    public boolean isClosed() {
        return !open;
    }
}
