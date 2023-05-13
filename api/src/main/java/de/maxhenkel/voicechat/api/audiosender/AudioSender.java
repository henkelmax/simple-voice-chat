package de.maxhenkel.voicechat.api.audiosender;

public interface AudioSender {

    /**
     * @return a builder for a microphone packet
     */
    MicrophonePacketBuilder microphonePacketBuilder();

    /**
     * @return if a microphone packet can be sent. This will return false if the player has the mod installed or the {@link AudioSender} is not registered.
     */
    boolean canSend();

    /**
     * Flushes the audio sender.
     * This method should be called when the sending of audio is stopped.
     * You can still send audio after this method is called.
     */
    void flush();

    /**
     * This builder can be reused.
     * <br/>
     * <b>NOTE</b>: Some values are required to be set.
     */
    public interface MicrophonePacketBuilder {
        /**
         * This is required to be set!
         *
         * @param data the opus encoded audio data from the player
         * @return the builder
         */
        MicrophonePacketBuilder opusEncodedData(byte[] data);

        /**
         * @param whispering if the player should whisper
         * @return the builder
         */
        MicrophonePacketBuilder whispering(boolean whispering);

        /**
         * Sets the sequence number of the packet.
         * <br/>
         * Setting this will override the automatic sequence numbering of the {@link AudioSender}.
         *
         * @param sequenceNumber the sequence number (Must be >= 0)
         * @return the builder
         */
        MicrophonePacketBuilder sequenceNumber(long sequenceNumber);

        /**
         * Acts as if the player has sent a microphone packet.
         *
         * @return <code>true</code> if the packet was sent, <code>false</code> if the sender does have the mod installed or the {@link AudioSender} is not registered
         */
        boolean send();
    }

}
