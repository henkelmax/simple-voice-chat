package de.maxhenkel.voicechat.api.audiochannel;

import de.maxhenkel.voicechat.api.Entity;

public interface EntityAudioChannel extends AudioChannel {

    /**
     * @param whispering if the entity should whisper
     */
    void setWhispering(boolean whispering);

    /**
     * @return if the entity is whispering
     */
    boolean isWhispering();

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

}
