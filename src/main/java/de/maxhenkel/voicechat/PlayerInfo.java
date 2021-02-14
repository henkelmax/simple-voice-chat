package de.maxhenkel.voicechat;

import net.minecraft.text.Text;

import java.util.UUID;

public class PlayerInfo {

    private final UUID uuid;
    private final Text name;

    public PlayerInfo(UUID uuid, Text name) {
        this.uuid = uuid;
        this.name = name;
    }

    public UUID getUuid() {
        return uuid;
    }

    public Text getName() {
        return name;
    }

}
