package de.maxhenkel.voicechat.api.audiolistener;

import de.maxhenkel.voicechat.api.ServerPlayer;
import de.maxhenkel.voicechat.api.packets.SoundPacket;

import java.util.UUID;
import java.util.function.Consumer;

public interface PlayerAudioListener extends AudioListener {

    /**
     * @return the {@link UUID} of the player that is listened to
     */
    UUID getPlayerUuid();

    public interface Builder {

        /**
         * <b>Note</b>: It is required to either set a player with this method or with {@link #setPlayer(UUID)}
         *
         * @param player the player that should be listened to
         */
        Builder setPlayer(ServerPlayer player);

        /**
         * <b>Note</b>: It is required to either set a player with this method or with {@link #setPlayer(ServerPlayer)}
         *
         * @param playerUuid the player UUID that should be listened to
         */
        Builder setPlayer(UUID playerUuid);

        /**
         * <b>Note</b>: It is required to set a listener
         *
         * @param listener the listener
         */
        Builder setPacketListener(Consumer<SoundPacket> listener);

        /**
         * @return the built listener
         * @throws IllegalStateException if the player or the listener is not set
         */
        PlayerAudioListener build();

    }

}
