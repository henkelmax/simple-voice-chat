package de.maxhenkel.voicechat.voice.client.speaker;

import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public interface Speaker {

    void open() throws SpeakerException;

    void play(short[] data, float volume, @Nullable Vec3 position, float maxDistance);

    default void play(short[] data, float volume, @Nullable Vec3 position) {
        play(data, volume, position, 0F);
    }

    void close();

}
