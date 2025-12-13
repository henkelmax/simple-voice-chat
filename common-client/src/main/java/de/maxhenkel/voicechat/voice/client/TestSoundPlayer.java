package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.debug.VoicechatUncaughtExceptionHandler;
import de.maxhenkel.voicechat.voice.client.speaker.Speaker;
import de.maxhenkel.voicechat.voice.client.speaker.SpeakerManager;
import de.maxhenkel.voicechat.voice.common.AudioUtils;
import de.maxhenkel.voicechat.voice.common.Utils;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class TestSoundPlayer {

    private static final AtomicBoolean running = new AtomicBoolean(false);
    private static short[][] testSound;

    public static synchronized boolean preload() {
        if (testSound != null) {
            return true;
        }
        try {
            testSound = loadTestSound();
            return true;
        } catch (Exception e) {
            Voicechat.LOGGER.error("Failed to load test sound", e);
            testSound = new short[0][0];
            return false;
        }
    }

    public boolean canPlay() {
        return testSound != null && testSound.length > 0;
    }

    public static void playTestSound(Runnable onDone) {
        if (!running.compareAndSet(false, true)) {
            return;
        }
        Thread thread = new Thread(() -> play(onDone));
        thread.setDaemon(true);
        thread.setName("TestSoundPlayer");
        thread.setUncaughtExceptionHandler(new VoicechatUncaughtExceptionHandler());
        thread.start();
    }

    private static void play(Runnable onDone) {
        try {
            if (!preload()) {
                Voicechat.LOGGER.error("Failed to play test sound");
                return;
            }
            SoundManager soundManager;
            boolean ownSoundManager = false;
            ClientVoicechat client = ClientManager.getClient();
            if (client != null) {
                soundManager = client.getSoundManager();
                if (soundManager == null) {
                    Voicechat.LOGGER.error("Failed to play test sound - Sound manager not loaded");
                    return;
                }
            } else {
                soundManager = SoundManager.create();
                ownSoundManager = true;
            }

            Speaker speaker = SpeakerManager.createSpeaker(soundManager, UUID.randomUUID());
            speaker.open();
            for (short[] shorts : testSound) {
                speaker.play(shorts, 1F, null);
                Utils.sleep(20);
            }
            Utils.sleep(VoicechatClient.CLIENT_CONFIG.outputBufferSize.get() * 20 + 250);
            speaker.close();
            if (ownSoundManager) {
                soundManager.close();
            }
        } catch (Exception e) {
            Voicechat.LOGGER.error("Failed to play test sound", e);
        }
        running.set(false);
        onDone.run();
    }

    private static short[][] loadTestSound() throws IOException, UnsupportedAudioFileException {
        InputStream testSoundStream = TestSoundPlayer.class.getResourceAsStream("/assets/voicechat/raw_sounds/test.wav");
        if (testSoundStream == null) {
            throw new IOException("Failed to load test sound");
        }
        try (AudioInputStream pcm = AudioSystem.getAudioInputStream(new BufferedInputStream(testSoundStream));
             ByteArrayOutputStream bout = new ByteArrayOutputStream()) {
            byte[] buf = new byte[8192];
            int numRead;
            while ((numRead = pcm.read(buf)) != -1) {
                bout.write(buf, 0, numRead);
            }
            return splitIntoFrames(AudioUtils.bytesToShorts(bout.toByteArray()));
        }
    }

    public static short[][] splitIntoFrames(short[] samples) {
        int frames = (samples.length + AudioUtils.FRAME_SIZE - 1) / AudioUtils.FRAME_SIZE;
        short[][] out = new short[frames][AudioUtils.FRAME_SIZE];

        int src = 0;
        for (int f = 0; f < frames; f++) {
            int len = Math.min(AudioUtils.FRAME_SIZE, samples.length - src);
            if (len > 0) {
                System.arraycopy(samples, src, out[f], 0, len);
                src += len;
            }
        }
        return out;
    }

}
