package de.maxhenkel.voicechat.intercompatibility;

import com.mojang.brigadier.CommandDispatcher;
import de.maxhenkel.voicechat.BuildConstants;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.api.MinecraftServer;
import de.maxhenkel.voicechat.api.Player;
import de.maxhenkel.voicechat.api.ServerPlayer;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.command.VoicechatCommands;
import de.maxhenkel.voicechat.plugins.impl.VoicechatServerApiImpl;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.apache.logging.log4j.core.tools.picocli.CommandLine;

import java.util.function.Consumer;

public abstract class CommonCompatibilityManager extends UncommonCompatibilityManager {

    public abstract void onRegisterServerCommands(Consumer<CommandDispatcher<CommandSourceStack>> onRegisterServerCommands);

    @Override
    public VoicechatServerApi getServerApi() {
        return VoicechatServerApiImpl.INSTANCE;
    }

    @Override
    public void execute(MinecraftServer server, Runnable runnable) {
        ((net.minecraft.server.MinecraftServer) server.getMinecraftServer()).execute(runnable);
    }

    @Override
    public void sendIncompatibleMessage(ServerPlayer serverPlayer, int compatibilityVersion) {
        Component component;
        if (compatibilityVersion <= 6) {
            component = Component.literal(Voicechat.TRANSLATIONS.voicechatNotCompatibleMessage.get().formatted(BuildConstants.MOD_COMPATIBLE_VERSION, UncommonCompatibilityManager.INSTANCE.getModName()));
        } else {
            component = Component.translatableWithFallback("message.voicechat.incompatible_version",
                    "Your voice chat client version is not compatible with the server-side version.\nPlease install version %s of %s.",
                    Component.literal(BuildConstants.MOD_COMPATIBLE_VERSION).withStyle(ChatFormatting.BOLD),
                    Component.literal(UncommonCompatibilityManager.INSTANCE.getModName()).withStyle(ChatFormatting.BOLD));
        }
        getServerPlayer(serverPlayer).sendSystemMessage(component);
    }

    @Override
    public void registercommands() {
        ((CommonCompatibilityManager)CommonCompatibilityManager.INSTANCE).onRegisterServerCommands(VoicechatCommands::register);
    }

    public net.minecraft.server.level.ServerPlayer getServerPlayer(ServerPlayer player) {
        if (player.getEntity() instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            return serverPlayer;
        } else throw new IllegalArgumentException("api.ServerPlayer does not wrap level.ServerPlayer");
    }
}
