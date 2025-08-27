package de.maxhenkel.voicechat.api.audiochannel;

import de.maxhenkel.voicechat.api.VoicechatConnection;

public interface StaticAudioChannel extends AudioChannel {

    /**
     * Sets whether this audio channel should be received by targets in isolated groups.
     *
     * @param bypassGroupIsolation if group isolation should be bypassed
     */
    void setBypassGroupIsolation(boolean bypassGroupIsolation);

    /**
     * @return if this audio channel is received by targets in isolated groups
     */
    boolean bypassesGroupIsolation();

    /**
     * Adds a target to the audio channel.
     *
     * @param target the target to add
     */
    void addTarget(VoicechatConnection target);

    /**
     * Removes a target from the audio channel.
     *
     * @param target the target to remove
     */
    void removeTarget(VoicechatConnection target);

    /**
     * Removes all targets from the audio channel.
     */
    void clearTargets();

}
