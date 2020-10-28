package de.maxhenkel.voicechat;

import net.minecraft.util.text.ITextComponent;

import java.util.UUID;

public class PlayerInfo {

    private final UUID uuid;
    private final ITextComponent name;

    public PlayerInfo(UUID uuid, ITextComponent name) {
        this.uuid = uuid;
        this.name = name;
    }

    public UUID getUuid() {
        return uuid;
    }

    public ITextComponent getName() {
        return name;
    }

}
