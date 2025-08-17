package de.maxhenkel.voicechat.api.events;

import de.maxhenkel.voicechat.api.VoicechatClientApi;

import java.util.UUID;

/**
 * This event is emitted when an icon is getting rendered.
 * <br/>
 * This event should not be used to do any actual rendering.
 * Just use this event to cancel the rendering of the icon.
 * <br/>
 * <b>Warning: </b> Don't do heavy operations in this event, as it will block the render thread!
 * <br/>
 * <br/>
 * <b>Note: </b> Don't keep references to this event. It will be reused for every icon render to avoid allocations.
 * <br/>
 * <br/>
 * Use {@link VoicechatClientApi#isTalking(UUID)}, {@link VoicechatClientApi#isWhispering(UUID)},
 * {@link VoicechatClientApi#isDisabled(UUID)} and {@link VoicechatClientApi#isDisconnected(UUID)}
 * to get the status of the player and render it using your mod loaders render event system.
 */
public interface NameTagIconRenderEvent extends ClientEvent {

    /**
     * @return the UUID of the entity the icon is being rendered for
     */
    UUID getEntityId();

}
