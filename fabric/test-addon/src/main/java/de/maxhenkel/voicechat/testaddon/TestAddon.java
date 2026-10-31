package de.maxhenkel.voicechat.testaddon;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TestAddon implements ModInitializer {

    public static final Set<UUID> FORCE_MUTED = ConcurrentHashMap.newKeySet();
    public static final Set<UUID> FORCE_DISABLED = ConcurrentHashMap.newKeySet();

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, context, commandSelection) -> {
            dispatcher.register(Commands.literal("voicechattest")
                    .then(Commands.literal("forcemute").then(Commands.argument("target", EntityArgument.player()).executes(c -> toggle(c, FORCE_MUTED, "forceMuted"))))
                    .then(Commands.literal("forcedisable").then(Commands.argument("target", EntityArgument.player()).executes(c -> toggle(c, FORCE_DISABLED, "forceDisabled"))))
                    .then(Commands.literal("category").then(Commands.argument("id", StringArgumentType.word()).then(Commands.argument("name", StringArgumentType.string())
                            .executes(c -> registerCategory(c, null, null))
                            .then(Commands.argument("tab", StringArgumentType.word())
                                    .executes(c -> registerCategory(c, StringArgumentType.getString(c, "tab"), null))
                                    .then(Commands.argument("tabName", StringArgumentType.string())
                                            .executes(c -> registerCategory(c, StringArgumentType.getString(c, "tab"), StringArgumentType.getString(c, "tabName")))
                                    )
                            )
                    )))
            );
        });
    }

    private static int registerCategory(CommandContext<CommandSourceStack> context, @Nullable String tab, @Nullable String tabName) {
        String id = StringArgumentType.getString(context, "id");
        String name = StringArgumentType.getString(context, "name");
        if (!TestVoicechatPlugin.registerVolumeCategory(id, name, tab, tabName)) {
            context.getSource().sendFailure(Component.literal("Failed to register category"));
            return 0;
        }
        context.getSource().sendSuccess(Component.literal(String.format("Registered volume category %s", id)), false);
        return 1;
    }

    private static int toggle(CommandContext<CommandSourceStack> context, Set<UUID> players, String key) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "target");
        UUID uuid = player.getUUID();
        boolean forced = !players.remove(uuid);
        if (forced) {
            players.add(uuid);
        }
        TestVoicechatPlugin.evaluateForcedState(uuid);
        context.getSource().sendSuccess(Component.empty()
                .append(player.getDisplayName())
                .append(" ")
                .append(key)
                .append("=")
                .append(String.valueOf(forced)), false);
        return 1;
    }

}
