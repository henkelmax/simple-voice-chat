package de.maxhenkel.voicechat.api;

import java.util.UUID;

public interface Entity {

    /**
     * @return the UUID of the entity
     */
    UUID getUuid();

    /**
     * @return the actual entity object
     */
    Object getEntity();

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

    boolean isSpectator();

    MinecraftServer getServer();

    boolean hasPermissions(int operatorUserPermissionLevel);
}
