package de.maxhenkel.voicechat.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.voice.client.*;
import de.maxhenkel.voicechat.voice.client.Denoiser;
import de.maxhenkel.voicechat.voice.common.Utils;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import javax.annotation.Nullable;

public class MicTestButton extends AbstractButton {

    private boolean micActive;
    private VoiceThread voiceThread;
    private final MicListener micListener;
    private final Client client;

    public MicTestButton(int xIn, int yIn, int widthIn, int heightIn, MicListener micListener, Client client) {
        super(xIn, yIn, widthIn, heightIn, TextComponent.EMPTY);
        this.micListener = micListener;
        this.client = client;
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
            if (voiceThread != null && !voiceThread.isClosed()) {
                voiceThread.close();
            }
            try {
                voiceThread = new VoiceThread();
                voiceThread.start();
            } catch (Exception e) {
                setMicActive(false);
                active = false;
                e.printStackTrace();
            }
        } else {
            if (voiceThread != null && !voiceThread.isClosed()) {
                voiceThread.close();
            }
        }
    }

    private ALMicrophone getMic() {
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

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }

    private class VoiceThread extends Thread {

        private final ALMicrophone mic;
        private final ALSpeaker speaker;
        private boolean running;
        private long lastRender;
        @Nullable
        private Denoiser denoiser;

        public VoiceThread() throws SpeakerException, MicrophoneException {
            this.running = true;
            setDaemon(true);
            mic = getMic();
            if (mic == null) {
                throw new MicrophoneException("No microphone");
            }
            speaker = new ALSpeaker(client.getSoundManager(), SoundManager.SAMPLE_RATE, SoundManager.FRAME_SIZE);

            speaker.open();

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
                if (mic.available() < SoundManager.FRAME_SIZE) {
                    Utils.sleep(1);
                    continue;
                }
                short[] buff = new short[SoundManager.FRAME_SIZE];
                mic.read(buff);
                Utils.adjustVolumeMono(buff, VoicechatClient.CLIENT_CONFIG.microphoneAmplification.get().floatValue());

                if (denoiser != null && VoicechatClient.CLIENT_CONFIG.denoiser.get()) {
                    buff = denoiser.denoise(buff);
                }

                micListener.onMicValue(Utils.dbToPerc(Utils.getHighestAudioLevel(buff)));

                speaker.write(buff, VoicechatClient.CLIENT_CONFIG.voiceChatVolume.get().floatValue(), null);
            }
        }

        public void updateLastRender() {
            lastRender = System.currentTimeMillis();
        }

        public boolean isClosed() {
            return !running;
        }

        public void close() {
            Voicechat.LOGGER.info("Closing mic test audio channel");
            running = false;
            speaker.close();
            mic.stop();
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
