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
        ping.listener.onPong(ping.attempt, System.currentTimeMillis() - packet.getTimestamp());
    }

    public void checkTimeouts() {
        if (listeners.isEmpty()) {
            return;
        }
        List<Map.Entry<UUID, Ping>> timedOut = listeners.entrySet().stream().filter(uuidPingEntry -> uuidPingEntry.getValue().isTimedOut()).toList();
        for (Map.Entry<UUID, Ping> pingEntry : timedOut) {
            Ping ping = pingEntry.getValue();
            if (ping.attempt >= ping.maxAttempts) {
                listeners.remove(pingEntry.getKey());
                ping.listener.onTimeout(ping.attempt);
            } else {
                ping.listener.onFailedAttempt(ping.attempt);
                try {
                    ping.send();
                } catch (Exception e) {
                    ping.listener.onTimeout(ping.attempt);
                    Voicechat.LOGGER.warn("Failed to send ping {} after attempt {}", ping.id, ping.attempt);
                }
            }
        }
    }

    public void sendPing(ClientConnection connection, long timeout, int attempts, PingListener listener) throws Exception {
        Ping ping = new Ping(connection, listener, timeout, attempts);
        listeners.put(ping.id, ping);
        ping.send();
    }

    private class Ping {
        private final UUID id;
        private final ClientConnection connection;
        private final PingListener listener;
        private long timestamp;
        private final long timeout;
        private final int maxAttempts;
        private int attempt;

        public Ping(ClientConnection connection, PingListener listener, long timeout, int maxAttempts) {
            this.id = UUID.randomUUID();
            this.connection = connection;
            this.listener = listener;
            this.timeout = timeout;
            this.maxAttempts = maxAttempts;
            this.attempt = 0;
        }

        public boolean isTimedOut() {
            return (System.currentTimeMillis() - timestamp) >= timeout;
        }

        public void send() throws Exception {
            timestamp = System.currentTimeMillis();
            attempt++;
            server.sendPacket(new PingPacket(id, timestamp), connection);
            Voicechat.LOGGER.info("Sent ping {} attempt {}", id, attempt);
        }
    }

    public interface PingListener {
        void onPong(int attempts, long pingMilliseconds);

        void onFailedAttempt(int attempts);

        void onTimeout(int attempts);
    }

}