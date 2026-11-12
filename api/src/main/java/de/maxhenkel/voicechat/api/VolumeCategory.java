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
     * @return the ID of the parent category
     */
    @Nullable
    String getParent();

    public interface Builder {

        /**
         * This ID has to be between 1 and 16 characters and can only contain lowercase <code>a-z</code> and <code>_</code>.
         *
         * @param id the unique ID of the category
         * @return the builder
         */
        Builder setId(String id);

        /**
         * Can't be longer than 16 characters.
         *
         * @param name the full name of the category, that is displayed for the end user if no translation is present
         * @return the builder
         */
        Builder setName(String name);

        /**
         * Can't be longer than 256 characters.
         *
         * @param translationKey the translation key for the full name of the category
         * @return the builder
         */
        Builder setNameTranslationKey(@Nullable String translationKey);

        /**
         * Can't be longer than 256 characters.
         *
         * @param description the hover tooltip that is shown for this category if no translation is present
         * @return the builder
         */
        Builder setDescription(@Nullable String description);

        /**
         * Can't be longer than 256 characters.
         *
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
         * Nests this category below the provided parent category.
         * <br>
         * This makes the volume dependent on its parent - For example when the parent is at 50% will also halve the volume of this category.
         * <br>
         * <br>
         * <b>NOTE</b>: Only one level of nesting is allowed. Deeper nesting will result in undefined behavior!
         *
         * @param parent the ID of the parent category
         * @return the builder
         */
        Builder setParent(@Nullable String parent);

        /**
         * @return the built category
         * @throws IllegalStateException if the ID or name is not set, the ID or parent is invalid, a string is too long or the icon is not 16x16
         */
        VolumeCategory build();

    }

}
