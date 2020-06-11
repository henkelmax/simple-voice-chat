package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.Main;
import de.maxhenkel.voicechat.voice.common.AuthenticatePacket;
import de.maxhenkel.voicechat.voice.common.KeepAlivePacket;
import de.maxhenkel.voicechat.voice.common.NetworkMessage;
import de.maxhenkel.voicechat.voice.common.Utils;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Client extends Thread {

    private Socket socket;
    private DataInputStream fromServer;
    private DataOutputStream toServer;
    private List<AudioChannel> audioChannels;
    private MicThread micThread;
    private boolean running;
    private TalkCache talkCache;

    public Client(String serverIp, int serverPort) throws IOException {
        this.socket = new Socket(serverIp, serverPort);
        this.fromServer = new DataInputStream(socket.getInputStream());
        this.toServer = new DataOutputStream(socket.getOutputStream());
        this.audioChannels = new ArrayList<>();
        this.running = true;
        this.talkCache = new TalkCache();
        setDaemon(true);

        try {
            micThread = new MicThread(toServer);
            micThread.start();
        } catch (Exception e) {
            Main.LOGGER.error("Mic unavailable " + e);
        }
    }

    @Override
    public void run() {
        try {
            while (running) {
                if (socket.getInputStream().available() > 0) {
                    NetworkMessage in = NetworkMessage.readPacket(fromServer);
                    // Ignoring KeepAlive packets
                    if (in.getPacket() instanceof KeepAlivePacket) {
                        continue;
                    }
                    AudioChannel sendTo = audioChannels.stream().filter(audioChannel -> audioChannel.getUUID().equals(in.getPlayerUUID())).findFirst().orElse(null); //TODO to map
                    if (sendTo == null) {
                        AudioChannel ch = new AudioChannel(this, in.getPlayerUUID());
                        ch.addToQueue(in);
                        ch.start();
                        audioChannels.add(ch);
                    } else {
                        sendTo.addToQueue(in);
                    }
                } else {
                    audioChannels.stream().filter(AudioChannel::canKill).forEach(AudioChannel::closeAndKill);
                    audioChannels.removeIf(AudioChannel::canKill);
                    Utils.sleep(1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void authenticate(UUID playerUUID, UUID secret) {
        try {
            (new NetworkMessage(new AuthenticatePacket(playerUUID, secret))).send(toServer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        Main.LOGGER.info("Disconnecting client");
        running = false;
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (micThread != null) {
            micThread.close();
        }
    }

    public MicThread getMicThread() {
        return micThread;
    }

    public boolean isConnected() {
        return running && !socket.isClosed();
    }

    public TalkCache getTalkCache() {
        return talkCache;
    }
}
 