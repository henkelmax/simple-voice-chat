package de.maxhenkel.voicechat.integration;

import com.viaversion.viaversion.api.Via;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import de.maxhenkel.voicechat.net.FabricNetManager;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.Protocol1_12_2To1_13;

import java.util.Set;

public class ViaVersionCompatibility {

    private static final String OLD_VOICECHAT_PREFIX = "vc";

    private static boolean voicechatInitialized;

    public static void onVoicechatInitialized() {
        voicechatInitialized = true;
        register();
    }

    /**
     * Ran by <code>viafabric:via_api_initialized</code> entrypoint.
     * In case this runs before VC init, it gets skipped and register gets run by us.
     */
    public static void onViaInitialized() {
        if (voicechatInitialized) {
            register();
        }
    }

    private static void register() {
        try {
            if (FabricLoader.getInstance().isModLoaded("viaversion") && Via.isLoaded()) {
                registerMappings();
                Voicechat.LOGGER.info("Successfully registered ViaVersion mappings");
            }
        } catch (Throwable t) {
            Voicechat.LOGGER.error("Failed to register ViaVersion mappings", t);
        }
    }

    private static void registerMappings() {
        Set<Identifier> packets = ((FabricNetManager) CommonCompatibilityManager.INSTANCE.getNetManager()).getPackets();
        for (Identifier id : packets) {
            Protocol1_12_2To1_13.MAPPINGS.getChannelMappings().put(String.format("%s:%s", OLD_VOICECHAT_PREFIX, id.getPath()), id.toString());
        }
    }

}
