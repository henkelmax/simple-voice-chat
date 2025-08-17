package de.maxhenkel.voicechat.api;

import de.maxhenkel.voicechat.api.audiochannel.ClientEntityAudioChannel;
import de.maxhenkel.voicechat.api.audiochannel.ClientLocationalAudioChannel;
import de.maxhenkel.voicechat.api.audiochannel.ClientStaticAudioChannel;
import de.maxhenkel.voicechat.api.config.ConfigAccessor;

import javax.annotation.Nullable;
import java.util.UUID;

public interface VoicechatClientApi extends VoicechatApi {

    /**
     * @return if the local voice chat is muted
     */
    boolean isMuted();

    /**
     * Does the same as {@link #isDisabled(UUID)} with <code>null</code> as the player UUID.
     *
     * @return if the local voice chat is disabled
     */
    default boolean isDisabled() {
        return isDisabled(null);
    }

    /**
     * @param playerId the UUID of the player or <code>null</code> for the local player
     * @return if the player with the provided UUID has the voice chat disabled
     */
    boolean isDisabled(@Nullable UUID playerId);

    /**
     * Does the same as {@link #isDisconnected(UUID)} with <code>null</code> as the player UUID.
     *
     * @return if the voice chat is disconnected from the server
     */
    default boolean isDisconnected() {
        return isDisconnected(null);
    }

    /**
     * @param playerId the UUID of the player or <code>null</code> for the local player
     * @return if the player with the provided UUID is disconnected from voice chat
     */
    boolean isDisconnected(@Nullable UUID playerId);

    /**
     * Does the same as {@link #isTalking(UUID)} with <code>null</code> as the player UUID.
     *
     * @return if the local player is talking
     */
    default boolean isTalking() {
        return isTalking(null);
    }

    /**
     * @param playerId the UUID of the player or <code>null</code> for the local player
     * @return if the player with the provided UUID is currently talking
     */
    boolean isTalking(@Nullable UUID playerId);

    /**
     * Does the same as {@link #isWhispering(UUID)} with <code>null</code> as the player UUID.
     *
     * @return if the local player is whispering
     */
    default boolean isWhispering() {
        return isWhispering(null);
    }

    /**
     * @param playerId the UUID of the player or <code>null</code> for the local player
     * @return if the player with the provided UUID is currently whispering
     */
    boolean isWhispering(@Nullable UUID playerId);

    /**
     * This method returns if the push to talk key is pressed, even when using voice activation.
     *
     * @return if the push to talk key is pressed
     */
    boolean isPushToTalkKeyPressed();

    /**
     * @return if the whisper key is pressed
     */
    boolean isWhisperKeyPressed();

    @Nullable
    Group getGroup();

    /**
     * Creates a client side entity audio channel.
     *
     * @param uuid the UUID of the entity
     * @return the audio channel
     * @deprecated use {@link #createEntityAudioChannel(UUID, Entity)}
     */
    @Deprecated
    ClientEntityAudioChannel createEntityAudioChannel(UUID uuid);

    /**
     * Creates a client side entity audio channel.
     *
     * @param uuid   the UUID of the entity
     * @param entity the entity
     * @return the audio channel
     */
    ClientEntityAudioChannel createEntityAudioChannel(UUID uuid, Entity entity);

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

    /**
     * @return a read-only config accessor for the mods client config
     */
    ConfigAccessor getClientConfig();

}
