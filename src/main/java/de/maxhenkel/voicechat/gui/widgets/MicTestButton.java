package de.maxhenkel.voicechat.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.voice.client.Client;
import de.maxhenkel.voicechat.voice.client.DataLines;
import de.maxhenkel.voicechat.voice.client.MicThread;
import de.maxhenkel.voicechat.voice.common.Utils;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import javax.sound.sampled.*;

public class MicTestButton extends AbstractButton {

    private boolean micActive;
    private VoiceThread voiceThread;
    private MicListener micListener;
    private Client client;

    public MicTestButton(int xIn, int yIn, int widthIn, int heightIn, MicListener micListener, Client client) {
        super(xIn, yIn, widthIn, heightIn, TextComponent.EMPTY);
        this.micListener = micListener;
        this.client = client;
        if (getMic() == null) {
            micActive = false;
        }
        updateText();

    }

    private void updateText() {
        if (!visible) {
            setMessage(new TranslatableComponent("message.voicechat.mic_test_unavailable"));
            return;
        }
        if (micActive) {
            setMessage(new TranslatableComponent("message.voicechat.mic_test_on"));
        } else {
            setMessage(new TranslatableComponent("message.voicechat.mic_test_off"));
        }
    }

    @Override
    public void render(PoseStack matrixStack, int x, int y, float partialTicks) {
        super.render(matrixStack, x, y, partialTicks);
        if (voiceThread != null) {
            voiceThread.updateLastRender();
        }
    }

    public void setMicActive(boolean micActive) {
        this.micActive = micActive;
        updateText();
    }

    @Override
    public void onPress() {
        setMicActive(!micActive);
        updateText();
        if (micActive) {
            if (voiceThread != null) {
                voiceThread.close();
            }
            try {
                voiceThread = new VoiceThread();
                voiceThread.start();
            } catch (LineUnavailableException e) {
                setMicActive(false);
                e.printStackTrace();
            }
        } else {
            if (voiceThread != null) {
                voiceThread.close();
            }
        }
    }

    private TargetDataLine getMic() {
        MicThread micThread = client.getMicThread();
        if (micThread == null) {
            return null;
        }
        return micThread.getMic();
    }

    private void setMicLocked(boolean locked) {
        MicThread micThread = client.getMicThread();
        if (micThread == null) {
            return;
        }
        micThread.setMicrophoneLocked(locked);
    }

    private class VoiceThread extends Thread {

        private final AudioFormat audioFormat;
        private final TargetDataLine mic;
        private final SourceDataLine speaker;
        private final FloatControl gainControl;
        private boolean running;
        private long lastRender;

        public VoiceThread() throws LineUnavailableException {
            this.running = true;
            setDaemon(true);
            audioFormat = client.getAudioChannelConfig().getMonoFormat();
            mic = getMic();
            if (mic == null) {
                throw new LineUnavailableException("No microphone");
            }
            speaker = DataLines.getSpeaker(audioFormat);
            if (speaker == null) {
                throw new LineUnavailableException("No speaker");
            }
            speaker.open(audioFormat);
            speaker.start();

            gainControl = (FloatControl) speaker.getControl(FloatControl.Type.MASTER_GAIN);

            updateLastRender();
            setMicLocked(true);
        }

        @Override
        public void run() {
            while (running) {
                if (System.currentTimeMillis() - lastRender > 500L) {
                    close();
                    return;
                }
                mic.start();
                int dataLength = client.getAudioChannelConfig().getFrameSize();
                if (mic.available() < dataLength) {
                    Utils.sleep(1);
                    continue;
                }
                byte[] buff = new byte[dataLength];
                mic.read(buff, 0, buff.length);
                Utils.adjustVolumeMono(buff, VoicechatClient.CLIENT_CONFIG.microphoneAmplification.get().floatValue());

                micListener.onMicValue(Utils.dbToPerc(Utils.getHighestAudioLevel(buff)));

                gainControl.setValue(Math.min(Math.max(Utils.percentageToDB(VoicechatClient.CLIENT_CONFIG.voiceChatVolume.get().floatValue()), gainControl.getMinimum()), gainControl.getMaximum()));

                speaker.write(buff, 0, buff.length);
            }
        }

        public void updateLastRender() {
            lastRender = System.currentTimeMillis();
        }

        public void close() {
            Voicechat.LOGGER.info("Closing mic test audio channel");
            running = false;
            speaker.stop();
            speaker.flush();
            speaker.close();
            mic.stop();
            mic.flush();
            setMicLocked(false);
            micListener.onMicValue(0D);
        }

    }

    public static interface MicListener {
        void onMicValue(double perc);
    }
}
