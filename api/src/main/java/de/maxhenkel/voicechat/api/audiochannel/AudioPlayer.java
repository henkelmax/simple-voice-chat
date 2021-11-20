package de.maxhenkel.voicechat.api.audiochannel;

public interface AudioPlayer {

    /**
     * Starts playing/streaming the audio
     */
    void startPlaying();

    /**
     * Stops playing/streaming the audio
     */
    void stopPlaying();

    /**
     * @return if the player has been started
     */
    boolean isStarted();

    /**
     * @return if the audio is still playing
     */
    boolean isPlaying();

    /**
     * @return if the player stopped playing
     */
    boolean isStopped();

}
