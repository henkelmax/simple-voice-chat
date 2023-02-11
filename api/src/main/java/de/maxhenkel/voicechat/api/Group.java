package de.maxhenkel.voicechat.api;

import java.util.UUID;

public interface Group {

    /**
     * @return the visual name of the group
     */
    String getName();

    /**
     * @return if the group has a password
     */
    boolean hasPassword();

    /**
     * @return the ID of the group
     */
    UUID getId();

    /**
     * @return if the group is persistent
     */
    boolean isPersistent();

}
