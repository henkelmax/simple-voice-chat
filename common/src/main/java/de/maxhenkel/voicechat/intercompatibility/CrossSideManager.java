package de.maxhenkel.voicechat.intercompatibility;

import net.minecraft.server.MinecraftServer;

public abstract class CrossSideManager {

    private static CrossSideManager instance;

    public static CrossSideManager get() {
        if (instance == null) {
            if (CommonCompatibilityManager.INSTANCE.isDedicatedServer()) {
                instance = new DedicatedServerCrossSideManager();
            } else {
                try {
                    Class<?> crossSideManagerClass = Class.forName("de.maxhenkel.voicechat.intercompatibility.ClientCrossSideManager");
                    instance = (CrossSideManager) crossSideManagerClass.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return instance;
    }

    public abstract int getMtuSize();

    public abstract boolean useNatives();

    public abstract boolean shouldRunVoiceChatServer(MinecraftServer server);

}
