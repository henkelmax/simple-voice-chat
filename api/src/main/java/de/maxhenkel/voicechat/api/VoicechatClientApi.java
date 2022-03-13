package de.maxhenkel.voicechat.api;

import de.maxhenkel.voicechat.api.audiochannel.ClientEntityAudioChannel;
import de.maxhenkel.voicechat.api.audiochannel.ClientLocationalAudioChannel;
import de.maxhenkel.voicechat.api.audiochannel.ClientStaticAudioChannel;

import javax.annotation.Nullable;
import java.util.UUID;

public interface VoicechatClientApi extends VoicechatApi {

    boolean isMuted();

    boolean isDisabled();

    boolean isDisconnected();

    @Nullable
    Group getGroup();

    /**
     * Creates a client side entity audio channel
     *
     * @param uuid the UUID od the entity
     * @return the audio channel
     */
    ClientEntityAudioChannel createEntityAudioChannel(UUID uuid);

    /**
     * Creates a client side locational audio channel
     *
     * @param uuid the ID of the channel
     * @return the audio channel
     */
    ClientLocationalAudioChannel createLocationalAudioChannel(UUID uuid, Position position);

    /**
     * Creates a client side static audio channel
     *
     * @param uuid the ID of the channel
     * @return the audio channel
     */
    ClientStaticAudioChannel createStaticAudioChannel(UUID uuid);

}
