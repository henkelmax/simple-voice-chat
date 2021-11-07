package de.maxhenkel.voicechat.api.events;

public interface Event {

    /**
     * @return if this event can be cancelled
     */
    boolean isCancellable();

    /**
     * Cancels this event.
     * Does nothing if the event isn't cancellable.
     *
     * @return if the event was actually cancelled
     */
    boolean cancel();

    /**
     * @return if the event was cancelled
     */
    boolean isCancelled();

}
