package de.maxhenkel.voicechat.gui;

import de.maxhenkel.voicechat.api.Group;
import net.minecraft.network.chat.Component;

public enum GroupType {
    NORMAL(Component.translatable("message.voicechat.group_type.normal"), Component.translatable("message.voicechat.group_type.normal.description"), Group.Type.NORMAL),
    OPEN(Component.translatable("message.voicechat.group_type.open"), Component.translatable("message.voicechat.group_type.open.description"), Group.Type.OPEN),
    ISOLATED(Component.translatable("message.voicechat.group_type.isolated"), Component.translatable("message.voicechat.group_type.isolated.description"), Group.Type.ISOLATED);

    private final Component translation;
    private final Component description;
    private final Group.Type type;

    GroupType(Component translation, Component description, Group.Type type) {
        this.translation = translation;
        this.description = description;
        this.type = type;
    }

    public Component getTranslation() {
        return translation;
    }

    public Component getDescription() {
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
