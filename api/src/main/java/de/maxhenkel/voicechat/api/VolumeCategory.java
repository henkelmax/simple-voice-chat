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
     * Handle creating a new VolumeCategory from an incoming packet
     *
     * @param buf the data coming in
     * @return an instance of your mod/plugin loaders implementation of VolumeCategory
     */
    VolumeCategory fromBytes(Object buf);

    /**
     * @return the unique ID of the category
     */
    String getId();

    /**
     * @return the full name of the category, that is displayed for the end user
     */
    String getName();

    /**
     * @return the translation key for the full name of the category
     */
    @Nullable
    String getNameTranslationKey();

    /**
     * @return the hover tooltip that is shown for this category
     */
    @Nullable
    String getDescription();

    /**
     * @return the translation key for the hover tooltip of the category
     */
    @Nullable
    String getDescriptionTranslationKey();

    /**
     * @return the icon that is shown for this category
     */
    @Nullable
    int[][] getIcon();

    /**
     * Fill a buffer with your VolumeCategory data to be sent over networking
     *
     * @param buf the packet to fill
     */
    void toBytes(Object buf);

    public interface Builder {

        /**
         * This ID has to be between 1 and 16 characters and can only contain lowercase <code>a-z</code> and <code>_</code>.
         *
         * @param id the unique ID of the category
         * @return the builder
         */
        Builder setId(String id);

        /**
         * @param name the full name of the category, that is displayed for the end user if no translation is present
         * @return the builder
         */
        Builder setName(String name);

        /**
         * @param translationKey the translation key for the full name of the category
         * @return the builder
         */
        Builder setNameTranslationKey(@Nullable String translationKey);

        /**
         * @param description the hover tooltip that is shown for this category if no translation is present
         * @return the builder
         */
        Builder setDescription(@Nullable String description);

        /**
         * @param translationKey the translation key for the hover tooltip
         * @return the builder
         */
        Builder setDescriptionTranslationKey(@Nullable String translationKey);

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
