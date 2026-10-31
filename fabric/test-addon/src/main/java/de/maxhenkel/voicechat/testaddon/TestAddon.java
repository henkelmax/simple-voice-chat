package de.maxhenkel.voicechat.testaddon;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.entity.player.ServerPlayerEntity;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TestAddon implements ModInitializer {

    public static final Set<UUID> FORCE_MUTED = ConcurrentHashMap.newKeySet();
    public static final Set<UUID> FORCE_DISABLED = ConcurrentHashMap.newKeySet();

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
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

    private static int registerCategory(CommandContext<CommandSource> context, @Nullable String tab, @Nullable String tabName) {
        String id = StringArgumentType.getString(context, "id");
        String name = StringArgumentType.getString(context, "name");
        if (!TestVoicechatPlugin.registerVolumeCategory(id, name, tab, tabName)) {
            context.getSource().sendFailure(new StringTextComponent("Failed to register category"));
            return 0;
        }
        context.getSource().sendSuccess(new StringTextComponent(String.format("Registered volume category %s", id)), false);
        return 1;
    }

    private static int toggle(CommandContext<CommandSource> context, Set<UUID> players, String key) throws CommandSyntaxException {
        ServerPlayerEntity player = EntityArgument.getPlayer(context, "target");
        UUID uuid = player.getUUID();
        boolean forced = !players.remove(uuid);
        if (forced) {
            players.add(uuid);
        }
        TestVoicechatPlugin.evaluateForcedState(uuid);
        context.getSource().sendSuccess(new StringTextComponent("")
                .append(player.getDisplayName())
                .append(" ")
                .append(key)
                .append("=")
                .append(String.valueOf(forced)), false);
        return 1;
    }

}
