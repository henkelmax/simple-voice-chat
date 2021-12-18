package de.maxhenkel.voicechat.voice.server;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.voice.common.PingPacket;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PingManager {

    private final Map<UUID, Ping> listeners;
    private final Server server;

    public PingManager(Server server) {
        this.server = server;
        listeners = new HashMap<>();
    }

    public void onPongPacket(PingPacket packet) {
        Voicechat.LOGGER.info("Received pong {}", packet.getId());
        Ping ping = listeners.remove(packet.getId());
        if (ping == null) {
            return;
        }
        ping.listener.onPong(packet);
    }

    public void checkTimeouts() {
        if (listeners.isEmpty()) {
            return;
        }
        List<Map.Entry<UUID, Ping>> timedOut = listeners.entrySet().stream().filter(uuidPingEntry -> uuidPingEntry.getValue().isTimedOut()).toList();
        for (Map.Entry<UUID, Ping> ping : timedOut) {
            ping.getValue().listener.onTimeout();
            listeners.remove(ping.getKey());
        }
    }

    public void sendPing(ClientConnection connection, long timeout, PingListener listener) throws Exception {
        UUID id = UUID.randomUUID();
        long timestamp = System.currentTimeMillis();
        listeners.put(id, new Ping(listener, timestamp, timeout));
        server.sendPacket(new PingPacket(id, timestamp), connection);
        Voicechat.LOGGER.info("Sent ping {}", id);
    }

    private static class Ping {
        private final PingListener listener;
        private final long timestamp;
        private final long timeout;

        public Ping(PingListener listener, long timestamp, long timeout) {
            this.listener = listener;
            this.timestamp = timestamp;
            this.timeout = timeout;
        }

        public boolean isTimedOut() {
            return (System.currentTimeMillis() - timestamp) >= timeout;
        }
    }

    public static interface PingListener {
        void onPong(PingPacket packet);

        void onTimeout();
    }

}