package de.maxhenkel.voicechat.intercompatibility;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.CommandDispatcher;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.events.PlayerEvents;
import de.maxhenkel.voicechat.net.FabricNetManager;
import de.maxhenkel.voicechat.net.NetManager;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.impl.client.keybinding.KeyBindingRegistryImpl;
import net.fabricmc.fabric.mixin.client.keybinding.KeyCodeAccessor;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModDependency;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.KeyMapping;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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

    @Override
    public String listLoadedMods() {
        StringBuilder sb = new StringBuilder();
        for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
            ModMetadata metadata = mod.getMetadata();
            sb.append("Mod ID: " + metadata.getId());
            sb.append("\n");
            sb.append("Name: " + metadata.getName());
            sb.append("\n");
            sb.append("Version: " + metadata.getVersion());
            sb.append("\n");
            sb.append("Dependencies: " + metadata.getDepends().stream().map(ModDependency::getModId).collect(Collectors.joining(", ")));
            sb.append("\n\n");
        }
        return sb.toString();
    }

    @Override
    public String listKeybinds() {
        StringBuilder sb = new StringBuilder();
        try {
            Field moddedKeyBindings = KeyBindingRegistryImpl.class.getDeclaredField("moddedKeyBindings");
            moddedKeyBindings.setAccessible(true);
            List<KeyMapping> mappings = (List<KeyMapping>) moddedKeyBindings.get(null);
            for (KeyMapping mapping : mappings) {
                InputConstants.Key boundKey = ((KeyCodeAccessor) mapping).fabric_getBoundKey();
                sb.append(mapping.getName() + "(" + mapping.getCategory() + "): " + boundKey.getName() + " (" + mapping.getDefaultKey().getName() + ")");
                sb.append("\n");
            }
            sb.append("\n");
        } catch (Exception e) {
            sb.append("Error: " + e.getMessage());
            sb.append("\n");
        }
        return sb.toString();
    }

}
