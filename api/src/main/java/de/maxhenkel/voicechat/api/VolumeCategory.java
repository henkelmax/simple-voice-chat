package de.maxhenkel.voicechat.api;

import javax.annotation.Nullable;

/**
 * A custom volume category for the "adjust volumes screen".
 * Create it with {@link VoicechatApi#volumeCategoryBuilder()}.
 * Register it with {@link VoicechatServerApi#registerVolumeCategory(VolumeCategory)} or {@link VoicechatClientApi#registerClientVolumeCategory(VolumeCategory)}.
 * Unregister it with {@link VoicechatServerApi#unregisterVolumeCategory} or {@link VoicechatClientApi#unregisterClientVolumeCategory}.
 */
public interface VolumeCategory {

    /**
     * @return the unique ID of the category
     */
    String getId();

    /**
     * @return the full name of the category, that is displayed for the end user
     */
    String getName();

    /**
     * @return the hover tooltip that is shown for this category
     */
    @Nullable
    String getDescription();

    /**
     * @return the icon that is shown for this category
     */
    @Nullable
    int[][] getIcon();

    public interface Builder {

        /**
         * This ID has to be between 1 and 16 characters and can only contain lowercase <code>a-z</code> and <code>_</code>.
         *
         * @param id the unique ID of the category
         * @return the builder
         */
        Builder setId(String id);

        /**
         * @param name the full name of the category, that is displayed for the end user
         * @return the builder
         */
        Builder setName(String name);

        /**
         * @param description the hover tooltip that is shown for this category
         * @return the builder
         */
        Builder setDescription(@Nullable String description);

        /**
         * The array has to be 16x16.
         * Each integer represents a pixel in the RGBA format.
         *
         * @param icon the icon that is shown for this category
         * @return the builder
         */
        Builder setIcon(@Nullable int[][] icon);

        /**
         * @return the built category
         */
        VolumeCategory build();

    }

}
