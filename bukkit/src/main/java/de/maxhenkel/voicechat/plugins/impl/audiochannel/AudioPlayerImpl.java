package de.maxhenkel.voicechat.plugins.impl.audiochannel;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.api.audiochannel.AudioChannel;
import de.maxhenkel.voicechat.api.audiochannel.AudioPlayer;
import de.maxhenkel.voicechat.api.opus.OpusEncoder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

public class AudioPlayerImpl extends Thread implements AudioPlayer {

    private static final long FRAME_SIZE_NS = 20_000_000;
    public static final int FRAME_SIZE = 960;

    private final AudioChannel audioChannel;
    private final OpusEncoder encoder;
    private final Supplier<short[]> audioSupplier;
    private boolean started;
    @Nullable
    private Runnable onStopped;

    public AudioPlayerImpl(AudioChannel audioChannel, @Nonnull OpusEncoder encoder, Supplier<short[]> audioSupplier) {
        this.audioChannel = audioChannel;
        this.encoder = encoder;
        this.audioSupplier = audioSupplier;
        setDaemon(true);
        setName("AudioPlayer-%s".formatted(audioChannel.getId()));
    }

    @Override
    public void startPlaying() {
        if (started) {
            return;
        }
        start();
        started = true;
    }

    @Override
    public void stopPlaying() {
        interrupt();
    }

    @Override
    public boolean isStarted() {
        return started;
    }

    @Override
    public boolean isPlaying() {
        return isAlive();
    }

    @Override
    public boolean isStopped() {
        return started && !isAlive();
    }

    @Override
    public void setOnStopped(Runnable onStopped) {
        this.onStopped = onStopped;
    }

    @Override
    public void run() {
        int framePosition = 0;

        long startTime = System.nanoTime();

        short[] frame;

        while ((frame = audioSupplier.get()) != null) {
            if (frame.length != FRAME_SIZE) {
                Voicechat.LOGGER.error("Got invalid audio frame size {}!={}", frame.length, FRAME_SIZE);
                break;
            }
            audioChannel.send(encoder.encode(frame));
            framePosition++;
            long waitTimestamp = startTime + framePosition * FRAME_SIZE_NS;

            long waitNanos = waitTimestamp - System.nanoTime();

            try {
                if (waitNanos > 0L) {
                    Thread.sleep(waitNanos / 1_000_000L, (int) (waitNanos % 1_000_000));
                }
            } catch (InterruptedException e) {
                break;
            }
        }

        encoder.close();
        audioChannel.flush();

        if (onStopped != null) {
            onStopped.run();
        }
    }

}
