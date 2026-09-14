package de.maxhenkel.voicechat.api.internal.events;

import de.maxhenkel.voicechat.api.events.ClientEvent;

/**
 * <b>For internal use only! Do not use this!</b>
 */
public interface ForceShowIconsEvent extends ClientEvent {

    void forceShow();

}
