package de.maxhenkel.voicechat.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.debug.VoicechatUncaughtExceptionHandler;
import de.maxhenkel.voicechat.voice.client.*;
import de.maxhenkel.voicechat.voice.client.speaker.Speaker;
import de.maxhenkel.voicechat.voice.client.speaker.SpeakerException;
import de.maxhenkel.voicechat.voice.client.speaker.SpeakerManager;
import de.maxhenkel.voicechat.voice.common.AudioUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class MicTestButton extends ToggleImageButton implements ImageButton.TooltipSupplier {

    private static final ResourceLocation MICROPHONE = new ResourceLocation(Voicechat.MODID, "textures/icons/microphone_button.png");
    private static final Component TEST_DISABLED = Component.translatable("message.voicechat.mic_test.disabled");
    private static final Component TEST_ENABLED = Component.translatable("message.voicechat.mic_test.enabled");
    private static final Component TEST_UNAVAILABLE = Component.translatable("message.voicechat.mic_test_unavailable").withStyle(ChatFormatting.RED);

    private boolean micActive;
    @Nullable
    private VoiceThread voiceThread;
    @Nullable
    private final MicListener micListener;
    private final boolean raw;
    @Nullable
    private final ClientVoicechat client;

    public MicTestButton(int xIn, int yIn, boolean raw, @Nullable MicListener micListener) {
        super(xIn, yIn, MICROPHONE, null, null, null);
        this.raw = raw;
        this.micListener = micListener;
        this.client = ClientManager.getClient();
        active = client == null || client.getSoundManager() != null;

        stateSupplier = () -> !micActive;
        tooltipSupplier = this;
    }

    public MicTestButton(int xIn, int yIn, boolean raw) {
        this(xIn, yIn, raw, null);
    }

    @Override
    public void render(PoseStack matrixStack, int x, int y, float partialTicks) {
        super.render(matrixStack, x, y, partialTicks);
        updateLastRender();
    }

    public void updateLastRender() {
        if (visible && voiceThread != null) {
            voiceThread.updateLastRender();
        }
    }

    @Override
    protected boolean shouldRenderTooltip() {
        return false;
    }

    public void setMicActive(boolean micActive) {
        this.micActive = micActive;
    }

    public boolean isHovered() {
        return isHovered;
    }

    public boolean isMicActive() {
        return micActive;
    }

    @Override
    public void onPress() {
        setMicActive(!micActive);
        if (micActive) {
            close();
            try {
                voiceThread = new VoiceThread(e -> {
                    setMicActive(false);
                    active = false;
                    Voicechat.LOGGER.error("Microphone error", e);
                });
                voiceThread.start();
            } catch (Exception e) {
                setMicActive(false);
                active = false;
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
    public void updateNarration(NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }

    @Override
    public void onTooltip(ImageButton button, PoseStack matrices, int mouseX, int mouseY) {
        Screen screen = mc.screen;
        if (screen == null) {
            return;
        }
        if (!active) {
            screen.renderTooltip(matrices, TEST_UNAVAILABLE, mouseX, mouseY);
            return;
        }
        if (micActive) {
            screen.renderTooltip(matrices, TEST_ENABLED, mouseX, mouseY);
        } else {
            screen.renderTooltip(matrices, TEST_DISABLED, mouseX, mouseY);
        }
    }

    private class VoiceThread extends Thread {

        private final Speaker speaker;
        private boolean running;
        private long lastRender;
        private MicThread micThread;
        private boolean usesOwnMicThread;
        @Nullable
        private SoundManager ownSoundManager;

        public VoiceThread(Consumer<MicrophoneException> onMicError) throws SpeakerException {
            this.running = true;
            setDaemon(true);
            setName("VoiceTestingThread");
            setUncaughtExceptionHandler(new VoicechatUncaughtExceptionHandler());

            micThread = client != null ? client.getMicThread() : null;
            if (micThread == null) {
                micThread = new MicThread(client, null, onMicError);
                usesOwnMicThread = true;
            } else {
                micThread.getError(onMicError);
            }

            SoundManager soundManager;
            if (client == null) {
                soundManager = new SoundManager();
                ownSoundManager = soundManager;
            } else {
                soundManager = client.getSoundManager();
            }

            if (soundManager == null) {
                throw new SpeakerException("No sound manager");
            }

            speaker = SpeakerManager.createSpeaker(soundManager, null);

            updateLastRender();
            setMicLocked(true);
        }

        @Override
        public void run() {
            while (running) {
                if (System.currentTimeMillis() - lastRender > 500L) {
                    break;
                }
                if (micThread.isClosed()) {
                    break;
                }
                short[] buff = raw ? micThread.pollMic() : micThread.pollProcessedAudio(true);
                if (buff == null) {
                    continue;
                }

                if (micListener != null) {
                    micListener.onMicValue(AudioUtils.getHighestAudioLevel(buff));
                }

                if (raw || micThread.shouldTransmitAudio()) {
                    play(buff);
                }
            }
            speaker.close();
            setMicLocked(false);
            if (micListener != null) {
                micListener.onStop();
            }
            if (usesOwnMicThread) {
                micThread.close();
            }
            if (ownSoundManager != null) {
                ownSoundManager.close();
            }
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
        void onMicValue(double dB);

        void onStop();
    }
}
