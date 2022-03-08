package de.maxhenkel.voicechat.voice.client.speaker;

import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public interface Speaker {

    void open() throws SpeakerException;

    void play(short[] data, float volume, @Nullable Vec3 position);

    void close();

}
