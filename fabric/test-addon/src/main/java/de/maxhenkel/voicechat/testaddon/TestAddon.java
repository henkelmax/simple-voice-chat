package de.maxhenkel.voicechat.testaddon;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

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
            );
        });
    }

    private static int toggle(CommandContext<CommandSourceStack> context, Set<UUID> players, String key) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "target");
        UUID uuid = player.getUUID();
        boolean forced = !players.remove(uuid);
        if (forced) {
            players.add(uuid);
        }
        context.getSource().sendSuccess(() -> Component.empty()
                .append(player.getDisplayName())
                .append(" ")
                .append(key)
                .append("=")
                .append(String.valueOf(forced)), false);
        return 1;
    }

}
