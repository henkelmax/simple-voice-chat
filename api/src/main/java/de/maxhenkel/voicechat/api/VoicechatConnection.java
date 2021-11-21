package de.maxhenkel.voicechat.api;

import javax.annotation.Nullable;

public interface VoicechatConnection {

    /**
     * @return the group of the player - <code>null</code> if the player is not in a group
     */
    @Nullable
    Group getGroup();

    /**
     * @return if the player is in a group
     */
    boolean isInGroup();

    /**
     * Joins this player to the provided group.
     *
     * @param group the group to join or <code>null</code> to leave the current group
     */
    void setGroup(@Nullable Group group);

    /**
     * @return If the player muted the sound
     */
    boolean isDisabled();

    /**
     * @return the player
     */
    ServerPlayer getPlayer();

}
