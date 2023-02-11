package de.maxhenkel.voicechat.api.events;

import de.maxhenkel.voicechat.api.Group;
import de.maxhenkel.voicechat.api.VoicechatConnection;

import javax.annotation.Nullable;

/**
 * This event is only cancellable if the group is persistent
 */
public interface RemoveGroupEvent extends GroupEvent {

    /**
     * @return the group that was removed
     */
    Group getGroup();

    /**
     * @return <code>null</code>
     */
    @Nullable
    @Deprecated
    VoicechatConnection getConnection();

}
