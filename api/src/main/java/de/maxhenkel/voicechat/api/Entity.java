package de.maxhenkel.voicechat.api;

import java.util.UUID;

public interface Entity {

    /**
     * @return the UUID of the entity
     */
    UUID getUUID();

    /**
     * @return the actual entity object
     */
    Object getEntity();

    /**
     * @return the entity name
     */
    String getName();

    /**
     * @return the current position of the entity
     */
    Position getPosition();

    /**
     * @return the level of the entity
     */
    ServerLevel getServerLevel();

    /**
     * @return the current eye position of the entity
     */
    Position getEyePosition();

    /**
     * @return whether the entity is in spectator
     */
    boolean isSpectator();

    /**
     * @return the minecraft server of the entity
     */
    MinecraftServer getServer();

    /**
     * @return whether the entity has given permission level
     */
    boolean hasPermissions(int operatorUserPermissionLevel);
}
