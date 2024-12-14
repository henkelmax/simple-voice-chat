package de.maxhenkel.voicechat.voice.client.speaker;

import de.maxhenkel.voicechat.voice.client.PositionalAudioUtils;
import de.maxhenkel.voicechat.voice.client.SoundManager;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.openal.AL10;

import javax.annotation.Nullable;
import java.nio.ShortBuffer;
import java.util.UUID;

@Deprecated
public class FakeALSpeaker extends ALSpeakerBase {

    public FakeALSpeaker(SoundManager soundManager, int sampleRate, int bufferSize, @Nullable UUID audioChannelId) {
        super(soundManager, sampleRate, bufferSize, audioChannelId);
        this.bufferSize *= 2;
    }

    @Override
    protected void openSync() {
        super.openSync();
        AL10.alDistanceModel(AL10.AL_NONE);
        SoundManager.checkAlError();
    }

    @Override
    protected ShortBuffer convert(short[] data, @Nullable Vec3d position) {
        return toShortBuffer(PositionalAudioUtils.convertToStereo(data, position));
    }

    @Override
    protected int getFormat() {
        return AL10.AL_FORMAT_STEREO16;
    }

    @Override
    protected void setPositionSync(@Nullable Vec3d soundPos, float maxDistance) {

    }

    @Override
    protected float getVolume(float volume, @Nullable Vec3d position, float maxDistance) {
        if (position == null) {
            return super.getVolume(volume, position, maxDistance);
        }
        return super.getVolume(volume, position, maxDistance) * PositionalAudioUtils.getDistanceVolume(maxDistance, position);
    }

    @Override
    protected int getBufferSize() {
        return Math.min(super.getBufferSize(), 3);
    }

    @Override
    protected void linearAttenuation(float maxDistance) {

    }
}
