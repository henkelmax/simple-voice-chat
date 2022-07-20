package de.maxhenkel.voicechat.api.packets;

/**
 * The receiver of this event will hear the sound non-directional.
 * This is what is basically used for group chats.
 */
public interface StaticSoundPacket extends SoundPacket {

    /**
     * A builder to build a static sound packet.
     *
     * @param <T> the builder itself
     */
    public interface Builder<T extends StaticSoundPacket.Builder<T>> extends SoundPacket.Builder<T, StaticSoundPacket> {

    }

}
