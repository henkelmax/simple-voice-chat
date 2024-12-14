package de.maxhenkel.voicechat;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.RootCommandNode;
import de.maxhenkel.voicechat.command.VoicechatCommands;
import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import de.maxhenkel.voicechat.intercompatibility.PaperCommonCompatibilityManager;
import io.papermc.paper.command.brigadier.ApiMirrorRootNode;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.bootstrap.PluginProviderContext;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.minecraft.commands.CommandSourceStack;
import org.bukkit.plugin.java.JavaPlugin;

public final class VoicechatBootstrap implements PluginBootstrap {

    @Override
    public void bootstrap(BootstrapContext context) {
        LifecycleEventManager<BootstrapContext> lifecycleManager = context.getLifecycleManager();
        lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS, (event) -> {
            PaperCommonCompatibilityManager manager = (PaperCommonCompatibilityManager) CommonCompatibilityManager.INSTANCE;
            Commands registrar = event.registrar();
            RootCommandNode<io.papermc.paper.command.brigadier.CommandSourceStack> root = registrar.getDispatcher().getRoot();
            if (root instanceof ApiMirrorRootNode apiMirrorRootNode) {
                CommandDispatcher<CommandSourceStack> dispatcher = apiMirrorRootNode.getDispatcher();
                manager.onRegisterCommands(dispatcher);
                VoicechatCommands.register(dispatcher);
            } else {
                Voicechat.LOGGER.error("Failed to register commands");
            }

        });
    }

    @Override
    public JavaPlugin createPlugin(PluginProviderContext context) {
        return new VoicechatPaperPlugin();
    }

}
