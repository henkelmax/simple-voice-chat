package de.maxhenkel.voicechat.api.audiosender;

public interface AudioSender {

    /**
     * @param whispering if the player should whisper
     * @return the audio sender itself
     */
    AudioSender whispering(boolean whispering);

    /**
     * @return if the player is whispering
     */
    boolean isWhispering();

    /**
     * Sets the sequence number of the packet.
     * <br/>
     * Setting this will override the automatic sequence numbering.
     * <br/>
     * If you set this value, consequent calls to {@link AudioSender#send(byte[])} will increase this number by 1.
     * If you don't intend this, you need to set the sequence number every time before calling {@link AudioSender#send(byte[])}.
     * <br/>
     * Calling {@link AudioSender#reset()} will also reset the sequence number to start with <code>0</code> again.
     *
     * @param sequenceNumber the sequence number (Must be >= 0)
     * @return the audio sender itself
     */
    AudioSender sequenceNumber(long sequenceNumber);

    /**
     * @return if a microphone packet can be sent. This will return false if the player has the mod installed or the {@link AudioSender} is not registered.
     */
    boolean canSend();

    /**
     * Acts as if the player has sent a microphone packet.
     *
     * @param opusEncodedAudioData the opus encoded audio data
     * @return <code>true</code> if the packet was sent, <code>false</code> if the sender does have the mod installed or the {@link AudioSender} is not registered
     */
    boolean send(byte[] opusEncodedAudioData);

    /**
     * Resets the sequence number and indicates to clients that the current audio stream is paused/stopped.
     * <br/>
     * This method should be called when the sending of audio is paused or stopped.
     * You can still send audio after this method is called.
     */
    boolean reset();

}
