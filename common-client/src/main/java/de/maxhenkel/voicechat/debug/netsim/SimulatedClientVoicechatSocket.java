package de.maxhenkel.voicechat.debug.netsim;

import de.maxhenkel.voicechat.api.ClientVoicechatSocket;
import de.maxhenkel.voicechat.api.RawUdpPacket;
import de.maxhenkel.voicechat.voice.common.NamedThreadPoolFactory;

import javax.annotation.Nullable;
import java.net.ProtocolException;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.concurrent.*;

public class SimulatedClientVoicechatSocket implements ClientVoicechatSocket {

    private static final int REORDER_DELAY_MILLIS = 50;

    private final ClientVoicechatSocket socket;
    private final ScheduledExecutorService scheduler;
    private final BlockingQueue<Received> received;
    @Nullable
    private volatile Exception sendError;

    public SimulatedClientVoicechatSocket(ClientVoicechatSocket socket) {
        NetworkSimulator.register();
        this.socket = socket;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(NamedThreadPoolFactory.create("VoiceChatNetworkSimulatorThread"));
        this.received = new LinkedBlockingQueue<>();
    }

    @Override
    public void open() throws Exception {
        socket.open();
        NetworkSimulator.INSTANCE.setSocket(this);
        Thread receiveThread = new Thread(this::receiveLoop, "VoiceChatNetworkSimulatorReceiveThread");
        receiveThread.setDaemon(true);
        receiveThread.start();
    }

    private void receiveLoop() {
        while (true) {
            try {
                Received packet = new Received(socket.read(), null);
                if (!NetworkSimulator.INSTANCE.isActive()) {
                    received.add(packet);
                    continue;
                }
                for (long delay : simulateDelays()) {
                    schedule(delay, () -> received.add(packet));
                }
            } catch (ProtocolException e) {
                received.add(new Received(null, e));
            } catch (Exception e) {
                received.add(new Received(null, e));
                return;
            }
        }
    }

    @Override
    public RawUdpPacket read() throws Exception {
        Received r = received.take();
        if (r.getError() != null) {
            throw r.getError();
        }
        return r.getPacket();
    }

    @Override
    public void send(byte[] data, SocketAddress address) throws Exception {
        Exception error = sendError;
        if (error != null) {
            throw error;
        }
        if (!NetworkSimulator.INSTANCE.isActive()) {
            socket.send(data, address);
            return;
        }
        for (long delay : simulateDelays()) {
            schedule(delay, () -> {
                try {
                    socket.send(data, address);
                } catch (Exception e) {
                    sendError = e;
                }
            });
        }
    }

    /**
     * @return the delay in milliseconds for each copy of the packet that should be delivered - Empty if the packet is lost
     */
    private long[] simulateDelays() {
        NetworkSimulator sim = NetworkSimulator.INSTANCE;
        ThreadLocalRandom random = ThreadLocalRandom.current();
        if (random.nextDouble() < sim.getLossProbability()) {
            return new long[0];
        }
        long[] delays = new long[random.nextDouble() < sim.getDuplicateProbability() ? 2 : 1];
        for (int i = 0; i < delays.length; i++) {
            int jitter = sim.getJitterMillis();
            delays[i] = sim.getDelayMillis() + random.nextLong(-jitter, jitter + 1L);
            if (random.nextDouble() < sim.getReorderProbability()) {
                delays[i] += REORDER_DELAY_MILLIS;
            }
        }
        return delays;
    }

    private void schedule(long delayMillis, Runnable task) {
        try {
            scheduler.schedule(task, delayMillis, TimeUnit.MILLISECONDS);
        } catch (RejectedExecutionException ignored) {
            // The socket was closed
        }
    }

    public void simulateFailure() {
        received.add(new Received(null, new SocketException("Simulated connection failure")));
    }

    @Override
    public void close() {
        scheduler.shutdownNow();
        socket.close();
    }

    @Override
    public boolean isClosed() {
        return socket.isClosed();
    }

    private static class Received {

        @Nullable
        private final RawUdpPacket packet;
        @Nullable
        private final Exception error;

        public Received(@Nullable RawUdpPacket packet, @Nullable Exception error) {
            this.packet = packet;
            this.error = error;
        }

        @Nullable
        public RawUdpPacket getPacket() {
            return packet;
        }

        @Nullable
        public Exception getError() {
            return error;
        }
    }

}
