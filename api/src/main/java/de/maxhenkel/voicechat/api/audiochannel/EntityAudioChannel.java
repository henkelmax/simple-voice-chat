package de.maxhenkel.voicechat.api.audiochannel;

import de.maxhenkel.voicechat.api.Entity;

public interface EntityAudioChannel extends AudioChannel {

    /**
     * @return if the entity is whispering
     */
    boolean isWhispering();

    /**
     * @param whispering if the entity should whisper
     */
    void setWhispering(boolean whispering);

    /**
     * Sets a new entity where this channel is attached to.
     *
     * @param entity the entity to attach the channel to
     */
    void updateEntity(Entity entity);

    /**
     * @return the entity where the channel is attached to
     */
    Entity getEntity();

    /**
     * @return the distance, the audio can be heard
     */
    float getDistance();

    /**
     * @param distance the distance, the audio can be heard
     */
    void setDistance(float distance);

}
