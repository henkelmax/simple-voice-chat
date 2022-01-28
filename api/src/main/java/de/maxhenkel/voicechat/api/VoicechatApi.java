package de.maxhenkel.voicechat.api;

import de.maxhenkel.voicechat.api.audio.AudioConverter;
import de.maxhenkel.voicechat.api.opus.OpusDecoder;
import de.maxhenkel.voicechat.api.opus.OpusEncoder;
import de.maxhenkel.voicechat.api.opus.OpusEncoderMode;

import javax.annotation.Nullable;

public interface VoicechatApi {

    /**
     * Creates a new opus encoder.
     * Note that the encoder needs to be closed after you are finished using it.
     *
     * @return the opus encoder or <code>null</code> if there are no natives for this platform.
     */
    @Nullable
    OpusEncoder createEncoder();

    /**
     * Creates a new opus encoder.
     * Note that the encoder needs to be closed after you are finished using it.
     *
     * @param mode the opus encoder mode
     * @return the opus encoder or <code>null</code> if there are no natives for this platform.
     */
    @Nullable
    OpusEncoder createEncoder(OpusEncoderMode mode);

    /**
     * Creates a new opus decoder.
     * Note that the decoder needs to be closed after you are finished using it.
     *
     * @return the opus decoder or <code>null</code> if there are no natives for this platform.
     */
    @Nullable
    OpusDecoder createDecoder();

    /**
     * @return the audio converter
     */
    AudioConverter getAudioConverter();

    /**
     * Creates an entity object from an actual entity.
     *
     * @param entity the entity implementation of your mod/plugin loader
     * @return the entity object
     */
    Entity fromEntity(Object entity);

    /**
     * Creates a level object from an actual level.
     *
     * @param serverLevel the level implementation of your mod/plugin loader
     * @return the level
     */
    ServerLevel fromServerLevel(Object serverLevel);

    /**
     * Creates a player object from an actual player.
     *
     * @param serverPlayer the player implementation of your mod/plugin loader
     * @return the player
     */
    ServerPlayer fromServerPlayer(Object serverPlayer);

    /**
     * Creates a new position object.
     *
     * @param x the X position
     * @param y the Y position
     * @param z the Z position
     * @return the position with the provided coordinates
     */
    Position createPosition(double x, double y, double z);

}
