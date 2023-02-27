package de.maxhenkel.voicechat.gui;

import de.maxhenkel.voicechat.api.Group;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

public enum GroupType {
    NORMAL(new TextComponentTranslation("message.voicechat.group_type.normal"), new TextComponentTranslation("message.voicechat.group_type.normal.description"), Group.Type.NORMAL),
    OPEN(new TextComponentTranslation("message.voicechat.group_type.open"), new TextComponentTranslation("message.voicechat.group_type.open.description"), Group.Type.OPEN),
    ISOLATED(new TextComponentTranslation("message.voicechat.group_type.isolated"), new TextComponentTranslation("message.voicechat.group_type.isolated.description"), Group.Type.ISOLATED);

    private final ITextComponent translation;
    private final ITextComponent description;
    private final Group.Type type;

    GroupType(ITextComponent translation, ITextComponent description, Group.Type type) {
        this.translation = translation;
        this.description = description;
        this.type = type;
    }

    public ITextComponent getTranslation() {
        return translation;
    }

    public ITextComponent getDescription() {
        return description;
    }

    public Group.Type getType() {
        return type;
    }

    public static GroupType fromType(Group.Type type) {
        for (GroupType groupType : values()) {
            if (groupType.getType() == type) {
                return groupType;
            }
        }
        return NORMAL;
    }

}
