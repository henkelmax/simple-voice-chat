package de.maxhenkel.voicechat.gui.widgets;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.debug.VoicechatUncaughtExceptionHandler;
import de.maxhenkel.voicechat.voice.client.*;
import de.maxhenkel.voicechat.voice.client.speaker.Speaker;
import de.maxhenkel.voicechat.voice.client.speaker.SpeakerException;
import de.maxhenkel.voicechat.voice.client.speaker.SpeakerManager;
import de.maxhenkel.voicechat.voice.common.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;

public class MicTestButton extends ToggleImageButton implements ImageButton.TooltipSupplier {

    private static final ResourceLocation MICROPHONE = new ResourceLocation(Voicechat.MODID, "textures/icons/microphone_button.png");
    private static final ITextComponent TEST_DISABLED = new TextComponentTranslation("message.voicechat.mic_test.disabled");
    private static final ITextComponent TEST_ENABLED = new TextComponentTranslation("message.voicechat.mic_test.enabled");
    private static final ITextComponent TEST_UNAVAILABLE = new TextComponentTranslation("message.voicechat.mic_test_unavailable").setStyle(new Style().setColor(TextFormatting.RED));

    private boolean micActive;
    @Nullable
    private VoiceThread voiceThread;
    private final MicListener micListener;
    @Nullable
    private final ClientVoicechat client;

    public MicTestButton(int id, int xIn, int yIn, MicListener micListener) {
        super(id, xIn, yIn, MICROPHONE, null, null, null);
        this.micListener = micListener;
        this.client = ClientManager.getClient();
        // enabled = client == null || client.getSoundManager() != null;

        stateSupplier = () -> !micActive;
        tooltipSupplier = this;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        super.drawButton(mc, mouseX, mouseY, partialTicks);
        if (visible && voiceThread != null) {
            voiceThread.updateLastRender();
        }
    }

    public void setMicActive(boolean micActive) {
        this.micActive = micActive;
    }

    public boolean isHovered() {
        return hovered;
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
                Voicechat.LOGGER.error("Microphone error", e);
            }
        } else {
            close();
        }
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

    @Override
    public void onTooltip(ImageButton button, int mouseX, int mouseY) {
        GuiScreen screen = mc.currentScreen;
        if (screen == null) {
            return;
        }
        if (!enabled) {
            screen.drawHoveringText(TEST_UNAVAILABLE.getFormattedText(), mouseX, mouseY);
            return;
        }
        if (micActive) {
            screen.drawHoveringText(TEST_ENABLED.getFormattedText(), mouseX, mouseY);
        } else {
            screen.drawHoveringText(TEST_DISABLED.getFormattedText(), mouseX, mouseY);
        }
        GlStateManager.disableLighting();
    }

    private class VoiceThread extends Thread {

        private final MicActivator micActivator;
        private final Speaker speaker;
        private boolean running;
        private long lastRender;
        private MicThread micThread;
        private boolean usesOwnMicThread;
        // @Nullable
        // private SoundManager ownSoundManager;

        public VoiceThread() throws SpeakerException, MicrophoneException {
            this.running = true;
            setDaemon(true);
            setName("VoiceTestingThread");
            setUncaughtExceptionHandler(new VoicechatUncaughtExceptionHandler());

            micActivator = new MicActivator();

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

                if (VoicechatClient.CLIENT_CONFIG.microphoneActivationType.get().equals(MicrophoneActivationType.VOICE)) {
                    if (micActivator.push(buff, a -> {
                    })) {
                        play(buff);
                    }
                } else {
                    micActivator.stopActivating();
                    play(buff);
                }

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
            setMicActive(false);
            Voicechat.LOGGER.info("Mic test audio channel closed");
        }

        private void play(short[] buff) {
            speaker.play(buff, VoicechatClient.CLIENT_CONFIG.voiceChatVolume.get().floatValue(), null);
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
                Voicechat.LOGGER.warn("Failed to close microphone", e);
            }
        }
    }

    public interface MicListener {
        void onMicValue(double percentage);
    }
}
