package de.maxhenkel.voicechat.plugins.impl;

import de.maxhenkel.voicechat.api.VolumeCategory;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.LanguageMap;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;
import java.util.regex.Pattern;

public class VolumeCategoryImpl implements VolumeCategory {

    public static final Pattern ID_REGEX = Pattern.compile("^[a-z_]{1,16}$");

    private final String id;
    private final String name;
    @Nullable
    private final String nameTranslationKey;
    @Nullable
    private final String description;
    @Nullable
    private final String descriptionTranslationKey;
    @Nullable
    private final int[][] icon;
    @Nullable
    private final String tab;
    @Nullable
    private final String tabName;
    @Nullable
    private final String tabTranslationKey;

    public VolumeCategoryImpl(String id, String name, @Nullable String nameTranslationKey, @Nullable String description, @Nullable String descriptionTranslationKey, @Nullable int[][] icon, @Nullable String tab, @Nullable String tabName, @Nullable String tabTranslationKey) {
        if (!ID_REGEX.matcher(id).matches()) {
            throw new IllegalArgumentException("Volume category ID can only contain a-z and _ with a maximum amount of 16 characters");
        }
        if (tab != null && !ID_REGEX.matcher(tab).matches()) {
            throw new IllegalArgumentException("Volume category tab can only contain a-z and _ with a maximum amount of 16 characters");
        }
        this.id = id;
        this.name = name;
        this.nameTranslationKey = nameTranslationKey;
        this.description = description;
        this.descriptionTranslationKey = descriptionTranslationKey;
        this.icon = icon;
        this.tab = tab;
        this.tabName = tabName;
        this.tabTranslationKey = tabTranslationKey;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    @Nullable
    public String getNameTranslationKey() {
        return nameTranslationKey;
    }

    public ITextComponent getDisplayName() {
        if (nameTranslationKey != null) {
            LanguageMap lang = LanguageMap.getInstance();
            if (lang.has(nameTranslationKey)) {
                return new TranslationTextComponent(nameTranslationKey);
            }
        }
        return new StringTextComponent(name);
    }

    public String getSearchName() {
        if (nameTranslationKey == null) {
            return name;
        }
        LanguageMap lang = LanguageMap.getInstance();
        if (lang.has(nameTranslationKey)) {
            return lang.getOrDefault(nameTranslationKey);
        }
        return name;
    }

    @Nullable
    @Override
    public String getDescription() {
        return description;
    }

    public ITextComponent getDisplayDescription() {
        if (descriptionTranslationKey != null) {
            LanguageMap lang = LanguageMap.getInstance();
            if (lang.has(descriptionTranslationKey)) {
                return new TranslationTextComponent(descriptionTranslationKey);
            }
        }
        return description != null ? new StringTextComponent(description) : StringTextComponent.EMPTY;
    }

    @Override
    @Nullable
    public String getDescriptionTranslationKey() {
        return descriptionTranslationKey;
    }

    @Nullable
    @Override
    public int[][] getIcon() {
        return icon;
    }

    @Nullable
    @Override
    public String getTab() {
        return tab;
    }

    @Nullable
    @Override
    public String getTabName() {
        return tabName;
    }

    /**
     * @return the tab name or <code>null</code> if this category doesn't provide one, so the name of another category in the same tab or the tab ID can be used instead
     */
    @Nullable
    public ITextComponent getTabDisplayName() {
        if (tabTranslationKey != null) {
            LanguageMap lang = LanguageMap.getInstance();
            if (lang.has(tabTranslationKey)) {
                return new TranslationTextComponent(tabTranslationKey);
            }
            return new StringTextComponent(tabName != null ? tabName : tab);
        }
        return tabName != null ? new StringTextComponent(tabName) : null;
    }

    @Nullable
    @Override
    public String getTabTranslationKey() {
        return tabTranslationKey;
    }

    public static VolumeCategoryImpl fromBytes(PacketBuffer buf) {
        String id = buf.readUtf(16);
        String name = buf.readUtf(16);
        String nameTranslationKey = readOptionalString(buf, 256);
        String description = readOptionalString(buf, 256);
        String descriptionTranslationKey = readOptionalString(buf, 256);
        int[][] icon = null;
        if (buf.readBoolean()) {
            icon = new int[16][16];
            for (int x = 0; x < icon.length; x++) {
                for (int y = 0; y < icon.length; y++) {
                    icon[x][y] = buf.readInt();
                }
            }
        }
        String tab = readOptionalString(buf, 16);
        String tabName = readOptionalString(buf, 16);
        String tabTranslationKey = readOptionalString(buf, 256);
        return new VolumeCategoryImpl(id, name, nameTranslationKey, description, descriptionTranslationKey, icon, tab, tabName, tabTranslationKey);
    }

    @Nullable
    private static String readOptionalString(PacketBuffer buf, int maxLength) {
        if (buf.readBoolean()) {
            return buf.readUtf(maxLength);
        }
        return null;
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeUtf(id, 16);
        buf.writeUtf(name, 16);
        writeOptionalString(buf, nameTranslationKey, 256);
        writeOptionalString(buf, description, 256);
        writeOptionalString(buf, descriptionTranslationKey, 256);
        buf.writeBoolean(icon != null);
        if (icon != null) {
            if (icon.length != 16) {
                throw new IllegalStateException("Icon is not 16x16");
            }
            for (int x = 0; x < icon.length; x++) {
                if (icon[x].length != 16) {
                    throw new IllegalStateException("Icon is not 16x16");
                }
                for (int y = 0; y < icon.length; y++) {
                    buf.writeInt(icon[x][y]);
                }
            }
        }
        writeOptionalString(buf, tab, 16);
        writeOptionalString(buf, tabName, 16);
        writeOptionalString(buf, tabTranslationKey, 256);
    }

    private static void writeOptionalString(PacketBuffer buf, @Nullable String string, int maxLength) {
        buf.writeBoolean(string != null);
        if (string != null) {
            buf.writeUtf(string, maxLength);
        }
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        VolumeCategoryImpl that = (VolumeCategoryImpl) object;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public static class BuilderImpl implements VolumeCategory.Builder {

        private String id;
        private String name;
        @Nullable
        private String nameTranslationKey;
        @Nullable
        private String description;
        @Nullable
        private String descriptionTranslationKey;
        @Nullable
        private int[][] icon;
        @Nullable
        private String tab;
        @Nullable
        private String tabName;
        @Nullable
        private String tabTranslationKey;

        @Override
        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        @Override
        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        @Override
        public Builder setNameTranslationKey(@Nullable String translationKey) {
            this.nameTranslationKey = translationKey;
            return this;
        }

        @Override
        public Builder setDescription(@Nullable String description) {
            this.description = description;
            return this;
        }

        @Override
        public Builder setDescriptionTranslationKey(@Nullable String translationKey) {
            this.descriptionTranslationKey = translationKey;
            return this;
        }

        @Override
        public Builder setIcon(@Nullable int[][] icon) {
            this.icon = icon;
            return this;
        }

        @Override
        public Builder setTab(@Nullable String tab) {
            this.tab = tab;
            return this;
        }

        @Override
        public Builder setTabName(@Nullable String tabName) {
            this.tabName = tabName;
            return this;
        }

        @Override
        public Builder setTabTranslationKey(@Nullable String translationKey) {
            this.tabTranslationKey = translationKey;
            return this;
        }

        @Override
        public VolumeCategory build() {
            if (id == null) {
                throw new IllegalStateException("Id missing");
            }
            if (name == null) {
                throw new IllegalStateException("Name missing");
            }
            checkLength(name, 16, "Name");
            checkLength(nameTranslationKey, 256, "Name translation key");
            checkLength(description, 256, "Description");
            checkLength(descriptionTranslationKey, 256, "Description translation key");
            checkLength(tabName, 16, "Tab name");
            checkLength(tabTranslationKey, 256, "Tab translation key");
            return new VolumeCategoryImpl(id, name, nameTranslationKey, description, descriptionTranslationKey, icon, tab, tabName, tabTranslationKey);
        }

        private static void checkLength(@Nullable String value, int maxLength, String name) {
            if (value != null && value.length() > maxLength) {
                throw new IllegalStateException(String.format("%s can't be longer than %s characters", name, maxLength));
            }
        }
    }

}
