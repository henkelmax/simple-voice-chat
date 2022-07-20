package de.maxhenkel.voicechat.api;

import de.maxhenkel.voicechat.api.audiochannel.ClientEntityAudioChannel;
import de.maxhenkel.voicechat.api.audiochannel.ClientLocationalAudioChannel;
import de.maxhenkel.voicechat.api.audiochannel.ClientStaticAudioChannel;

import javax.annotation.Nullable;
import java.util.UUID;

public interface VoicechatClientApi extends VoicechatApi {

    /**
     * @return if the voice chat is muted
     */
    boolean isMuted();

    /**
     * @return if the voice chat is disabled
     */
    boolean isDisabled();

    /**
     * @return if the voice chat is disconnected from the server
     */
    boolean isDisconnected();

    @Nullable
    Group getGroup();

    /**
     * Creates a client side entity audio channel.
     *
     * @param uuid the UUID od the entity
     * @return the audio channel
     */
    ClientEntityAudioChannel createEntityAudioChannel(UUID uuid);

    /**
     * Creates a client side locational audio channel.
     *
     * @param uuid the ID of the channel
     * @return the audio channel
     */
    ClientLocationalAudioChannel createLocationalAudioChannel(UUID uuid, Position position);

    /**
     * Creates a client side static audio channel.
     *
     * @param uuid the ID of the channel
     * @return the audio channel
     */
    ClientStaticAudioChannel createStaticAudioChannel(UUID uuid);

    /**
     * Registers a volume category just for this client.
     * A category can be created with {@link VoicechatApi#volumeCategoryBuilder()}.
     * The category can be unregistered with {@link #unregisterClientVolumeCategory}.
     *
     * @param category the category to register
     */
    void registerClientVolumeCategory(VolumeCategory category);

    /**
     * Unregisters a category on this client.
     * This will release the texture ID for the icon if one exists.
     *
     * @param category the category to remove
     */
    default void unregisterClientVolumeCategory(VolumeCategory category) {
        unregisterClientVolumeCategory(category.getId());
    }

    /**
     * Unregisters a category on this client.
     * This will release the texture ID for the icon if one exists.
     *
     * @param categoryId the category ID to remove
     */
    void unregisterClientVolumeCategory(String categoryId);

}
