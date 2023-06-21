package de.maxhenkel.voicechat.api.config;

import de.maxhenkel.voicechat.api.VoicechatClientApi;
import de.maxhenkel.voicechat.api.VoicechatServerApi;

import javax.annotation.Nullable;

/**
 * An interface to access a config.
 * This is kept very generic, so it can be used for any config type.
 * <br/>
 * This contains a few default methods to get primitive values from the config.
 * <br/>
 * <b>NOTE</b>: You need to implement more complex values yourself (e.g. enums).
 * <br/>
 * Can be obtained by calling {@link VoicechatServerApi#getServerConfig()} or {@link VoicechatClientApi#getClientConfig()}.
 */
public interface ConfigAccessor {

    /**
     * Checks if the config has the given key.
     *
     * @param key the key
     * @return <code>true</code> if the config has the key, <code>false</code> otherwise
     */
    boolean hasKey(String key);

    /**
     * Gets the raw value of the given key.
     * <b>NOTE</b>: This gets the <i>raw</i> value from the config, so it will be a {@link String} even if the value is a number, a boolean or an enum.
     *
     * @param key the key
     * @return the value or <code>null</code> if the key doesn't exist
     */
    @Nullable
    String getValue(String key);

    /**
     * Gets the string value of the given key.
     *
     * @param key the key
     * @param def the default value
     * @return the value or the default value if the key doesn't exist
     */
    default String getString(String key, String def) {
        String value = getValue(key);
        if (value == null) {
            return def;
        }
        return value;
    }

    /**
     * Gets the boolean value of the given key.
     *
     * @param key the key
     * @param def the default value
     * @return the boolean value, <code>false</code> if the value is not <code>"true"</code> or the default value if the key doesn't exist
     */
    default boolean getBoolean(String key, boolean def) {
        String value = getValue(key);
        if (value == null) {
            return def;
        }
        return Boolean.parseBoolean(value);
    }

    /**
     * Gets the integer value of the given key.
     *
     * @param key the key
     * @param def the default value
     * @return the value or the default value if the key doesn't exist
     * @throws NumberFormatException if the value is not an integer
     */
    default int getInt(String key, int def) {
        String value = getValue(key);
        if (value == null) {
            return def;
        }
        return Integer.parseInt(value);
    }

    /**
     * Gets the double value of the given key.
     *
     * @param key the key
     * @param def the default value
     * @return the value or the default value if the key doesn't exist
     * @throws NumberFormatException if the value is not a double
     */
    default double getDouble(String key, double def) {
        String value = getValue(key);
        if (value == null) {
            return def;
        }
        return Double.parseDouble(value);
    }

}
