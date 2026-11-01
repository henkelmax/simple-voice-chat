package de.maxhenkel.voicechat.api.events;

import de.maxhenkel.voicechat.api.ServerPlayer;
import de.maxhenkel.voicechat.api.VoicechatConnection;

/**
 * This event is emitted when voice chat evaluates if a player is visible to another player.
 * <br>
 * Hidden players don't show up in the voice chat state of the observer.
 * This includes icons, the group HUD and the volume list.
 * <br>
 * <b>NOTE</b>:
 * <br>
 * This event is emitted whenever the state of a player is sent to another player.
 * It is not emitted for the player itself.
 * To re-evaluate the visibility of a player, use {@link VoicechatConnection#evaluateVisibility()}.
 * <br>
 * Note that hiding a player from another player still makes the hidden player send audio to the observer, if you don't intend that, you need to handle this yourself.
 * <br>
 * This can be emitted on any thread, including the main server thread, and needs to return very quickly, don't run expensive code here!
 */
public interface PlayerVisibilityEvent extends ServerEvent {

    /**
     * @return the player that would see the other player
     */
    ServerPlayer getObserver();

    /**
     * @return the player that is checked for visibility
     */
    ServerPlayer getPlayer();

    /**
     * @return if the player is hidden from the observer - either because of a vanish plugin or mod or because {@link #hide()} was called by this addon or an addon that received this event before this one
     */
    boolean isHidden();

    /**
     * Hides the player from the observer.
     */
    void hide();

}
