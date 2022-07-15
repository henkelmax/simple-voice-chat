package de.maxhenkel.voicechat.intercompatibility;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.CommandDispatcher;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.events.PlayerEvents;
import de.maxhenkel.voicechat.net.NetManager;
import de.maxhenkel.voicechat.net.QuiltNetManager;
import de.maxhenkel.voicechat.permission.PermissionManager;
import de.maxhenkel.voicechat.permission.QuiltPermissionManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.impl.client.keybinding.KeyBindingRegistryImpl;
import net.fabricmc.fabric.mixin.client.keybinding.KeyCodeAccessor;
import net.minecraft.client.KeyMapping;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.ModMetadata;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.loader.api.entrypoint.EntrypointContainer;
import org.quiltmc.loader.api.minecraft.MinecraftQuiltLoader;
import org.quiltmc.qsl.command.api.CommandRegistrationCallback;
import org.quiltmc.qsl.lifecycle.api.event.ServerLifecycleEvents;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class QuiltCommonCompatibilityManager extends CommonCompatibilityManager {

    @Override
    public String getModVersion() {
        ModContainer modContainer = QuiltLoader.getModContainer(Voicechat.MODID).orElse(null);
        if (modContainer == null) {
            return "N/A";
        }
        return modContainer.metadata().version().raw();
    }

    @Override
    public String getModName() {
        ModContainer modContainer = QuiltLoader.getModContainer(Voicechat.MODID).orElse(null);
        if (modContainer == null) {
            return Voicechat.MODID;
        }
        return modContainer.metadata().name();
    }

    @Override
    public Path getGameDirectory() {
        return QuiltLoader.getGameDir();
    }

    @Override
    public void onServerStarting(Consumer<MinecraftServer> onServerStarting) {
        ServerLifecycleEvents.READY.register(onServerStarting::accept);
    }

    @Override
    public void onServerStopping(Consumer<MinecraftServer> onServerStopping) {
        ServerLifecycleEvents.STOPPING.register(onServerStopping::accept);
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
        CommandRegistrationCallback.EVENT.register((dispatcher, integrated, dedicated) -> onRegisterServerCommands.accept(dispatcher));
    }

    private QuiltNetManager netManager;

    @Override
    public NetManager getNetManager() {
        if (netManager == null) {
            netManager = new QuiltNetManager();
        }
        return netManager;
    }

    @Override
    public String listLoadedMods() {
        StringBuilder sb = new StringBuilder();
        for (ModContainer mod : QuiltLoader.getAllMods()) {
            ModMetadata metadata = mod.metadata();
            sb.append("Mod ID: " + metadata.id());
            sb.append("\n");
            sb.append("Name: " + metadata.name());
            sb.append("\n");
            sb.append("Version: " + metadata.version().raw());
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

    @Override
    public boolean isDevEnvironment() {
        return QuiltLoader.isDevelopmentEnvironment();
    }

    @Override
    public boolean isDedicatedServer() {
        return MinecraftQuiltLoader.getEnvironmentType().equals(EnvType.SERVER);
    }

    @Override
    public List<VoicechatPlugin> loadPlugins() {
        return QuiltLoader.getEntrypointContainers(Voicechat.MODID, VoicechatPlugin.class).stream().map(EntrypointContainer::getEntrypoint).collect(Collectors.toList());
    }

    @Override
    public PermissionManager createPermissionManager() {
        return new QuiltPermissionManager();
    }

}
