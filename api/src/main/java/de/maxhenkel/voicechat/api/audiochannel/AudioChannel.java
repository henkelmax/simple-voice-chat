package de.maxhenkel.voicechat.api.audiochannel;

import de.maxhenkel.voicechat.api.ServerPlayer;
import de.maxhenkel.voicechat.api.VolumeCategory;
import de.maxhenkel.voicechat.api.packets.MicrophonePacket;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.function.Predicate;

public interface AudioChannel {

    /**
     * Sends the audio data to this audio channel.
     *
     * @param opusData Opus encoded audio data
     */
    void send(byte[] opusData);

    /**
     * Forwards the provided microphone packet to this audio channel.
     *
     * @param packet the microphone packet containing the audio data
     */
    void send(MicrophonePacket packet);

    /**
     * Applies a filter to the audio channel.
     *
     * @param filter only sends the data to players where the filter applies
     */
    void setFilter(Predicate<ServerPlayer> filter);

    /**
     * Call this if you are finished sending data.
     * You can still use this audio channel after calling this function,
     * just don't call it after every packet, only after you are finished sending data.
     */
    void flush();

    /**
     * @return if the audio channel is closed
     */
    boolean isClosed();

    /**
     * @return the ID of the audio channel
     */
    UUID getId();

    /**
     * @return the category ID of the audio channel
     */
    @Nullable
    String getCategory();

    /**
     * Make sure you registered your category before using it.
     * See {@link de.maxhenkel.voicechat.api.VoicechatServerApi#registerVolumeCategory(VolumeCategory)}.
     *
     * @param category the category ID of the audio channel
     */
    void setCategory(@Nullable String category);

}
