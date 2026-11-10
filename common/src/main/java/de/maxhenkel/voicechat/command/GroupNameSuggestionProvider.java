package de.maxhenkel.voicechat.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.voice.server.Group;
import de.maxhenkel.voicechat.voice.server.Server;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.suggestions.ServerOnlyCustomSuggestion;
import net.minecraft.commands.suggestions.SuggestionContext;
import net.minecraft.commands.suggestions.SuggestionProviders;
import net.minecraft.server.MinecraftServer;

import java.util.concurrent.CompletableFuture;

public class GroupNameSuggestionProvider implements ServerOnlyCustomSuggestion {

    public static final SuggestionProvider<CommandSourceStack> INSTANCE = SuggestionProviders.serverOnly(new GroupNameSuggestionProvider());

    @Override
    public CompletableFuture<Suggestions> generate(MinecraftServer minecraftServer, CommandContext<? extends SuggestionContext> context, SuggestionsBuilder builder) {
        Server server = Voicechat.SERVER.getServer();
        if (server == null) {
            return builder.buildFuture();
        }
        server.getGroupManager().getGroups().values().stream().map(Group::getName).distinct().map(s -> {
            if (s.contains(" ")) {
                return String.format("\"%s\"", s);
            }
            return s;
        }).forEach(builder::suggest);
        return builder.buildFuture();
    }
}
