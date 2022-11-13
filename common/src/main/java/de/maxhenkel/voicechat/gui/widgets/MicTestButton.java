package de.maxhenkel.voicechat.gui.widgets;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.voice.client.*;
import de.maxhenkel.voicechat.voice.client.speaker.*;
import de.maxhenkel.voicechat.voice.common.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nullable;

public class MicTestButton extends ButtonBase {

    private static final ITextComponent TEST_UNAVAILABLE = new TextComponentTranslation("message.voicechat.mic_test_unavailable");
    private static final ITextComponent TEST_ON = new TextComponentTranslation("message.voicechat.mic_test_on");
    private static final ITextComponent TEST_OFF = new TextComponentTranslation("message.voicechat.mic_test_off");

    private boolean micActive;
    @Nullable
    private VoiceThread voiceThread;
    private final MicListener micListener;
    @Nullable
    private final ClientVoicechat client;

    public MicTestButton(int id, int xIn, int yIn, int widthIn, int heightIn, MicListener micListener) {
        super(id, xIn, yIn, widthIn, heightIn, new TextComponentString(""));
        this.micListener = micListener;
        this.client = ClientManager.getClient();
        // enabled = client == null || client.getSoundManager() != null;
        updateText();
    }

    private void updateText() {
        if (!enabled) {
            displayString = TEST_UNAVAILABLE.getUnformattedComponentText();
            return;
        }
        if (micActive) {
            displayString = TEST_ON.getUnformattedComponentText();
        } else {
            displayString = TEST_OFF.getUnformattedComponentText();
        }
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        super.drawButton(mc, mouseX, mouseY, partialTicks);
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
        if (micActive) {
            close();
            try {
                voiceThread = new VoiceThread();
                voiceThread.start();
            } catch (Exception e) {
                setMicActive(false);
                enabled = false;
                e.printStackTrace();
            }
        } else {
            close();
        }
        updateText();
    }

    private void close() {
        if (voiceThread != null) {
            voiceThread.close();
            voiceThread = null;
        }
    }

    public void stop() {
        close();
        setMicActive(false);
    }

    private class VoiceThread extends Thread {

        private final Speaker speaker;
        private boolean running;
        private long lastRender;
        private MicThread micThread;
        private boolean usesOwnMicThread;
        // @Nullable
        // private SoundManager ownSoundManager;

        public VoiceThread() throws SpeakerException, MicrophoneException, NativeDependencyException {
            this.running = true;
            setDaemon(true);
            setName("VoiceTestingThread");

            micThread = client != null ? client.getMicThread() : null;
            if (micThread == null) {
                micThread = new MicThread(client, null);
                usesOwnMicThread = true;
            }

            // SoundManager soundManager;
            // if (client == null) {
            //     soundManager = new SoundManager(VoicechatClient.CLIENT_CONFIG.speaker.get());
            //     ownSoundManager = soundManager;
            // } else {
            //     soundManager = client.getSoundManager();
            // }

            // if (soundManager == null) {
            //     throw new SpeakerException("No sound manager");
            // }

            speaker = SpeakerManager.createSpeaker(null/*soundManager*/, null);

            updateLastRender();
            setMicLocked(true);
        }

        @Override
        public void run() {
            while (running) {
                if (System.currentTimeMillis() - lastRender > 500L) {
                    break;
                }
                short[] buff = micThread.pollMic();
                if (buff == null) {
                    continue;
                }

                micListener.onMicValue(Utils.dbToPerc(Utils.getHighestAudioLevel(buff)));

                speaker.play(buff, VoicechatClient.CLIENT_CONFIG.voiceChatVolume.get().floatValue(), null);
            }
            speaker.close();
            setMicLocked(false);
            micListener.onMicValue(0D);
            if (usesOwnMicThread) {
                micThread.close();
            }
            // if (ownSoundManager != null) {
            //     ownSoundManager.close();
            // }
            Voicechat.LOGGER.info("Mic test audio channel closed");
        }

        public void updateLastRender() {
            lastRender = System.currentTimeMillis();
        }

        private void setMicLocked(boolean locked) {
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
