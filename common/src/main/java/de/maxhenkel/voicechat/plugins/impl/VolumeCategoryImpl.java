package de.maxhenkel.voicechat.plugins.impl;

import de.maxhenkel.voicechat.api.VolumeCategory;
import net.minecraft.client.resources.I18n;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

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

    public VolumeCategoryImpl(String id, String name, @Nullable String nameTranslationKey, @Nullable String description, @Nullable String descriptionTranslationKey, @Nullable int[][] icon) {
        if (!ID_REGEX.matcher(id).matches()) {
            throw new IllegalArgumentException("Volume category ID can only contain a-z and _ with a maximum amount of 16 characters");
        }
        this.id = id;
        this.name = name;
        this.nameTranslationKey = nameTranslationKey;
        this.description = description;
        this.descriptionTranslationKey = descriptionTranslationKey;
        this.icon = icon;
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
            if (I18n.hasKey(nameTranslationKey)) {
                return new TextComponentTranslation(nameTranslationKey);
            }
        }
        return new TextComponentString(name);
    }

    public String getSearchName() {
        if (nameTranslationKey == null) {
            return name;
        }
        if (I18n.hasKey(nameTranslationKey)) {
            return I18n.format(nameTranslationKey);
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
            if (I18n.hasKey(descriptionTranslationKey)) {
                return new TextComponentTranslation(descriptionTranslationKey);
            }
        }
        return description != null ? new TextComponentString(description) : new TextComponentString("");
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

    public static VolumeCategoryImpl fromBytes(PacketBuffer buf) {
        String id = buf.readString(16);
        String name = buf.readString(16);
        String nameTranslationKey = readOptionalString(buf);
        String description = readOptionalString(buf);
        String descriptionTranslationKey = readOptionalString(buf);
        int[][] icon = null;
        if (buf.readBoolean()) {
            icon = new int[16][16];
            for (int x = 0; x < icon.length; x++) {
                for (int y = 0; y < icon.length; y++) {
                    icon[x][y] = buf.readInt();
                }
            }
        }
        return new VolumeCategoryImpl(id, name, nameTranslationKey, description, descriptionTranslationKey, icon);
    }

    @Nullable
    private static String readOptionalString(PacketBuffer buf) {
        if (buf.readBoolean()) {
            return buf.readString(32767);
        }
        return null;
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeString(id);
        buf.writeString(name);
        writeOptionalString(buf, nameTranslationKey);
        writeOptionalString(buf, description);
        writeOptionalString(buf, descriptionTranslationKey);
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
    }

    private static void writeOptionalString(PacketBuffer buf, @Nullable String string) {
        buf.writeBoolean(string != null);
        if (string != null) {
            buf.writeString(string);
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
        public VolumeCategory build() {
            if (id == null) {
                throw new IllegalStateException("id missing");
            }
            if (name == null) {
                throw new IllegalStateException("name missing");
            }
            return new VolumeCategoryImpl(id, name, nameTranslationKey, description, descriptionTranslationKey, icon);
        }
    }

}
