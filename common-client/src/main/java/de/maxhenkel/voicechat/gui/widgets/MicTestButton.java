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
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class MicTestButton extends ToggleImageButton implements ImageButton.TooltipSupplier {

    private static final ResourceLocation MICROPHONE = new ResourceLocation(Voicechat.MODID, "textures/icons/microphone_button.png");
    private static final Component TEST_DISABLED = new TranslatableComponent("message.voicechat.mic_test.disabled");
    private static final Component TEST_ENABLED = new TranslatableComponent("message.voicechat.mic_test.enabled");
    private static final Component TEST_UNAVAILABLE = new TranslatableComponent("message.voicechat.mic_test_unavailable").withStyle(ChatFormatting.RED);

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
            voiceThread = new VoiceThread(e -> {
                setMicActive(false);
                active = false;
                Voicechat.LOGGER.error("Microphone testing error", e);
            });
            voiceThread.start();
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

        private boolean running;
        private long lastRender;
        private MicThread micThread;
        private boolean usesOwnMicThread;
        private final Consumer<Exception> onError;

        public VoiceThread(Consumer<Exception> onError) {
            this.onError = onError;
            this.running = true;
            setDaemon(true);
            setName("VoiceTestingThread");
            setUncaughtExceptionHandler(new VoicechatUncaughtExceptionHandler());

            micThread = client != null ? client.getMicThread() : null;
            if (micThread == null) {
                micThread = new MicThread(client, null, onError::accept);
                usesOwnMicThread = true;
            } else {
                micThread.getError(onError::accept);
            }

            updateLastRender();
            setMicLocked(true);
        }

        @Override
        public void run() {
            SoundManager soundManager = client != null ? client.getSoundManager() : null;
            boolean ownSoundManager = soundManager == null;
            Speaker speaker = null;
            try {
                if (ownSoundManager) {
                    soundManager = SoundManager.create();
                }
                speaker = SpeakerManager.createSpeaker(soundManager, null);

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
                        speaker.play(buff, VoicechatClient.CLIENT_CONFIG.voiceChatVolume.get().floatValue(), null);
                    }
                }
            } catch (SpeakerException e) {
                onError.accept(e);
            } finally {
                if (speaker != null) {
                    speaker.close();
                }
                if (ownSoundManager && soundManager != null) {
                    soundManager.close();
                }
                setMicLocked(false);
                if (micListener != null) {
                    micListener.onStop();
                }
                if (usesOwnMicThread) {
                    micThread.close();
                }
                setMicActive(false);
                Voicechat.LOGGER.info("Mic test audio channel closed");
            }
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
