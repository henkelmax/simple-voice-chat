package de.maxhenkel.voicechat.api;

import de.maxhenkel.voicechat.api.audiochannel.*;
import de.maxhenkel.voicechat.api.audiolistener.AudioListener;
import de.maxhenkel.voicechat.api.audiolistener.PlayerAudioListener;
import de.maxhenkel.voicechat.api.audiosender.AudioSender;
import de.maxhenkel.voicechat.api.config.ConfigAccessor;
import de.maxhenkel.voicechat.api.opus.OpusEncoder;
import de.maxhenkel.voicechat.api.packets.EntitySoundPacket;
import de.maxhenkel.voicechat.api.packets.LocationalSoundPacket;
import de.maxhenkel.voicechat.api.packets.StaticSoundPacket;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface VoicechatServerApi extends VoicechatApi {

    /**
     * Sends the sound packet to the provided connection.
     *
     * @param connection the connection to send the packet to
     * @param packet     the packet to send
     */
    void sendEntitySoundPacketTo(VoicechatConnection connection, EntitySoundPacket packet);

    /**
     * Sends the sound packet to the provided connection.
     *
     * @param connection the connection to send the packet to
     * @param packet     the packet to send
     */
    void sendLocationalSoundPacketTo(VoicechatConnection connection, LocationalSoundPacket packet);

    /**
     * Sends the sound packet to the provided connection.
     *
     * @param connection the connection to send the packet to
     * @param packet     the packet to send
     */
    void sendStaticSoundPacketTo(VoicechatConnection connection, StaticSoundPacket packet);

    /**
     * Creates a sound channel for the specified entity.
     *
     * @param channelId the ID of the channel - Has to be unique
     * @param entity    the entity to attach the channel to
     * @return the channel
     */
    @Nullable
    EntityAudioChannel createEntityAudioChannel(UUID channelId, Entity entity);

    /**
     * Creates a sound channel at the provided location.
     *
     * @param channelId       the ID of the channel - Has to be unique
     * @param level           the world
     * @param initialPosition the position where the sound should be played
     * @return the channel
     */
    @Nullable
    LocationalAudioChannel createLocationalAudioChannel(UUID channelId, ServerLevel level, Position initialPosition);

    /**
     * Creates a static audio channel.
     *
     * @param channelId  the ID of the channel - Has to be unique
     * @param level      the level
     * @param connection the connection that should hear the audio
     * @return the channel
     */
    @Nullable
    StaticAudioChannel createStaticAudioChannel(UUID channelId, ServerLevel level, VoicechatConnection connection);

    /**
     * Creates a new audio player.
     * <br/>
     * <br/>
     * <b>NOTE</b>: Never use more than one audio player for every audio channel.
     *
     * @param audioChannel  the channel where the audio player should send the audio to
     * @param encoder       the optus encoder used to encode the audio data
     * @param audioSupplier this gets called whenever a new audio frame needs to be sent. The size of the array always needs to be 960. To end the playback, return <code>null</code>
     * @return the audio player
     */
    AudioPlayer createAudioPlayer(AudioChannel audioChannel, OpusEncoder encoder, Supplier<short[]> audioSupplier);

    /**
     * Creates a new audio player.
     * <br/>
     * <br/>
     * <b>NOTE</b>: Never use more than one audio player for every audio channel.
     *
     * @param audioChannel the channel where the audio player should send the audio to
     * @param encoder      the optus encoder used to encode the audio data
     * @param audio        the audio data
     * @return the audio player
     */
    AudioPlayer createAudioPlayer(AudioChannel audioChannel, OpusEncoder encoder, short[] audio);

    /**
     * Creates a new audio sender.
     * <br/>
     * This can be used to simulate a player sending microphone packets.
     * This needs to be registered using {@link #registerAudioSender(AudioSender)} and unregistered using {@link #unregisterAudioSender(AudioSender)}.
     *
     * @param connection the connection of the player
     * @return the audio sender
     */
    AudioSender createAudioSender(VoicechatConnection connection);

    /**
     * <b>NOTE</b>: Only one instance of this can exist per player. This will return <code>false</code> if an audio sender for this player already exists.
     * <br/>
     * <b>NOTE</b>: The audio sender will only work for players that are connected to the server and don't have the mod installed. Otherwise, it will return <code>false</code>.
     *
     * @param sender the sender to register
     * @return if the sender was registered
     */
    boolean registerAudioSender(AudioSender sender);

    /**
     * Unregisters an audio sender.
     *
     * @param sender the sender to unregister
     * @return <code>false</code> if the audio sender was not registered.
     */
    boolean unregisterAudioSender(AudioSender sender);

    /**
     * @return a {@link PlayerAudioListener} builder
     */
    PlayerAudioListener.Builder playerAudioListenerBuilder();

    /**
     * Registers a new {@link AudioListener}.
     * <br/>
     * Returns false if the listener is already registered.
     *
     * @param listener the listener to register
     * @return if the listener was registered
     */
    boolean registerAudioListener(AudioListener listener);

    /**
     * Unregisters an {@link AudioListener}.
     * <br/>
     * Returns false if the listener is already unregistered.
     *
     * @param listener the listener to unregister
     * @return if the listener was unregistered
     */
    boolean unregisterAudioListener(AudioListener listener);

    /**
     * Unregisters an {@link AudioListener}.
     * <br/>
     * Returns false if the listener is already unregistered.
     *
     * @param listenerId the {@link AudioListener#getListenerId()} of the listener to unregister
     * @return if the listener was unregistered
     */
    boolean unregisterAudioListener(UUID listenerId);

    /**
     * Gets the connection of the player with this UUID.
     *
     * @param playerUuid the players UUID
     * @return the connection or <code>null</code> if the player is not connected
     */
    @Nullable
    VoicechatConnection getConnectionOf(UUID playerUuid);

    /**
     * Gets the connection of the player.
     *
     * @param player the player
     * @return the connection or <code>null</code> if the player is not connected
     */
    @Nullable
    default VoicechatConnection getConnectionOf(ServerPlayer player) {
        return getConnectionOf(player.getUuid());
    }

    /**
     * Creates a new group.
     *
     * @param name     the name of the group
     * @param password the password of the group - <code>null</code> for no password
     * @return the group
     * @deprecated use {@link #groupBuilder()} instead
     */
    @Deprecated
    Group createGroup(String name, @Nullable String password);

    /**
     * Creates a new group.
     *
     * @param name       the name of the group
     * @param password   the password of the group - <code>null</code> for no password
     * @param persistent if the group should be persistent
     * @return the group
     * @deprecated use {@link #groupBuilder()} instead
     */
    @Deprecated
    Group createGroup(String name, @Nullable String password, boolean persistent);

    /**
     * @return a new group builder
     */
    Group.Builder groupBuilder();

    /**
     * Removes a persistent group.
     *
     * <b>NOTE</b>: You can't remove a group that is not persistent or has players in it.
     *
     * @param groupId the ID of the group
     * @return if the group was removed
     */
    boolean removeGroup(UUID groupId);

    /**
     * Gets a group by its ID.
     *
     * @param groupId the ID of the group
     * @return the group or <code>null</code> if the group doesn't exist
     */
    @Nullable
    Group getGroup(UUID groupId);

    /**
     * @return all groups
     */
    Collection<Group> getGroups();

    /**
     * Gets the secret for the user with this <code>userId</code>.
     * Calling this function with a players UUID, that is already connected, this will return the secret of that player.
     * Calling it with a new random UUID this will return a new secret.
     * Use this with caution - Keep this secret confidential, as this allows connecting to the voice chat as this person.
     * Note that if this <code>userId</code> is a players UUID and this player disconnects, the secret will get removed and thus is no longer valid to connect with.
     *
     * @param userId the user ID
     * @return the secret or <code>null</code> if no server is running
     */
    @Nullable
    @Deprecated
    UUID getSecret(UUID userId);

    /**
     * A convenience method to get all players in the range of a specific location.
     *
     * @param level  the world
     * @param pos    the location
     * @param range  the range
     * @param filter the filter to exclude specific players
     * @return all players in the provided location
     */
    Collection<ServerPlayer> getPlayersInRange(ServerLevel level, Position pos, double range, Predicate<ServerPlayer> filter);

    /**
     * The distance at which audio packets are broadcast.
     * <b>NOTE</b>: This is not the distance it can be heard.
     *
     * @return the maximum distance, voice chat audio packets can be received
     */
    double getBroadcastRange();

    /**
     * A convenience method to get all players in the range of a specific location.
     *
     * @param level the world
     * @param pos   the location
     * @param range the range
     * @return all players in the provided location
     */
    default Collection<ServerPlayer> getPlayersInRange(ServerLevel level, Position pos, double range) {
        return getPlayersInRange(level, pos, range, player -> true);
    }

    /**
     * Registers a volume category.
     * This will get synchronized to all connected clients and all future clients that will connect.
     * A category can be created with {@link VoicechatApi#volumeCategoryBuilder()}.
     * The category can be unregistered with {@link #unregisterVolumeCategory}.
     *
     * @param category the category to register
     */
    void registerVolumeCategory(VolumeCategory category);

    /**
     * Unregisters a category for all connected players.
     * This will release the texture ID for the icon if one exists on all clients.
     *
     * @param category the category to remove
     */
    default void unregisterVolumeCategory(VolumeCategory category) {
        unregisterVolumeCategory(category.getId());
    }

    /**
     * Unregisters a category for all connected players.
     * This will release the texture ID for the icon if one exists on all clients.
     *
     * @param categoryId the category ID to remove
     */
    void unregisterVolumeCategory(String categoryId);

    /**
     * @return all registered volume categories
     */
    Collection<VolumeCategory> getVolumeCategories();

    /**
     * @return a read-only config accessor for the mods server config
     */
    ConfigAccessor getServerConfig();

}
