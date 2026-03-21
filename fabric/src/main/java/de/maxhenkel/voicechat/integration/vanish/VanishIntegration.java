package de.maxhenkel.voicechat.integration.vanish;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.events.VanishEvents;
import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import me.drex.vanish.api.VanishAPI;
import net.minecraft.server.level.ServerPlayer;

public class VanishIntegration {

    private static Boolean loaded;

    public static boolean isLoaded() {
        if (loaded == null) {
            loaded = checkLoaded();
        }
        return loaded;
    }

    private static boolean checkLoaded() {
        if (CommonCompatibilityManager.INSTANCE.isModLoaded("melius-vanish") || CommonCompatibilityManager.INSTANCE.isModLoaded("vanish")) {
            try {
                Class.forName("me.drex.vanish.api.VanishAPI");
                Voicechat.LOGGER.info("Enabling vanish compatibility");
                return true;
            } catch (Throwable t) {
                Voicechat.LOGGER.warn("Failed to load vanish compatibility", t);
            }
        }
        return false;
    }

    public static void init() {
        if (!isLoaded()) {
            return;
        }
        try {
            me.drex.vanish.api.VanishEvents.VANISH_EVENT.register((vanishPlayer, vanish) -> {
                for (ServerPlayer player : vanishPlayer.level().getServer().getPlayerList().getPlayers()) {
                    if (vanish) {
                        if (CommonCompatibilityManager.INSTANCE.canSee(player, vanishPlayer)) {
                            continue;
                        }
                        VanishEvents.ON_VANISH.invoker().accept(vanishPlayer, player);
                    } else {
                        if (!CommonCompatibilityManager.INSTANCE.canSee(player, vanishPlayer)) {
                            continue;
                        }
                        VanishEvents.ON_UNVANISH.invoker().accept(vanishPlayer, player);
                    }
                }
            });
        } catch (Throwable t) {
            Voicechat.LOGGER.warn("Failed to use vanish compatibility", t);
            loaded = false;
        }
    }

    public static boolean canSee(ServerPlayer player, ServerPlayer other) {
        if (isLoaded()) {
            try {
                return VanishAPI.canSeePlayer(other, player);
            } catch (Throwable t) {
                Voicechat.LOGGER.warn("Failed to use vanish compatibility", t);
                loaded = false;
            }
        }
        return true;
    }

}
