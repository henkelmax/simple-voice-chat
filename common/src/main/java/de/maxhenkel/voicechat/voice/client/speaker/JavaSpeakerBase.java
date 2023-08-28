package de.maxhenkel.voicechat.voice.client.speaker;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.voice.client.AudioChannelConfig;
import de.maxhenkel.voicechat.voice.client.DataLines;
import de.maxhenkel.voicechat.voice.client.PositionalAudioUtils;
import de.maxhenkel.voicechat.voice.client.SoundManager;
import de.maxhenkel.voicechat.voice.common.Utils;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public abstract class JavaSpeakerBase implements Speaker {

    private SourceDataLine speaker;
    private FloatControl gainControl;
    private SpeakerWatchdogThread speakerWatchdogThread;

    @Override
    public void open() throws SpeakerException {
        speaker = DataLines.getSpeaker(AudioChannelConfig.STEREO_FORMAT);
        if (speaker == null) {
            throw new SpeakerException("Could not open speaker");
        }
        try {
            speaker.open(AudioChannelConfig.STEREO_FORMAT);
        } catch (LineUnavailableException e) {
            throw new SpeakerException(e.getMessage());
        }
        gainControl = (FloatControl) speaker.getControl(FloatControl.Type.MASTER_GAIN);
        speakerWatchdogThread = new SpeakerWatchdogThread();
        speakerWatchdogThread.start();
    }

    private int getAvailableSamples() {
        return speaker.getBufferSize() - speaker.available();
    }

    @Override
    public void play(short[] data, float volume, @Nullable Vec3d position, @Nullable String category, float maxDistance) {
        synchronized (speaker) {
            playInternal(data, volume, position, category, maxDistance);
        }
    }

    private void playInternal(short[] data, float volume, @Nullable Vec3d position, @Nullable String category, float maxDistance) {
        if (getAvailableSamples() <= 0) {
            byte[] emptyData = new byte[Math.min(SoundManager.FRAME_SIZE * 4 * VoicechatClient.CLIENT_CONFIG.outputBufferSize.get(), speaker.getBufferSize() - SoundManager.FRAME_SIZE * 4)];
            speaker.write(emptyData, 0, emptyData.length);
        }

        if (getAvailableSamples() > AudioChannelConfig.maxSpeakerBufferSize()) {
            Voicechat.LOGGER.debug("Skipping playing audio to avoid delay");
            return;
        }

        short[] stereo = convertToStereo(data, position);
        byte[] bytes = Utils.shortsToBytes(stereo);
        float distanceVolume = position == null ? 1F : PositionalAudioUtils.getDistanceVolume(maxDistance, position);
        gainControl.setValue(Math.min(Math.max(Utils.percentageToDB(volume * distanceVolume), gainControl.getMinimum()), gainControl.getMaximum()));
        speaker.write(bytes, 0, bytes.length);
        speaker.start();
    }

    protected abstract short[] convertToStereo(short[] data, @Nullable Vec3d position);

    @Override
    public void close() {
        speakerWatchdogThread.close();
        synchronized (speaker) {
            speaker.stop();
            speaker.flush();
            speaker.close();
        }
    }

    private class SpeakerWatchdogThread extends Thread {

        private boolean running;

        public SpeakerWatchdogThread() {
            super("Speaker Watchdog");
            running = true;
        }

        @Override
        public void run() {
            while (running) {
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    return;
                }
                synchronized (speaker) {
                    if (getAvailableSamples() <= 0) {
                        speaker.stop();
                    }
                }
            }
        }

        public void close() {
            if (!running) {
                return;
            }
            running = false;

            try {
                join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

}
