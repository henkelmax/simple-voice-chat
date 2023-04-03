package de.maxhenkel.voicechat.api.audiolistener;

import java.util.UUID;

public interface AudioListener {

    /**
     * @return the ID of the listener
     */
    UUID getListenerId();

}
