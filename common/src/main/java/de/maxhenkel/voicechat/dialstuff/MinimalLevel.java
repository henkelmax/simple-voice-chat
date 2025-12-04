package de.maxhenkel.voicechat.dialstuff;

import de.maxhenkel.voicechat.api.ServerPlayer;
import net.minecraft.world.level.Level;

import java.util.List;

public class MinimalLevel {
    public static MinimalLevel of(Level level) {
        return new MinimalLevel();
    }

    public List<ServerPlayer> players() {
        return List.of();
    }
}
