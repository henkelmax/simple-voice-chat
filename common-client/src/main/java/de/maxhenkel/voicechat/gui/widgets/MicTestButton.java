package de.maxhenkel.voicechat.gui.widgets;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.debug.VoicechatUncaughtExceptionHandler;
import de.maxhenkel.voicechat.voice.client.*;
import de.maxhenkel.voicechat.voice.client.speaker.Speaker;
import de.maxhenkel.voicechat.voice.client.speaker.SpeakerException;
import de.maxhenkel.voicechat.voice.client.speaker.SpeakerManager;
import de.maxhenkel.voicechat.voice.common.AudioUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class MicTestButton extends ToggleImageButton implements ImageButton.TooltipSupplier {

    private static final ResourceLocation MICROPHONE = new ResourceLocation(Voicechat.MODID, "textures/icons/microphone_button.png");
    private static final ITextComponent TEST_DISABLED = new TextComponentTranslation("message.voicechat.mic_test.disabled");
    private static final ITextComponent TEST_ENABLED = new TextComponentTranslation("message.voicechat.mic_test.enabled");
    private static final ITextComponent TEST_UNAVAILABLE = new TextComponentTranslation("message.voicechat.mic_test_unavailable").setStyle(new Style().setColor(TextFormatting.RED));

    private boolean micActive;
    @Nullable
    private VoiceThread voiceThread;
    @Nullable
    private final MicListener micListener;
    private final boolean raw;
    @Nullable
    private final ClientVoicechat client;

    public MicTestButton(int id, int xIn, int yIn, boolean raw, @Nullable MicListener micListener) {
        super(id, xIn, yIn, MICROPHONE, null, null, null);
        this.raw = raw;
        this.micListener = micListener;
        this.client = ClientManager.getClient();
        // enabled = client == null || client.getSoundManager() != null;

        stateSupplier = () -> !micActive;
        tooltipSupplier = this;
    }

    public MicTestButton(int id, int xIn, int yIn, boolean raw) {
        this(id, xIn, yIn, raw, null);
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        super.drawButton(mc, mouseX, mouseY, partialTicks);
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
        return hovered;
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
                enabled = false;
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
            // SoundManager soundManager = client != null ? client.getSoundManager() : null;
            // boolean ownSoundManager = soundManager == null;
            Speaker speaker = null;
            try {
                // if (ownSoundManager) {
                //     soundManager = SoundManager.create();
                // }
                speaker = SpeakerManager.createSpeaker(null/*soundManager*/, null);

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
                // if (ownSoundManager && soundManager != null) {
                //     soundManager.close();
                // }
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
