package de.maxhenkel.voicechat.voice.client.speaker;

import de.maxhenkel.voicechat.voice.client.PositionalAudioUtils;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;

public class MonoJavaSpeaker extends JavaSpeakerBase {

    @Override
    protected short[] convertToStereo(short[] data, @Nullable Vec3d position) {
        return PositionalAudioUtils.convertToStereo(data);
    }
}