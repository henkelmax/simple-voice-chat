package de.maxhenkel.voicechat.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import de.maxhenkel.voicechat.Main;
import de.maxhenkel.voicechat.voice.client.AudioChannelConfig;
import de.maxhenkel.voicechat.voice.client.Client;
import de.maxhenkel.voicechat.voice.client.DataLines;
import de.maxhenkel.voicechat.voice.client.MicThread;
import de.maxhenkel.voicechat.voice.common.Denoiser;
import de.maxhenkel.voicechat.voice.common.Utils;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import javax.annotation.Nullable;
import javax.sound.sampled.*;

public class MicTestButton extends AbstractButton {

    private boolean micActive;
    private VoiceThread voiceThread;
    private MicListener micListener;

    public MicTestButton(int xIn, int yIn, int widthIn, int heightIn, MicListener micListener) {
        super(xIn, yIn, widthIn, heightIn, TextComponent.EMPTY);
        this.micListener = micListener;
        if (getMic() == null) {
            active = false;
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
                active = false;
                e.printStackTrace();
            }
        } else {
            if (voiceThread != null) {
                voiceThread.close();
            }
        }
    }

    private TargetDataLine getMic() {
        Client client = Main.CLIENT_VOICE_EVENTS.getClient();
        if (client == null) {
            return null;
        }
        MicThread micThread = client.getMicThread();
        if (micThread == null) {
            return null;
        }
        return micThread.getMic();
    }

    private void setMicLocked(boolean locked) {
        Client client = Main.CLIENT_VOICE_EVENTS.getClient();
        if (client == null) {
            return;
        }
        MicThread micThread = client.getMicThread();
        if (micThread == null) {
            return;
        }
        micThread.setMicrophoneLocked(locked);
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }

    private class VoiceThread extends Thread {

        private final AudioFormat audioFormat;
        private final TargetDataLine mic;
        private final SourceDataLine speaker;
        private final FloatControl gainControl;
        private boolean running;
        private long lastRender;
        @Nullable
        private Denoiser denoiser;

        public VoiceThread() throws LineUnavailableException {
            this.running = true;
            setDaemon(true);
            audioFormat = AudioChannelConfig.getMonoFormat();
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
            speaker.write(new byte[client.getAudioChannelConfig().getFrameSize() * 10], 0, client.getAudioChannelConfig().getFrameSize() * 10);

            gainControl = (FloatControl) speaker.getControl(FloatControl.Type.MASTER_GAIN);

            denoiser = Denoiser.createDenoiser();

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
                int dataLength = AudioChannelConfig.getFrameSize();
                if (mic.available() < dataLength) {
                    Utils.sleep(1);
                    continue;
                }
                byte[] buff = new byte[dataLength];
                mic.read(buff, 0, buff.length);
                Utils.adjustVolumeMono(buff, Main.CLIENT_CONFIG.microphoneAmplification.get().floatValue());

                if (denoiser != null && VoicechatClient.CLIENT_CONFIG.denoiser.get()) {
                    buff = denoiser.denoise(buff);
                }

                micListener.onMicValue(Utils.dbToPerc(Utils.getHighestAudioLevel(buff)));

                gainControl.setValue(Math.min(Math.max(Utils.percentageToDB(Main.CLIENT_CONFIG.voiceChatVolume.get().floatValue()), gainControl.getMinimum()), gainControl.getMaximum()));

                speaker.write(buff, 0, buff.length);
            }
        }

        public void updateLastRender() {
            lastRender = System.currentTimeMillis();
        }

        public void close() {
            Main.LOGGER.info("Closing mic test audio channel");
            running = false;
            speaker.stop();
            speaker.flush();
            speaker.close();
            mic.stop();
            mic.flush();
            setMicLocked(false);
            micListener.onMicValue(0D);
            if (denoiser != null) {
                denoiser.close();
                denoiser = null;
            }
        }

    }

    public static interface MicListener {
        void onMicValue(double perc);
    }
}
