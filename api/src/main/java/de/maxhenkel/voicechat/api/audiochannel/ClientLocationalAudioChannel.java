package de.maxhenkel.voicechat.api.audiochannel;

import de.maxhenkel.voicechat.api.Position;

public interface ClientLocationalAudioChannel extends ClientAudioChannel {

    /**
     * @return the current location of the audio channel
     */
    Position getLocation();

    /**
     * Updates the location of the audio channel.
     *
     * @param position the location of the audio channel
     */
    void setLocation(Position position);

    /**
     * @return the distance, the audio can be heard
     */
    float getDistance();

    /**
     * @param distance the distance, the audio can be heard
     */
    void setDistance(float distance);

}
