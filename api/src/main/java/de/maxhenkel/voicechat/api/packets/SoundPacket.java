package de.maxhenkel.voicechat.api.packets;

import de.maxhenkel.voicechat.api.VolumeCategory;

import javax.annotation.Nullable;
import java.util.UUID;

public interface SoundPacket extends Packet, ConvertablePacket {

    /**
     * @return the sender of this packet - doesn't necessarily need to be a players UUID
     */
    UUID getSender();

    /**
     * @return the opus encoded audio data
     */
    byte[] getOpusEncodedData();

    /**
     * @return the sequence number of the packet
     */
    long getSequenceNumber();

    /**
     * @return the category ID of the sound packet
     */
    @Nullable
    String getCategory();

    /**
     * A builder to build a sound packet.
     *
     * @param <T> the builder itself
     * @param <P> the packet, the builder builds
     */
    public interface Builder<T extends Builder<T, P>, P extends SoundPacket> {

        /**
         * @param data the opus encoded audio data
         * @return the builder
         */
        T opusEncodedData(byte[] data);

        /**
         * Make sure you registered your category before using it.
         * See {@link de.maxhenkel.voicechat.api.VoicechatServerApi#registerVolumeCategory(VolumeCategory)}.
         *
         * @param category the category ID of the sound packet
         * @return the builder
         */
        T category(@Nullable String category);

        /**
         * Builds the packet.
         * <b>NOTE</b>: If you are missing required values, this will throw an {@link IllegalStateException}.
         *
         * @return the packet
         */
        P build();

    }

}
