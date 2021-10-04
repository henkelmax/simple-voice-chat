package de.maxhenkel.voicechat.intercompatibility;

import com.mojang.brigadier.CommandDispatcher;
import de.maxhenkel.voicechat.net.NetManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.nio.file.Path;
import java.util.function.Consumer;

public abstract class CommonCompatibilityManager {

    public static CommonCompatibilityManager INSTANCE;

    public abstract String getModVersion();

    public abstract String getModName();

    public abstract Path getGameDirectory();

    public abstract void onServerStarting(Consumer<MinecraftServer> onServerStarting);

    public abstract void onPlayerLoggedIn(Consumer<ServerPlayer> onPlayerLoggedIn);

    public abstract void onPlayerLoggedOut(Consumer<ServerPlayer> onPlayerLoggedOut);

    public abstract void onRegisterServerCommands(Consumer<CommandDispatcher<CommandSourceStack>> onRegisterServerCommands);

    public abstract NetManager getNetManager();

    public abstract String listLoadedMods();

    public abstract String listKeybinds();

    public abstract boolean isDevEnvironment();

}
