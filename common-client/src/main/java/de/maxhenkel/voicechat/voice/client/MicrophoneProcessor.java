package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.natives.Agc;
import de.maxhenkel.voicechat.natives.Denoiser;
import de.maxhenkel.voicechat.natives.RNNoiseManager;
import de.maxhenkel.voicechat.natives.SpeexManager;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public abstract class MicrophoneProcessor {

    public static final float AGC_PROBABILITY = 0.95F;

    private final MicActivator micActivator;
    private final MicActivator whisperMicActivator;
    private final VolumeManager volumeManager;
    private boolean whispering;
    private boolean activating;
    protected float speechProbability;
    @Nullable
    private Denoiser denoiser;
    @Nullable
    private Agc agc;

    public MicrophoneProcessor() {
        micActivator = new MicActivator(this::getDeactivationDelay);
        whisperMicActivator = new MicActivator(VoicechatClient.CLIENT_CONFIG.pttDeactivationDelay::get);
        volumeManager = new VolumeManager();
        denoiser = RNNoiseManager.createDenoiser();
        if (denoiser == null) {
            Voicechat.LOGGER.warn("Denoiser not available");
        }
        agc = SpeexManager.createAgc();
        if (agc == null) {
            Voicechat.LOGGER.warn("AGC not available");
        }
    }

    public abstract int getDeactivationDelay();

    protected void preprocess(short[] audio) {
        Denoiser denoiser = getDenoiser();
        if (denoiser != null) {
            if (useDenoiser()) {
                speechProbability = denoiser.denoiseInPlace(audio);
            } else {
                speechProbability = denoiser.getSpeechProbability(audio);
            }
        } else {
            speechProbability = 1F;
        }
        Agc agc = getAgc();
        if (useAgc() && agc != null) {
            if (speechProbability >= AGC_PROBABILITY && shouldAdjustGain()) {
                agc.setIncrement(12);
            } else {
                agc.setIncrement(0);
            }
            agc.agc(audio);
        } else {
            volumeManager.adjustVolume(audio, VoicechatClient.CLIENT_CONFIG.microphoneGain.get());
        }
    }

    public void process(short[] audio, boolean testing) {
        boolean w = isWhisperButtonDown();
        preprocess(audio);
        boolean a = processInternal(audio, testing);
        activating = micActivator.shouldStillSend(a);
        if (a) {
            whispering = w;
            whisperMicActivator.reset();
        } else {
            whispering = !isMuted() && whisperMicActivator.shouldStillSend(w);
        }
    }

    protected abstract boolean processInternal(short[] audio, boolean testing);

    protected abstract boolean shouldAdjustGain();

    public abstract MicrophoneActivationType getActivationType();

    public float getSpeechProbability() {
        return speechProbability;
    }

    public boolean isWhispering() {
        return whispering;
    }

    public boolean shouldTransmitAudio() {
        return activating || whispering;
    }

    public void reset() {
        micActivator.reset();
        whisperMicActivator.reset();
        whispering = false;
        activating = false;
    }

    public void close() {
        if (denoiser != null) {
            denoiser.close();
        }
        if (agc != null) {
            agc.close();
        }
    }

    public boolean isMuted() {
        return ClientManager.getPlayerStateManager().isMuted();
    }

    public boolean isAnyTalkButtonDown() {
        return ClientManager.getPttKeyHandler().isAnyDown();
    }

    public boolean isPttButtonDown() {
        return ClientManager.getPttKeyHandler().isPTTDown();
    }

    public boolean isWhisperButtonDown() {
        return ClientManager.getPttKeyHandler().isWhisperDown();
    }

    public boolean useAgc() {
        return VoicechatClient.CLIENT_CONFIG.agc.get();
    }

    public boolean useDenoiser() {
        return VoicechatClient.CLIENT_CONFIG.denoiser.get();
    }

    public boolean agcAvailable() {
        return agc != null;
    }

    public boolean denoiserAvailable() {
        return denoiser != null;
    }

    @Nullable
    protected Denoiser getDenoiser() {
        if (denoiser != null && denoiser.isClosed()) {
            denoiser = RNNoiseManager.createDenoiser();
        }
        return denoiser;
    }

    @Nullable
    protected Agc getAgc() {
        if (agc != null && agc.isClosed()) {
            agc = SpeexManager.createAgc();
        }
        return agc;
    }

    protected static class MicActivator {

        private final Supplier<Integer> delay;
        private int deactivationDelay;

        public MicActivator(Supplier<Integer> delay) {
            this.delay = delay;
        }

        public boolean shouldStillSend(boolean activating) {
            if (activating) {
                deactivationDelay = delay.get();
                return true;
            } else {
                if (deactivationDelay > 0) {
                    deactivationDelay--;
                    return true;
                } else {
                    return false;
                }
            }
        }

        public void reset() {
            deactivationDelay = 0;
        }
    }

}
