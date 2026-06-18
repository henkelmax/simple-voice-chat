package de.maxhenkel.voicechat.api.config;

import de.maxhenkel.voicechat.api.VoicechatClientApi;

import java.util.UUID;

/**
 * An interface to access client side adjusted volumes.
 * <br/>
 * Can be obtained by calling {@link VoicechatClientApi#getVolumeConfig()}.
 */
public interface VolumeConfigAccessor {

    /**
     * This will always return a value, even if the player is not in the volume config.
     * By default, the volume will be <code>1.0</code>.
     * A volume of <code>0.0</code> means this player is muted.
     * Can be larger than <code>1.0</code>.
     * Also works for players that are not online.
     *
     * @param uuid the UUID of the player
     * @return the volume of the player
     * @throws NullPointerException if the UUID is null
     */
    double getVolume(UUID uuid);

    /**
     * This will always return a value, even if the category doesn't exist.
     * By default, the volume will be <code>1.0</code>.
     * A volume of <code>0.0</code> means this category is muted.
     * Can be larger than <code>1.0</code>.
     * Also works for categories that are currently not registered.
     *
     * @param category the category
     * @return the volume of the category
     * @throws NullPointerException if the category is null
     */
    double getCategoryVolume(String category);

}
