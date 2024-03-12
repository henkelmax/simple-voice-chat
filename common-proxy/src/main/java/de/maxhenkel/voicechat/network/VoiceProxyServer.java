package de.maxhenkel.voicechat.network;

import de.maxhenkel.voicechat.VoiceProxy;

import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * The VoiceProxyServer implements the publicly facing UDP server which then proxies
 * the UDP traffic to the appropriate backend server's Simple Voice Chat UDP server.
 */
public class VoiceProxyServer extends Thread {

    /**
     * A queue of incoming datagrams on the public UDP socket
     */
    private final BlockingQueue<DatagramPacket> readQueue = new LinkedBlockingQueue<>();

    /**
     * The processing thread for incoming datagrams
     */
    private final ReadQueueProcessor readQueueProcessor = new ReadQueueProcessor();

    /**
     * A queue of outgoing datagrams on the public UDP socket
     */
    private final BlockingQueue<DatagramPacket> writeQueue = new LinkedBlockingQueue<>();

    /**
     * The processing thread for outgoing datagrams.
     */
    private final WriteQueueProcessor writeQueueProcessor = new WriteQueueProcessor();

    /**
     * The instance that created this VoiceProxyBridgeManager
     */
    private final VoiceProxy voiceProxy;

    /**
     * Manages all the VoiceProxyBridge instances for this particular VoiceProxyServer
     */
    private final VoiceProxyBridgeManager voiceProxyBridgeManager;

    /**
     * The public UDP socket of the VoiceProxyServer. This is where Minecraft SimpleVoiceChat clients will connect to.
     */
    private DatagramSocket socket;

    public VoiceProxyServer(VoiceProxy proxy) {
        setDaemon(true);
        setName("VoiceProxyServer");

        voiceProxy = proxy;
        voiceProxyBridgeManager = new VoiceProxyBridgeManager(voiceProxy, this);
    }

    @Override
    public void interrupt() {
        // First we prevent any more outgoing packets
        writeQueueProcessor.interrupt();
        writeQueue.clear();

        // Then we make sure internal bridges are torn down and no more bridges can be created through
        // incoming traffic
        voiceProxyBridgeManager.shutdown();

        // We can now safely close the socket as no more outgoing traffic will be produced
        if (socket != null) {
            socket.close();
        }

        // Since the socket is closed, we can now also stop processing incoming traffic
        readQueueProcessor.interrupt();
        readQueue.clear();

        super.interrupt();
    }

    @Override
    public void run() {
        try {
            // Ensure we start with a fresh UDP socket, if for some reason there is already a socket, we have to ensure it's closed
            if (socket != null) {
                socket.close();
            }
            int port = voiceProxy.getPort();

            String bindAddress = voiceProxy.getConfig().bindAddress.get();
            InetAddress address = null;
            if (bindAddress.isEmpty()) {
                address = voiceProxy.getDefaultBindSocket().getAddress();
                bindAddress = address.getHostAddress();
            } else if (!bindAddress.trim().equals("*")) {
                try {
                    address = InetAddress.getByName(bindAddress);
                } catch (Exception e) {
                    voiceProxy.getLogger().error("An invalid bind address was specified in the config '{}', falling back to proxy bind address", bindAddress);
                    address = voiceProxy.getDefaultBindSocket().getAddress();
                    bindAddress = address.getHostAddress();
                }
            }

            try {
                socket = new DatagramSocket(port, address);
                if (bindAddress.isEmpty()) {
                    voiceProxy.getLogger().info("Voice chat proxy server started at port {}", port);
                } else {
                    voiceProxy.getLogger().info("Voice chat proxy server started at {}:{}", bindAddress, port);
                }
            } catch (BindException e) {
                if (address == null || bindAddress.equals("0.0.0.0")) {
                    throw e;
                }
                voiceProxy.getLogger().error("Failed to bind to address '{}', binding to wildcard IP instead", bindAddress);
                socket = new DatagramSocket(port);
            }
        } catch (Exception e) {
            voiceProxy.getLogger().error("The voice chat proxy server encountered a fatal error and has been shut down", e);
            interrupt();
            return;
        }

        writeQueueProcessor.start();
        readQueueProcessor.start();
        voiceProxy.getLogger().debug("Read & Write queue processors started");

        while (!isInterrupted() && !socket.isClosed()) {
            try {
                DatagramPacket packet = new DatagramPacket(new byte[4096], 4096);
                socket.receive(packet);
                readQueue.add(packet);
            } catch (Exception e) {
                if (!socket.isClosed()) {
                    voiceProxy.getLogger().debug("An exception occurred while attempting to read & queue an incoming datagram", e);
                }
            }
        }
    }

    public VoiceProxyBridgeManager getVoiceProxyBridgeManager() {
        return voiceProxyBridgeManager;
    }

    /**
     * Queue a DatagramPacket for an outgoing write. It is assumed that the datagram is already addressed to the
     * correct target, no modification will be performed.
     *
     * @param packet The DatagramPacket to write out via the public UDP socket
     */
    public void write(DatagramPacket packet) {
        writeQueue.add(packet);
    }

    /**
     * ReadQueueProcessor implements the internal asynchronous datagram queue processing thread
     * which is responsible for handling incoming datagrams, figuring out which player they belong
     * to and ultimately relaying the data to the appropriate backend server.
     * <p>
     * Any invalid datagram packets will be discarded silently.
     */
    private class ReadQueueProcessor extends Thread {

        public ReadQueueProcessor() {
            setDaemon(true);
            setName("VoiceProxyServer.ReadQueueProcessor");
        }

        @Override
        public void run() {
            while (!isInterrupted() && !socket.isClosed()) {
                try {
                    DatagramPacket packet = readQueue.poll(10, TimeUnit.MILLISECONDS);
                    if (packet == null) {
                        continue;
                    }

                    // The first byte in the datagram must match the magic byte, else this is not a valid SimpleVoiceChat packet
                    ByteBuffer bb = ByteBuffer.wrap(packet.getData());
                    if (bb.get() != (byte) 0b11111111) {
                        continue;
                    }

                    // The Player UUID comes right after the magic byte in the form of two longs
                    UUID playerUuid = new UUID(bb.getLong(), bb.getLong());
                    playerUuid = voiceProxy.getSniffer().getMappedPlayerUUID(playerUuid);

                    VoiceProxyBridgeManager.VoiceProxyBridge bridge = voiceProxyBridgeManager.getOrCreateBridge(playerUuid, packet.getSocketAddress());
                    if (bridge == null) {
                        continue;
                    }

                    bridge.forward(packet);
                } catch (InterruptedException ignored) {
                    voiceProxy.getLogger().debug("ReadQueueProcessor interrupted, shutting down");
                } catch (Exception e) {
                    if (!socket.isClosed()) {
                        voiceProxy.getLogger().error("An exception occurred while processing an incoming datagram, continuing loop...", e);
                    }
                }
            }
        }
    }

    /**
     * WriteQueueProcessor implements the internal asynchronous datagram queue processing thread
     * which is responsible for handling outgoing datagrams.
     */
    private class WriteQueueProcessor extends Thread {

        public WriteQueueProcessor() {
            setDaemon(true);
            setName("VoiceProxyServer.WriteQueueProcessor");
        }

        @Override
        public void run() {
            while (!isInterrupted() && !socket.isClosed()) {
                try {
                    DatagramPacket packet = writeQueue.poll(10, TimeUnit.MILLISECONDS);
                    if (packet != null) socket.send(packet);
                } catch (InterruptedException ignored) {
                    voiceProxy.getLogger().debug("WriteQueueProcessor interrupted, shutting down");
                } catch (Exception e) {
                    if (!socket.isClosed()) {
                        voiceProxy.getLogger().error("An exception occurred while processing an outgoing datagram, continuing loop...", e);
                    }
                }
            }
        }
    }
}
