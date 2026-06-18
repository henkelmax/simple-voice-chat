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
     * Gets the volume of a player by its UUID.
     * <br/>
     * <br/>
     * This will always return a value, even if the player is not in the volume config.
     * <br/>
     * By default, the volume will be <code>1.0</code>.
     * A volume of <code>0.0</code> means this player is muted.
     * Can be larger than <code>1.0</code>.
     * Also works for players that are not online.
     * <br/>
     * <br/>
     * If you want the <code>Other</code> volume (All volumes that are not covered by categories or players), use the UUID <code>00000000-0000-0000-0000-000000000000</code>.
     *
     * @param uuid the UUID of the player
     * @return the volume of the player
     * @throws NullPointerException if the UUID is null
     */
    double getVolume(UUID uuid);

    /**
     * Gets the volume of a category by its UUID.
     * <br/>
     * <br/>
     * This will always return a value, even if the category doesn't exist.
     * <br/>
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
