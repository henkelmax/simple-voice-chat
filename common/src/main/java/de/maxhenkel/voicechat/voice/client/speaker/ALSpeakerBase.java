package de.maxhenkel.voicechat.voice.client.speaker;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.api.events.OpenALSoundEvent;
import de.maxhenkel.voicechat.plugins.PluginManager;
import de.maxhenkel.voicechat.voice.client.SoundManager;
import de.maxhenkel.voicechat.voice.common.NamedThreadPoolFactory;
import de.maxhenkel.voicechat.voice.common.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;

import javax.annotation.Nullable;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@Deprecated
public abstract class ALSpeakerBase implements Speaker {

    protected final Minecraft mc;
    protected final SoundManager soundManager;
    protected final int sampleRate;
    protected int bufferSize;
    protected int bufferSampleSize;
    protected int source;
    protected volatile int bufferIndex;
    protected final IntBuffer buffers;
    protected final ExecutorService executor;

    @Nullable
    protected UUID audioChannelId;

    public ALSpeakerBase(SoundManager soundManager, int sampleRate, int bufferSize, @Nullable UUID audioChannelId) {
        mc = Minecraft.getMinecraft();
        this.soundManager = soundManager;
        this.sampleRate = sampleRate;
        this.bufferSize = bufferSize;
        this.bufferSampleSize = bufferSize;
        this.audioChannelId = audioChannelId;
        this.buffers = BufferUtils.createIntBuffer(32);
        String threadName;
        if (audioChannelId == null) {
            threadName = "SoundSourceThread";
        } else {
            threadName = "SoundSourceThread-" + audioChannelId;
        }
        executor = Executors.newSingleThreadExecutor(NamedThreadPoolFactory.create(threadName));
    }

    @Override
    public void open() throws SpeakerException {
        runInContext(this::openSync);
    }

    protected void openSync() {
        if (hasValidSourceSync()) {
            return;
        }
        source = AL10.alGenSources();
        SoundManager.checkAlError();
        AL10.alSourcei(source, AL10.AL_LOOPING, AL10.AL_FALSE);
        SoundManager.checkAlError();

        AL10.alDistanceModel(AL11.AL_LINEAR_DISTANCE);
        SoundManager.checkAlError();
        AL10.alSourcef(source, AL10.AL_MAX_DISTANCE, Utils.getDefaultDistance());
        SoundManager.checkAlError();
        AL10.alSourcef(source, AL10.AL_REFERENCE_DISTANCE, 0F);
        SoundManager.checkAlError();

        AL10.alGenBuffers(buffers);
        SoundManager.checkAlError();
    }

    @Override
    public void play(short[] data, float volume, @Nullable Vec3d position, @Nullable String category, float maxDistance) {
        runInContext(() -> {
            removeProcessedBuffersSync();
            boolean stopped = isStoppedSync();
            if (stopped) {
                Voicechat.LOGGER.debug("Filling playback buffer {}", audioChannelId);
                for (int i = 0; i < getBufferSize(); i++) {
                    writeSync(new short[bufferSize], 1F, position, category, maxDistance);
                }
            }

            writeSync(data, volume, position, category, maxDistance);

            if (stopped) {
                AL10.alSourcePlay(source);
                SoundManager.checkAlError();
            }
        });
    }

    protected boolean isStoppedSync() {
        return getStateSync() == AL10.AL_INITIAL || getStateSync() == AL10.AL_STOPPED || getQueuedBuffersSync() <= 0;
    }

    protected int getBufferSize() {
        return VoicechatClient.CLIENT_CONFIG.outputBufferSize.get();
    }

    protected void writeSync(short[] data, float volume, @Nullable Vec3d position, @Nullable String category, float maxDistance) {
        PluginManager.instance().onALSound(source, audioChannelId, position, category, OpenALSoundEvent.Pre.class);
        setPositionSync(position, maxDistance);
        PluginManager.instance().onALSound(source, audioChannelId, position, category, OpenALSoundEvent.class);

        AL10.alSourcef(source, AL10.AL_MAX_GAIN, 6F);
        SoundManager.checkAlError();
        AL10.alSourcef(source, AL10.AL_GAIN, getVolume(volume, position, maxDistance));
        SoundManager.checkAlError();
        AL10.alListenerf(AL10.AL_GAIN, 1F);
        SoundManager.checkAlError();

        int queuedBuffers = getQueuedBuffersSync();
        if (queuedBuffers >= buffers.capacity()) {
            Voicechat.LOGGER.warn("Full playback buffer: {}/{}", queuedBuffers, buffers.capacity());
            int sampleOffset = AL10.alGetSourcei(source, AL11.AL_SAMPLE_OFFSET);
            SoundManager.checkAlError();
            int buffersToSkip = queuedBuffers - getBufferSize();
            AL10.alSourcei(source, AL11.AL_SAMPLE_OFFSET, sampleOffset + buffersToSkip * bufferSampleSize);
            SoundManager.checkAlError();
            removeProcessedBuffersSync();
        }

        AL10.alBufferData(buffers.get(bufferIndex), getFormat(), convert(data, position), sampleRate);
        SoundManager.checkAlError();
        AL10.alSourceQueueBuffers(source, buffers.get(bufferIndex));
        SoundManager.checkAlError();
        bufferIndex = (bufferIndex + 1) % buffers.capacity();

        PluginManager.instance().onALSound(source, audioChannelId, position, category, OpenALSoundEvent.Post.class);
    }

    protected float getVolume(float volume, @Nullable Vec3d position, float maxDistance) {
        return volume;
    }

    protected void linearAttenuation(float maxDistance) {
        AL10.alDistanceModel(AL11.AL_LINEAR_DISTANCE);
        SoundManager.checkAlError();

        AL10.alSourcef(source, AL10.AL_MAX_DISTANCE, maxDistance);
        SoundManager.checkAlError();

        AL10.alSourcef(source, AL10.AL_REFERENCE_DISTANCE, maxDistance / 2F);
        SoundManager.checkAlError();
    }

    protected abstract int getFormat();

    protected ShortBuffer convert(short[] data, @Nullable Vec3d position) {
        return toShortBuffer(data);
    }

    protected static ShortBuffer toShortBuffer(short[] data) {
        return BufferUtils.createShortBuffer(data.length).put(data);
    }

    protected void setPositionSync(@Nullable Vec3d soundPos, float maxDistance) {
        RenderManager renderManager = mc.getRenderManager();
        Vec3d position = new Vec3d(renderManager.viewerPosX, renderManager.viewerPosY, renderManager.viewerPosZ);
        Vec3d look = getVectorForRotation(renderManager.playerViewX, renderManager.playerViewY);
        // Vector3f up = camera.getUpVector();
        AL10.alListener3f(AL10.AL_POSITION, (float) position.x, (float) position.y, (float) position.z);
        SoundManager.checkAlError();
        // TODO check
        Vec3d up = look.rotatePitch(-90F);
        FloatBuffer floatBuffer = BufferUtils.createFloatBuffer(7);
        floatBuffer.put(new float[]{(float) look.x, (float) look.y, (float) look.z, (float) up.x, (float) up.y, (float) up.z});
        floatBuffer.flip();
        AL10.alListener(AL10.AL_ORIENTATION, floatBuffer);
        // AL10.alListenerfv(AL10.AL_ORIENTATION, new float[]{look.x(), look.y(), look.z(), up.x(), up.y(), up.z()});
        SoundManager.checkAlError();
        if (soundPos != null) {
            linearAttenuation(maxDistance);
            AL10.alSourcei(source, AL10.AL_SOURCE_RELATIVE, AL10.AL_FALSE);
            SoundManager.checkAlError();
            AL10.alSource3f(source, AL10.AL_POSITION, (float) soundPos.x, (float) soundPos.y, (float) soundPos.z);
            SoundManager.checkAlError();
        } else {
            linearAttenuation(48F);
            AL10.alSourcei(source, AL10.AL_SOURCE_RELATIVE, AL10.AL_TRUE);
            SoundManager.checkAlError();
            AL10.alSource3f(source, AL10.AL_POSITION, 0F, 0F, 0F);
            SoundManager.checkAlError();
        }
    }

    //TODO check
    protected final Vec3d getVectorForRotation(float pitch, float yaw) {
        float f = MathHelper.cos(-yaw * 0.017453292F - (float) Math.PI);
        float f1 = MathHelper.sin(-yaw * 0.017453292F - (float) Math.PI);
        float f2 = -MathHelper.cos(-pitch * 0.017453292F);
        float f3 = MathHelper.sin(-pitch * 0.017453292F);
        return new Vec3d((double) (f1 * f2), (double) f3, (double) (f * f2));
    }

    @Override
    public void close() {
        runInContext(this::closeSync);
    }

    protected void closeSync() {
        if (hasValidSourceSync()) {
            if (getStateSync() == AL10.AL_PLAYING) {
                AL10.alSourceStop(source);
                SoundManager.checkAlError();
            }

            AL10.alDeleteSources(source);
            SoundManager.checkAlError();
            AL10.alDeleteBuffers(buffers);
            SoundManager.checkAlError();
        }
        source = 0;
        executor.shutdown();
    }

    public void checkBufferEmpty(Runnable onEmpty) {
        runInContext(() -> {
            if (getStateSync() == AL10.AL_STOPPED || getQueuedBuffersSync() <= 0) {
                onEmpty.run();
            }
        });
    }

    protected void removeProcessedBuffersSync() {
        int processed = AL10.alGetSourcei(source, AL10.AL_BUFFERS_PROCESSED);
        SoundManager.checkAlError();
        for (int i = 0; i < processed; i++) {
            AL10.alSourceUnqueueBuffers(source);
            SoundManager.checkAlError();
        }
    }

    protected int getStateSync() {
        int state = AL10.alGetSourcei(source, AL10.AL_SOURCE_STATE);
        SoundManager.checkAlError();
        return state;
    }

    protected int getQueuedBuffersSync() {
        int buffers = AL10.alGetSourcei(source, AL10.AL_BUFFERS_QUEUED);
        SoundManager.checkAlError();
        return buffers;
    }

    protected boolean hasValidSourceSync() {
        boolean validSource = AL10.alIsSource(source);
        SoundManager.checkAlError();
        return validSource;
    }

    public void runInContext(Runnable runnable) {
        if (executor.isShutdown()) {
            return;
        }
        soundManager.runInContext(executor, runnable);
    }

    public void fetchQueuedBuffersAsync(Consumer<Integer> supplier) {
        runInContext(() -> {
            if (isStoppedSync()) {
                supplier.accept(-1);
                return;
            }
            supplier.accept(getQueuedBuffersSync());
        });
    }

}
