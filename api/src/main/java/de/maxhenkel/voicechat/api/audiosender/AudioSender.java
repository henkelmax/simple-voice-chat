package de.maxhenkel.voicechat.api.audiosender;

public interface AudioSender {

    /**
     * This can be reused.
     * <br/>
     * Invoking this method multiple times, will always return the same instance.
     *
     * @return a microphone packet sender
     */
    MicrophonePacketSender microphonePacketSender();

    /**
     * @return if a microphone packet can be sent. This will return false if the player has the mod installed or the {@link AudioSender} is not registered.
     */
    boolean canSend();

    /**
     * This sender can be reused.
     * <br/>
     * <b>NOTE</b>: Some values are required to be set.
     */
    public interface MicrophonePacketSender {
        /**
         * This is required to be set!
         *
         * @param data the opus encoded audio data from the player
         * @return the builder
         */
        MicrophonePacketSender opusEncodedData(byte[] data);

        /**
         * @param whispering if the player should whisper
         * @return the builder
         */
        MicrophonePacketSender whispering(boolean whispering);

        /**
         * Sets the sequence number of the packet.
         * <br/>
         * Setting this will override the automatic sequence numbering of the {@link AudioSender}.
         *
         * @param sequenceNumber the sequence number (Must be >= 0)
         * @return the builder
         */
        MicrophonePacketSender sequenceNumber(long sequenceNumber);

        /**
         * Acts as if the player has sent a microphone packet.
         * <br/>
         * <b>NOTE</b>: Calling this method will reset all values, meaning you need to set all required values again.
         *
         * @return <code>true</code> if the packet was sent, <code>false</code> if the sender does have the mod installed or the {@link AudioSender} is not registered
         */
        boolean send();

        /**
         * Resets the sequence number and indicates to clients that the current audio stream is paused/stopped.
         * <br/>
         * This method should be called when the sending of audio is paused or stopped.
         * You can still send audio after this method is called.
         * <br/>
         * <b>NOTE</b>: Calling this method will reset all values, meaning you need to set all required values again.
         */
        boolean reset();
    }

}
