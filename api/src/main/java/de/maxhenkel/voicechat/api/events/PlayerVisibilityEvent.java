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
 * This event is emitted
 * <ul>
 *     <li>Between the joining player and every other online player when a player joins</li>
 *     <li>When a vanish plugin or mod shows a player again</li>
 *     <li>When it is requested using {@link VoicechatConnection#evaluateVisibility()}</li>
 * </ul>
 * Voice chat remembers the result, so call {@link VoicechatConnection#evaluateVisibility()} when the conditions your addon uses to hide a player change.
 * It is not emitted for the player itself.
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
