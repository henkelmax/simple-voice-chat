package de.maxhenkel.voicechat.api;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Groups can be created using {@link VoicechatServerApi#groupBuilder()}.
 */
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

    /**
     * @return the group type
     */
    Type getType();

    public interface Type {

        /**
         * Players in a group can hear nearby players that are not in a group
         */
        public static final Type NORMAL = new Type() {
        };

        /**
         * Players in a group can hear nearby players and nearby players can hear players in the group
         */
        public static final Type OPEN = new Type() {
        };

        /**
         * Players in a group can only hear other players in the group
         */
        public static final Type ISOLATED = new Type() {
        };
    }

    public interface Builder {

        /**
         * Sets the ID of the group.
         * If this is not set, the ID will be randomly generated.
         * <br/>
         * <b>NOTE</b>: If there is already a group with the same ID, the group will be overwritten.
         *
         * @param id the group ID or <code>null</code> if the ID should be randomly generated
         * @return the builder
         */
        Builder setId(@Nullable UUID id);

        /**
         * <b>NOTE</b>: The name might be stripped of special characters and whitespace.
         *
         * @param name the name of the group
         * @return the builder
         */
        Builder setName(String name);

        /**
         * @param password the group password
         * @return the builder
         */
        Builder setPassword(@Nullable String password);

        /**
         * @param persistent if the group should be persistent
         * @return the builder
         */
        Builder setPersistent(boolean persistent);

        /**
         * @param type the group type
         * @return the builder
         */
        Builder setType(Type type);

        /**
         * @return the built group
         * @throws IllegalStateException if the name is not set or invalid
         */
        Group build();

    }

}
