package de.maxhenkel.voicechat.plugins.impl.events;

import de.maxhenkel.voicechat.api.events.MergeClientSoundEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class MergeClientSoundEventImpl extends ClientEventImpl implements MergeClientSoundEvent {

    @Nullable
    private List<short[]> audioToMerge;

    public MergeClientSoundEventImpl() {

    }

    @Override
    public boolean isCancellable() {
        return false;
    }

    @Override
    public void mergeAudio(short[] audio) {
        if (audioToMerge == null) {
            audioToMerge = new ArrayList<>();
        }
        audioToMerge.add(audio);
    }

    @Nullable
    public List<short[]> getAudioToMerge() {
        return audioToMerge;
    }
}
