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
     * @return the ID of the tab this category is shown in or <code>null</code> if it is shown in the default tab
     */
    @Nullable
    String getTab();

    /**
     * @return the name of the tab, that is displayed for the end user
     */
    @Nullable
    String getTabName();

    /**
     * @return the translation key for the name of the tab
     */
    @Nullable
    String getTabTranslationKey();

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
         * Categories with the same tab ID are shown in the same tab.
         * Not providing a name/translation key will either fall back to the ID or another addons name.
         * In case you are adding multiple categories in the same tab, you only need to provide a name/translation key once.
         * This is also the case for other addons that use this ID.
         * Use <code>players</code> to show the category in the player tab.
         * If <code>other</code> left <code>null</code>, it will be shown in the other tab.
         * This ID has to be between 1 and 16 characters and can only contain lowercase <code>a-z</code> and <code>_</code>.
         *
         * @param tab the ID of the tab this category is shown in or <code>null</code> to show it in the default tab
         * @return the builder
         */
        Builder setTab(@Nullable String tab);

        /**
         * If no category in the tab provides a name, the tab ID is displayed.
         * Can't be longer than 16 characters.
         *
         * @param tabName the name of the tab, that is displayed for the end user if no translation is present
         * @return the builder
         */
        Builder setTabName(@Nullable String tabName);

        /**
         * Can't be longer than 256 characters.
         *
         * @param translationKey the translation key for the name of the tab
         * @return the builder
         */
        Builder setTabTranslationKey(@Nullable String translationKey);

        /**
         * @return the built category
         */
        VolumeCategory build();

    }

}
