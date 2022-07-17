package de.maxhenkel.voicechat.api.audiochannel;

public interface ClientEntityAudioChannel extends ClientAudioChannel {

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
