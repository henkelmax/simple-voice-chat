package de.maxhenkel.voicechat.testaddon;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.StringArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class TestAddon implements ModInitializer {

    public static final Set<UUID> FORCE_MUTED = ConcurrentHashMap.newKeySet();
    public static final Set<UUID> FORCE_DISABLED = ConcurrentHashMap.newKeySet();
    public static final Set<UUID> HIDDEN = ConcurrentHashMap.newKeySet();
    public static final Set<UUID> PREVENT_LEAVE_GROUP = ConcurrentHashMap.newKeySet();
    public static final Set<UUID> PREVENT_CREATE_GROUP = ConcurrentHashMap.newKeySet();

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, context, commandSelection) -> {
            dispatcher.register(Commands.literal("voicechattest")
                    .then(Commands.literal("forcemute").then(Commands.argument("target", EntityArgument.player()).executes(c -> toggle(c, FORCE_MUTED, "forceMuted", TestVoicechatPlugin::evaluateForcedState))))
                    .then(Commands.literal("forcedisable").then(Commands.argument("target", EntityArgument.player()).executes(c -> toggle(c, FORCE_DISABLED, "forceDisabled", TestVoicechatPlugin::evaluateForcedState))))
                    .then(Commands.literal("hide").then(Commands.argument("target", EntityArgument.player()).executes(c -> toggle(c, HIDDEN, "hidden", TestVoicechatPlugin::evaluateVisibility))))
                    .then(Commands.literal("preventleavegroup").then(Commands.argument("target", EntityArgument.player()).executes(c -> toggle(c, PREVENT_LEAVE_GROUP, "preventLeaveGroup", uuid -> {}))))
                    .then(Commands.literal("preventcreategroup").then(Commands.argument("target", EntityArgument.player()).executes(c -> toggle(c, PREVENT_CREATE_GROUP, "preventCreateGroup", uuid -> {}))))
                    .then(Commands.literal("category").then(Commands.argument("id", StringArgument.word()).then(Commands.argument("name", StringArgument.string())
                            .executes(c -> registerCategory(c, null, null))
                            .then(Commands.argument("tab", StringArgument.word())
                                    .executes(c -> registerCategory(c, StringArgument.getString(c, "tab"), null))
                                    .then(Commands.argument("tabName", StringArgument.string())
                                            .executes(c -> registerCategory(c, StringArgument.getString(c, "tab"), StringArgument.getString(c, "tabName")))
                                    )
                            )
                    )))
            );
        });
    }

    private static int registerCategory(CommandContext<CommandSourceStack> context, @Nullable String tab, @Nullable String tabName) {
        String id = StringArgument.getString(context, "id");
        String name = StringArgument.getString(context, "name");
        if (!TestVoicechatPlugin.registerVolumeCategory(id, name, tab, tabName)) {
            context.getSource().sendFailure(Component.literal("Failed to register category"));
            return 0;
        }
        context.getSource().sendSuccess(() -> Component.literal(String.format("Registered volume category %s", id)), false);
        return 1;
    }

    private static int toggle(CommandContext<CommandSourceStack> context, Set<UUID> players, String key, Consumer<UUID> evaluate) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "target");
        UUID uuid = player.getUUID();
        boolean forced = !players.remove(uuid);
        if (forced) {
            players.add(uuid);
        }
        evaluate.accept(uuid);
        context.getSource().sendSuccess(() -> Component.empty()
                .append(player.getDisplayName())
                .append(" ")
                .append(key)
                .append("=")
                .append(String.valueOf(forced)), false);
        return 1;
    }

}
