package de.maxhenkel.voicechat.intercompatibility;

import de.maxhenkel.voicechat.Voicechat;
import net.minecraft.server.MinecraftServer;

public class DedicatedServerCrossSideManager extends CrossSideManager {

    @Override
    public int getMtuSize() {
        return Voicechat.SERVER_CONFIG.voiceChatMtuSize.get();
    }

    @Override
    public boolean useNatives() {
        return Voicechat.SERVER_CONFIG.useNatives.get();
    }

    @Override
    public boolean shouldRunVoiceChatServer(MinecraftServer server) {
        return true;
    }
}
