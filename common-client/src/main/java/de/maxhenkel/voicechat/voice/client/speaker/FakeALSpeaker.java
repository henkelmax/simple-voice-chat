package de.maxhenkel.voicechat.voice.client.speaker;

import de.maxhenkel.voicechat.voice.client.PositionalAudioUtils;
import de.maxhenkel.voicechat.voice.client.SoundManager;
import net.minecraft.util.math.vector.Vector3d;
import org.lwjgl.openal.AL11;

import javax.annotation.Nullable;
import java.util.UUID;

public class FakeALSpeaker extends ALSpeakerBase {

    public FakeALSpeaker(SoundManager soundManager, int sampleRate, int bufferSize, @Nullable UUID audioChannelId) {
        super(soundManager, sampleRate, bufferSize, audioChannelId);
        this.bufferSize *= 2;
    }

    @Override
    protected void openSync() {
        super.openSync();
        AL11.alDistanceModel(AL11.AL_NONE);
        SoundManager.checkAlError();
    }

    @Override
    protected short[] convert(short[] data, @Nullable Vector3d position) {
        return PositionalAudioUtils.convertToStereo(data, position);
    }

    @Override
    protected int getFormat() {
        return AL11.AL_FORMAT_STEREO16;
    }

    @Override
    protected void setPositionSync(@Nullable Vector3d soundPos, float maxDistance) {

    }

    @Override
    protected float getVolume(float volume, @Nullable Vector3d position, float maxDistance) {
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
