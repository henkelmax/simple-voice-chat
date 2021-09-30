package de.maxhenkel.voicechat.intercompatibility;

import com.mojang.brigadier.CommandDispatcher;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.events.PlayerEvents;
import de.maxhenkel.voicechat.net.FabricNetManager;
import de.maxhenkel.voicechat.net.NetManager;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.nio.file.Path;
import java.util.function.Consumer;

public class FabricCommonCompatibilityManager extends CommonCompatibilityManager {

    @Override
    public String getModVersion() {
        ModContainer modContainer = FabricLoader.getInstance().getModContainer(Voicechat.MODID).orElse(null);
        if (modContainer == null) {
            return "N/A";
        }
        return modContainer.getMetadata().getVersion().getFriendlyString();
    }

    @Override
    public String getModName() {
        ModContainer modContainer = FabricLoader.getInstance().getModContainer(Voicechat.MODID).orElse(null);
        if (modContainer == null) {
            return Voicechat.MODID;
        }
        return modContainer.getMetadata().getName();
    }

    @Override
    public Path getGameDirectory() {
        return FabricLoader.getInstance().getGameDir();
    }

    @Override
    public void onServerStarting(Consumer<MinecraftServer> onServerStarting) {
        ServerLifecycleEvents.SERVER_STARTED.register(onServerStarting::accept);
    }

    @Override
    public void onPlayerLoggedIn(Consumer<ServerPlayer> onPlayerLoggedIn) {
        PlayerEvents.PLAYER_LOGGED_IN.register(onPlayerLoggedIn);
    }

    @Override
    public void onPlayerLoggedOut(Consumer<ServerPlayer> onPlayerLoggedOut) {
        PlayerEvents.PLAYER_LOGGED_OUT.register(onPlayerLoggedOut);
    }

    @Override
    public void onRegisterServerCommands(Consumer<CommandDispatcher<CommandSourceStack>> onRegisterServerCommands) {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> onRegisterServerCommands.accept(dispatcher));
    }

    private FabricNetManager netManager;

    @Override
    public NetManager getNetManager() {
        if (netManager == null) {
            netManager = new FabricNetManager();
        }
        return netManager;
    }

}
