package de.maxhenkel.voicechat.api.audiochannel;

/**
 * Streams audio data from the server to clients.
 * A player instance can be obtained by calling {@link de.maxhenkel.voicechat.api.VoicechatServerApi#createAudioPlayer}.
 */
public interface AudioPlayer {

    /**
     * Starts playing/streaming the audio.
     */
    void startPlaying();

    /**
     * Stops playing/streaming the audio.
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

    /**
     * @param onStopped a runnable that's called when the player is finished playing
     */
    void setOnStopped(Runnable onStopped);

}
