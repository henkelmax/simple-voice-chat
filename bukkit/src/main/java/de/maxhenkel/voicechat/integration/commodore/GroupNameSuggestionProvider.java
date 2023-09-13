package de.maxhenkel.voicechat.integration.commodore;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.voice.server.Group;
import de.maxhenkel.voicechat.voice.server.Server;

import java.util.concurrent.CompletableFuture;

public class GroupNameSuggestionProvider implements SuggestionProvider<Object> {

    public static final GroupNameSuggestionProvider INSTANCE = new GroupNameSuggestionProvider();

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<Object> context, SuggestionsBuilder builder) {
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
