package de.maxhenkel.voicechat.api.events;

import de.maxhenkel.voicechat.api.Group;
import de.maxhenkel.voicechat.api.VoicechatConnection;

import javax.annotation.Nullable;

public interface LeaveGroupEvent extends GroupEvent {

    /**
     * @return the group that was left or <code>null</code> if the player was not in a group
     */
    @Nullable
    Group getGroup();

    /**
     * @return the connection of the player
     */
    VoicechatConnection getConnection();

}
