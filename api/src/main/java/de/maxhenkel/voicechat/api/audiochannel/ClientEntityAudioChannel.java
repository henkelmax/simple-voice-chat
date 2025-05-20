package de.maxhenkel.voicechat.api.audiochannel;

import java.util.UUID;

public interface ClientEntityAudioChannel extends ClientAudioChannel {

    /**
     * @return the UUID of the entity
     */
    UUID getEntityId();

    /**
     * @param whispering if the entity should whisper
     */
    void setWhispering(boolean whispering);

    /**
     * @return if the entity is whispering
     */
    boolean isWhispering();

    /**
     * @return the distance, the audio can be heard
     */
    float getDistance();

    /**
     * @param distance the distance, the audio can be heard
     */
    void setDistance(float distance);

}
