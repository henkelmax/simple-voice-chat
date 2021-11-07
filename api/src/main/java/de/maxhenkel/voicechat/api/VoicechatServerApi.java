package de.maxhenkel.voicechat.api;

import de.maxhenkel.voicechat.api.audiochannel.EntityAudioChannel;
import de.maxhenkel.voicechat.api.audiochannel.LocationalAudioChannel;
import de.maxhenkel.voicechat.api.audiochannel.StaticAudioChannel;
import de.maxhenkel.voicechat.api.packets.EntitySoundPacket;
import de.maxhenkel.voicechat.api.packets.LocationalSoundPacket;
import de.maxhenkel.voicechat.api.packets.StaticSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;
import java.util.function.Predicate;

public interface VoicechatServerApi extends VoicechatApi {

    /**
     * Sends the sound packet to the provided connection
     *
     * @param connection the connection to send the packet to
     * @param packet     the packet to send
     */
    void sendEntitySoundPacketTo(VoicechatConnection connection, EntitySoundPacket packet);

    /**
     * Sends the sound packet to the provided connection
     *
     * @param connection the connection to send the packet to
     * @param packet     the packet to send
     */
    void sendLocationalSoundPacketTo(VoicechatConnection connection, LocationalSoundPacket packet);

    /**
     * Sends the sound packet to the provided connection
     *
     * @param connection the connection to send the packet to
     * @param packet     the packet to send
     */
    void sendStaticSoundPacketTo(VoicechatConnection connection, StaticSoundPacket packet);

    /**
     * Creates a sound channel for the specified entity
     *
     * @param channelId the ID of the channel - Has to be unique
     * @param entity    the entity to attach the channel to
     * @return the channel
     */
    @Nullable
    EntityAudioChannel createEntityAudioChannel(UUID channelId, Entity entity);

    /**
     * Creates a sound channel at the provided location
     *
     * @param channelId       the ID of the channel - Has to be unique
     * @param level           the world
     * @param initialPosition the position where the sound should be played
     * @return the channel
     */
    @Nullable
    LocationalAudioChannel createLocationalAudioChannel(UUID channelId, ServerLevel level, Vec3 initialPosition);

    /**
     * Creates a static audio channel
     *
     * @param channelId  the ID of the channel - Has to be unique
     * @param level      the level
     * @param connection the connection that should hear the audio
     * @return the channel
     */
    @Nullable
    StaticAudioChannel createStaticAudioChannel(UUID channelId, ServerLevel level, VoicechatConnection connection);

    /**
     * Gets the connection of the player with this UUID
     *
     * @param playerUuid the players UUID
     * @return the connection or <code>null</code> if the player is not connected
     */
    @Nullable
    VoicechatConnection getConnectionOf(UUID playerUuid);

    /**
     * Gets the connection of the player
     *
     * @param player the player
     * @return the connection or <code>null</code> if the player is not connected
     */
    @Nullable
    default VoicechatConnection getConnectionOf(ServerPlayer player) {
        return getConnectionOf(player.getUUID());
    }

    /**
     * Creates a new group
     *
     * @param name     the name of the group
     * @param password the password of the group - <code>null</code> for no password
     * @return the group
     */
    Group createGroup(String name, @Nullable String password);

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
     * A convenience method to get all players in the range of a specific location
     *
     * @param level  the world
     * @param pos    the location
     * @param range  the range
     * @param filter the filter to exclude specific players
     * @return all players in the provided location
     */
    Collection<ServerPlayer> getPlayersInRange(ServerLevel level, Vec3 pos, double range, Predicate<ServerPlayer> filter);

    /**
     * A convenience method to get all players in the range of a specific location
     *
     * @param level the world
     * @param pos   the location
     * @param range the range
     * @return all players in the provided location
     */
    default Collection<ServerPlayer> getPlayersInRange(ServerLevel level, Vec3 pos, double range) {
        return getPlayersInRange(level, pos, range, player -> true);
    }

}
