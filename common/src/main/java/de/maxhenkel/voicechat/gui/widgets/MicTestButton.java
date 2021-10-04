package de.maxhenkel.voicechat.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.intercompatibility.ClientCompatibilityManager;
import de.maxhenkel.voicechat.voice.client.*;
import de.maxhenkel.voicechat.voice.common.Utils;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import javax.annotation.Nullable;

public class MicTestButton extends AbstractButton {

    private boolean micActive;
    @Nullable
    private VoiceThread voiceThread;
    private final MicListener micListener;
    @Nullable
    private final ClientVoicechat client;

    public MicTestButton(int xIn, int yIn, int widthIn, int heightIn, MicListener micListener) {
        super(xIn, yIn, widthIn, heightIn, TextComponent.EMPTY);
        this.micListener = micListener;
        this.client = ClientManager.getClient();
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
                voiceThread = null;
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
            if (voiceThread != null) {
                voiceThread.close();
                voiceThread = null;
            }
        }
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }

    private class VoiceThread extends Thread {

        private final ALMicrophone mic;
        private final ALSpeaker speaker;
        private final VolumeManager volumeManager;
        private boolean running;
        private long lastRender;
        private MicThread micThread;
        private boolean usesOwnMicThread;
        @Nullable
        private SoundManager ownSoundManager;


        public VoiceThread() throws SpeakerException, MicrophoneException, NativeDependencyException {
            this.running = true;
            setDaemon(true);
            setName("VoiceTestingThread");

            micThread = client != null ? client.getMicThread() : null;
            if (micThread == null) {
                micThread = new MicThread(client, null);
                usesOwnMicThread = true;
            }

            mic = micThread.getMic();
            volumeManager = micThread.getVolumeManager();
            SoundManager soundManager;
            if (client == null) {
                soundManager = ClientCompatibilityManager.INSTANCE.createSoundManager(VoicechatClient.CLIENT_CONFIG.speaker.get());
                ownSoundManager = soundManager;
            } else {
                soundManager = client.getSoundManager();
            }
            speaker = ClientCompatibilityManager.INSTANCE.createSpeaker(soundManager, SoundManager.SAMPLE_RATE, SoundManager.FRAME_SIZE);

            speaker.open();

            updateLastRender();
            setMicLocked(true);
        }

        @Override
        public void run() {
            while (running) {
                if (System.currentTimeMillis() - lastRender > 500L) {
                    break;
                }
                mic.start();
                if (mic.available() < SoundManager.FRAME_SIZE) {
                    Utils.sleep(1);
                    continue;
                }
                short[] buff = new short[SoundManager.FRAME_SIZE];
                mic.read(buff);
                volumeManager.adjustVolumeMono(buff, VoicechatClient.CLIENT_CONFIG.microphoneAmplification.get().floatValue());

                if (micThread.getDenoiser() != null && VoicechatClient.CLIENT_CONFIG.denoiser.get()) {
                    buff = micThread.getDenoiser().denoise(buff);
                }

                micListener.onMicValue(Utils.dbToPerc(Utils.getHighestAudioLevel(buff)));

                speaker.write(buff, VoicechatClient.CLIENT_CONFIG.voiceChatVolume.get().floatValue(), null);
            }
            speaker.close();
            mic.stop();
            setMicLocked(false);
            micListener.onMicValue(0D);
            if (usesOwnMicThread) {
                micThread.close();
            }
            if (ownSoundManager != null) {
                ownSoundManager.close();
            }
            Voicechat.LOGGER.info("Mic test audio channel closed");
        }

        public void updateLastRender() {
            lastRender = System.currentTimeMillis();
        }

        private void setMicLocked(boolean locked) {
            if (micThread == null) {
                return;
            }
            micThread.setMicrophoneLocked(locked);
        }

        public void close() {
            if (!running) {
                return;
            }
            Voicechat.LOGGER.info("Stopping mic test audio channel");
            running = false;
            try {
                join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public interface MicListener {
        void onMicValue(double percentage);
    }
}
