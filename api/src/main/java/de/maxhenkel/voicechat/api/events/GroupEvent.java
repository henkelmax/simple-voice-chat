package de.maxhenkel.voicechat.api.events;

import de.maxhenkel.voicechat.api.Group;
import de.maxhenkel.voicechat.api.VoicechatConnection;

import javax.annotation.Nullable;

public interface GroupEvent extends ServerEvent {

    /**
     * @return the group - <code>null</code> if there is no group
     */
    @Nullable
    Group getGroup();

    /**
     * @return the connection of the player
     */
    VoicechatConnection getConnection();

}
