package de.maxhenkel.voicechat.voice.client;

import com.mojang.math.Vector3f;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.openal.AL11;

import javax.annotation.Nullable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class ALSpeaker {

    protected final Minecraft mc;
    protected final SoundManager soundManager;
    protected final int sampleRate;
    protected final int bufferSize;
    protected final int bufferSampleSize;
    protected int source;
    protected int bufferIndex;
    protected final int[] buffers;
    private final ExecutorService executor;

    public ALSpeaker(SoundManager soundManager, int sampleRate, int bufferSize) {
        mc = Minecraft.getInstance();
        this.soundManager = soundManager;
        this.sampleRate = sampleRate;
        this.bufferSize = bufferSize;
        this.bufferSampleSize = bufferSize;
        this.buffers = new int[32];
        executor = Executors.newSingleThreadExecutor();
    }

    public void open() throws SpeakerException {
        runInContext(this::openSync);
    }

    private void openSync() {
        if (hasValidSourceSync()) {
            return;
        }
        source = AL11.alGenSources();
        SoundManager.checkAlError();
        AL11.alSourcei(source, AL11.AL_LOOPING, AL11.AL_FALSE);
        SoundManager.checkAlError();
        AL11.alDistanceModel(AL11.AL_NONE);
        SoundManager.checkAlError();
        AL11.alGenBuffers(buffers);
        SoundManager.checkAlError();
    }

    public void close() {
        runInContext(this::closeSync);
    }

    public void closeSync() {
        if (hasValidSourceSync()) {
            if (getStateSync() == AL11.AL_PLAYING) {
                AL11.alSourceStop(source);
                SoundManager.checkAlError();
            }

            AL11.alDeleteSources(source);
            SoundManager.checkAlError();
            AL11.alDeleteBuffers(buffers);
            SoundManager.checkAlError();
        }
        source = 0;
        executor.shutdown();
    }

    protected void setPositionSync(@Nullable Vec3 soundPos) {
        if (soundPos != null) {
            Camera camera = mc.gameRenderer.getMainCamera();
            Vec3 position = camera.getPosition();
            Vector3f look = camera.getLookVector();
            Vector3f up = camera.getUpVector();
            AL11.alListener3f(AL11.AL_POSITION, (float) position.x, (float) position.y, (float) position.z);
            SoundManager.checkAlError();
            AL11.alListenerfv(AL11.AL_ORIENTATION, new float[]{look.x(), look.y(), look.z(), up.x(), up.y(), up.z()});
            SoundManager.checkAlError();
            AL11.alSource3f(source, AL11.AL_POSITION, (float) soundPos.x, (float) soundPos.y, (float) soundPos.z);
            SoundManager.checkAlError();
        } else {
            AL11.alListener3f(AL11.AL_POSITION, 0F, 0F, 0F);
            SoundManager.checkAlError();
            AL11.alListenerfv(AL11.AL_ORIENTATION, new float[]{0F, 0F, -1F, 0F, 1F, 0F});
            SoundManager.checkAlError();
            AL11.alSource3f(source, AL11.AL_POSITION, 0F, 0F, 0F);
            SoundManager.checkAlError();
        }
    }

    public void checkBufferEmpty(Runnable onEmpty) {
        runInContext(() -> {
            if (getStateSync() == AL11.AL_STOPPED || getQueuedBuffersSync() <= 0) {
                onEmpty.run();
            }
        });
    }

    public void write(short[] data, float volume, Vec3 position) {
        runInContext(() -> {
            removeProcessedBuffersSync();
            int buffers = getQueuedBuffersSync();
            boolean stopped = getStateSync() == AL11.AL_INITIAL || getStateSync() == AL11.AL_STOPPED || buffers <= 1;
            if (stopped) {
                for (int i = 0; i < VoicechatClient.CLIENT_CONFIG.outputBufferSize.get(); i++) {
                    writeSync(new short[bufferSampleSize], 1F, null);
                }
            }

            writeSync(data, volume, position);

            if (stopped) {
                AL11.alSourcePlay(source);
                SoundManager.checkAlError();
            }
        });
    }

    private void writeSync(short[] data, float volume, @Nullable Vec3 position) {
        setPositionSync(position);

        AL11.alSourcef(source, AL11.AL_MAX_GAIN, 6F);
        SoundManager.checkAlError();
        AL11.alSourcef(source, AL11.AL_GAIN, volume);
        SoundManager.checkAlError();
        AL11.alListenerf(AL11.AL_GAIN, 1F);
        SoundManager.checkAlError();

        int queuedBuffers = getQueuedBuffersSync();
        if (queuedBuffers >= buffers.length) {
            Voicechat.LOGGER.warn("Full playback buffer: {}/{}", queuedBuffers, buffers.length);
            int sampleOffset = AL11.alGetSourcei(source, AL11.AL_SAMPLE_OFFSET);
            SoundManager.checkAlError();
            int buffersToSkip = queuedBuffers - VoicechatClient.CLIENT_CONFIG.outputBufferSize.get();
            AL11.alSourcei(source, AL11.AL_SAMPLE_OFFSET, sampleOffset + buffersToSkip * bufferSampleSize);
            SoundManager.checkAlError();
            removeProcessedBuffersSync();
        }

        AL11.alBufferData(buffers[bufferIndex], AL11.AL_FORMAT_MONO16, data, sampleRate);
        SoundManager.checkAlError();
        AL11.alSourceQueueBuffers(source, buffers[bufferIndex]);
        SoundManager.checkAlError();
        bufferIndex = (bufferIndex + 1) % buffers.length;
    }

    private void removeProcessedBuffersSync() {
        int processed = AL11.alGetSourcei(source, AL11.AL_BUFFERS_PROCESSED);
        SoundManager.checkAlError();
        for (int i = 0; i < processed; i++) {
            AL11.alSourceUnqueueBuffers(source);
            SoundManager.checkAlError();
        }
    }

    private int getStateSync() {
        int state = AL11.alGetSourcei(source, AL11.AL_SOURCE_STATE);
        SoundManager.checkAlError();
        return state;
    }

    private int getQueuedBuffersSync() {
        int buffers = AL11.alGetSourcei(source, AL11.AL_BUFFERS_QUEUED);
        SoundManager.checkAlError();
        return buffers;
    }

    private boolean hasValidSourceSync() {
        boolean validSource = AL11.alIsSource(source);
        SoundManager.checkAlError();
        return validSource;
    }

    public void runInContext(Runnable runnable) {
        soundManager.runInContext(executor, runnable);
    }

}
