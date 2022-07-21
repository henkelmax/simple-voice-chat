package de.maxhenkel.voicechat.api;

import de.maxhenkel.voicechat.api.audio.AudioConverter;
import de.maxhenkel.voicechat.api.opus.OpusDecoder;
import de.maxhenkel.voicechat.api.opus.OpusEncoder;
import de.maxhenkel.voicechat.api.opus.OpusEncoderMode;

public interface VoicechatApi {

    /**
     * Creates a new opus encoder.
     * Note that the encoder needs to be closed after you are finished using it.
     */
    OpusEncoder createEncoder();

    /**
     * Creates a new opus encoder.
     * Note that the encoder needs to be closed after you are finished using it.
     *
     * @param mode the opus encoder mode
     * @return the opus encoder
     */
    OpusEncoder createEncoder(OpusEncoderMode mode);

    /**
     * Creates a new opus decoder.
     * Note that the decoder needs to be closed after you are finished using it.
     *
     * @return the opus decoder
     */
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

    /**
     * Don't forget to register your category with {@link VoicechatServerApi#registerVolumeCategory(VolumeCategory)} or {@link VoicechatClientApi#registerClientVolumeCategory(VolumeCategory)}
     *
     * @return a builder to build a category
     */
    VolumeCategory.Builder volumeCategoryBuilder();

    /**
     * <b>NOTE</b>: Voice chat plugins can change this.
     * This is only the default value.
     * This value might not be correct if you are not connected to a server.
     *
     * @return the default distance, audio from voice chat is heard from
     */
    double getVoiceChatDistance();

}
