package de.maxhenkel.voicechat.plugins.impl;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.api.ClientVoicechatSocket;
import de.maxhenkel.voicechat.api.RawUdpPacket;
import de.maxhenkel.voicechat.net.NetManager;
import de.maxhenkel.voicechat.net.UDPWrapperPacket;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.client.ClientVoicechat;
import de.maxhenkel.voicechat.voice.client.ClientVoicechatConnection;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ClientIntegratedVoicechatSocketImpl implements ClientVoicechatSocket {

    private boolean open;
    private final Timer sendScheduleTimer;
    private final BlockingQueue<RawUdpPacket> incomingQueue;
    private final BlockingQueue<RawUdpPacket> outgoingQueue;

    public ClientIntegratedVoicechatSocketImpl() {
        incomingQueue = new LinkedBlockingQueue<>();
        outgoingQueue = new LinkedBlockingQueue<>();
        sendScheduleTimer = new Timer();
    }

    public void receivePackets(List<RawUdpPacket> packets) {
        incomingQueue.addAll(packets);
    }

    @Override
    public void open() {
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
        ClientVoicechat client = ClientManager.getClient();
        if (client == null) {
            return;
        }
        ClientVoicechatConnection connection = client.getConnection();
        if (connection == null) {
            return;
        }
        List<RawUdpPacket> packets = new ArrayList<>(outgoingQueue.size());
        // Don't send more than 8 packets at once from the server to the client to avoid getting kicked for too large packets
        outgoingQueue.drainTo(packets, 8);
        NetManager.sendToServer(new UDPWrapperPacket(packets, connection.getData().getPlayerUUID()));
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
