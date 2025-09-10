package de.maxhenkel.voicechat.util;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.UUID;

public class PingManager {

    public static void sendPing(SocketAddress address, int attempts, PingListener listener) {
        new PingThread(address, attempts, listener).start();
    }

    public static InetSocketAddress withPort(SocketAddress address, int newPort) {
        InetSocketAddress inetAddress = (InetSocketAddress) address;
        if (inetAddress.isUnresolved()) {
            return InetSocketAddress.createUnresolved(inetAddress.getHostString(), newPort);
        }
        return new InetSocketAddress(inetAddress.getAddress(), newPort);
    }

    private static class PingThread extends Thread {

        private static final UUID CHECK_V1 = UUID.fromString("58bc9ae9-c7a8-45e4-a11c-efbb67199425");
        protected static final byte MAGIC_BYTE = (byte) 0b11111111;

        private static final int INTERVAL = 1000;

        private final SocketAddress address;
        private final int totalAttempts;
        private final PingListener listener;

        public PingThread(SocketAddress address, int totalAttempts, PingListener listener) {
            this.address = address;
            this.totalAttempts = totalAttempts;
            this.listener = listener;
            setDaemon(true);
            setName("PingThread");
        }

        @Override
        public void run() {
            try (DatagramSocket socket = new DatagramSocket()) {
                socket.setSoTimeout(INTERVAL);
                int timeoutCount = 0;
                int successCount = 0;
                int lowestPing = -1;
                for (int i = 0; i < totalAttempts; i++) {
                    try {
                        listener.onSend(i + 1);
                        sendPing(socket, address);
                    } catch (Exception e) {
                        listener.onError(e);
                        return;
                    }
                    try {
                        Pong pong = receivePong(socket);
                        int ping = (int) (System.currentTimeMillis() - pong.getTimestamp());
                        listener.onSuccessfulAttempt(i + 1, ping);
                        successCount++;
                        if (lowestPing < 0 || ping < lowestPing) {
                            lowestPing = ping;
                        }
                        Thread.sleep(INTERVAL);
                    } catch (SocketTimeoutException e) {
                        listener.onFailedAttempt(i + 1);
                        timeoutCount++;
                    } catch (Exception e) {
                        listener.onError(e);
                        return;
                    }
                }
                listener.onFinish(successCount, timeoutCount, lowestPing);
            } catch (SocketException e) {
                listener.onError(e);
            }
        }

        private static void sendPing(DatagramSocket socket, SocketAddress address) throws IOException {
            Ping ping = new Ping(UUID.randomUUID(), System.currentTimeMillis());
            ByteBuffer byteBuf = ByteBuffer.allocate(3 * 8);
            ping.write(byteBuf);
            byteBuf.flip();
            byte[] byteArray = new byte[byteBuf.remaining()];
            byteBuf.get(byteArray);
            send(socket, address, CHECK_V1, byteArray);
        }

        private static Pong receivePong(DatagramSocket socket) throws IOException {
            ByteBuffer received = receive(socket);
            return read(received);
        }

        protected static void send(DatagramSocket socket, SocketAddress address, UUID id, byte[] payload) throws IOException {
            ByteBuffer byteBuf = ByteBuffer.allocate(4096);

            byteBuf.put(MAGIC_BYTE);
            byteBuf.putLong(id.getMostSignificantBits());
            byteBuf.putLong(id.getLeastSignificantBits());

            VarIntUtils.write(byteBuf, payload.length);
            byteBuf.put(payload);

            byteBuf.flip();
            byte[] byteArray = new byte[byteBuf.remaining()];
            byteBuf.get(byteArray);

            DatagramPacket sendPacket = new DatagramPacket(byteArray, byteArray.length, address);

            socket.send(sendPacket);
        }

        protected static ByteBuffer receive(DatagramSocket socket) throws IOException {
            byte[] receiveData = new byte[4096];

            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            socket.receive(receivePacket);

            return ByteBuffer.wrap(receiveData, 0, receivePacket.getLength());
        }
    }

    public interface PingListener {

        void onSend(int attempts);

        void onSuccessfulAttempt(int attempts, long pingMilliseconds);

        void onFailedAttempt(int attempts);

        void onFinish(int successfulAttempts, int timeoutAttempts, long pingMilliseconds);

        void onError(Exception e);
    }

    private static class Ping {

        protected UUID id;
        protected long timestamp;

        public Ping(UUID id, long timestamp) {
            this.id = id;
            this.timestamp = timestamp;
        }

        public void write(ByteBuffer buf) {
            buf.putLong(id.getMostSignificantBits());
            buf.putLong(id.getLeastSignificantBits());
            buf.putLong(timestamp);
        }

    }

    private static class Pong {

        protected UUID id;
        protected long timestamp;

        public Pong(UUID id, long timestamp) {
            this.id = id;
            this.timestamp = timestamp;
        }

        public Pong() {
        }

        public UUID getId() {
            return id;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }

    private static Pong read(ByteBuffer buf) {
        Pong pong = new Pong();
        pong.id = new UUID(buf.getLong(), buf.getLong());
        pong.timestamp = buf.getLong();
        return pong;
    }

}