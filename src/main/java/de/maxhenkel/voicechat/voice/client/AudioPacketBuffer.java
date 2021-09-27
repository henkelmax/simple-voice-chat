package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.voice.common.SoundPacket;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class AudioPacketBuffer {

    private final int packetThreshold;
    @Nullable
    private List<SoundPacket> packetBuffer;
    private long lastSequenceNumber = -1;

    public AudioPacketBuffer(int packetThreshold) {
        this.packetThreshold = packetThreshold;
        if (packetThreshold > 0) {
            this.packetBuffer = new ArrayList<>();
        }
    }

    @Nullable
    public SoundPacket poll(BlockingQueue<SoundPacket> queue) throws InterruptedException {
        SoundPacket packet = queue.poll(10, TimeUnit.MILLISECONDS);
        if (packetThreshold <= 0) {
            return packet;
        }

        if (packet == null) {
            return getNext();
        }

        if (lastSequenceNumber + 1 == packet.getSequenceNumber()) {
            lastSequenceNumber = packet.getSequenceNumber();
            return packet;
        }

        addSorted(packet);
        return getNext();
    }

    private void addSorted(SoundPacket packet) {
        packetBuffer.add(packet);
        packetBuffer.sort(Comparator.comparingLong(SoundPacket::getSequenceNumber));
    }

    @Nullable
    private SoundPacket getNext() {
        if (packetBuffer.size() > packetThreshold) {
            SoundPacket packet = packetBuffer.remove(0);
            lastSequenceNumber = packet.getSequenceNumber();
            return packet;
        } else {
            return null;
        }
    }

    public void clear() {
        if (packetBuffer != null) {
            packetBuffer.clear();
        }
        lastSequenceNumber = -1;
    }

}
