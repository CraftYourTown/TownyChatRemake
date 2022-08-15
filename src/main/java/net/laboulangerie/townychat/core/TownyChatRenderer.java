package net.laboulangerie.townychat.core;

import io.papermc.paper.chat.ChatRenderer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.laboulangerie.townychat.TownyChat;
import net.laboulangerie.townychat.player.ChatPlayer;
import net.laboulangerie.townychat.player.ChatPlayerManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TownyChatRenderer implements ChatRenderer.ViewerUnaware {
    private final FileConfiguration config;
    private final ChatPlayerManager chatPlayerManager;
    private final ComponentRenderer componentRenderer;
    private final Map<String, TagResolver> resolverMap = Map.of(
            "townychat.format.color", StandardTags.color(),
            "townychat.format.bold", TagResolver.resolver(Set.of("bold", "b"), (argumentQueue, context) -> Tag.styling(TextDecoration.BOLD)),
            "townychat.format.underline", TagResolver.resolver(Set.of("underline", "u"), (argumentQueue, context) -> Tag.styling(TextDecoration.UNDERLINED)),
            "townychat.format.italic", TagResolver.resolver(Set.of("italic", "i", "em"), (argumentQueue, context) -> Tag.styling(TextDecoration.ITALIC)),
            "townychat.format.strikethrough", TagResolver.resolver(Set.of("strikethrough", "st"), (argumentQueue, context) -> Tag.styling(TextDecoration.STRIKETHROUGH)),
            "townychat.format.obfuscated", TagResolver.resolver(Set.of("obfuscated", "obf"), (argumentQueue, context) -> Tag.styling(TextDecoration.OBFUSCATED)),
            "townychat.format.rainbow", StandardTags.rainbow()
    );

    public TownyChatRenderer() {
        this.config = TownyChat.PLUGIN.getConfig();
        this.chatPlayerManager = TownyChat.PLUGIN.getChatPlayerManager();
        this.componentRenderer = TownyChat.PLUGIN.getComponentRenderer();
    }

    @Override
    public @NotNull Component render(@NotNull Player source, @NotNull Component sourceDisplayName,
                                     @NotNull Component message) {

        ChatPlayer chatPlayer = chatPlayerManager.getChatPlayer(source);
        String channelFormat = chatPlayer.getCurrentChannel().getFormat();

        // Censor the message with the word blacklist
        String plainText = PlainTextComponentSerializer.plainText().serialize(message);
        censorString(plainText);

        List<TagResolver> resolvers = new ArrayList<>();
        resolverMap.forEach((perm, resolver) -> {
            if (source.hasPermission(perm)) {
                resolvers.add(resolver);
            }
        });

        message = MiniMessage.builder()
                .tags(TagResolver.builder().resolvers(resolvers).build())
                .build()
                .deserialize(plainText);

        return componentRenderer.parse(source, channelFormat, Placeholder.component("message", message));
    }

    // TODO : Remove redundant method, but how???
    public @NotNull Component spyRender(@NotNull Player source, @NotNull Component message) {

        ChatPlayer chatPlayer = chatPlayerManager.getChatPlayer(source);
        String channelFormat = chatPlayer.getCurrentChannel().getSpyFormat();

        if (source.hasPermission("townychat.format")) {
            TextComponent textMessage = (TextComponent) message;
            message = MiniMessage.miniMessage().deserialize(textMessage.content());
        }

        return componentRenderer.parse(source, channelFormat, Placeholder.component("message", message));
    }

    private String censorString(String string) {
        List<String> words = config.getStringList("blacklist");
        String[] censorChars = {"#", "@", "!", "*"};

        for (String word : words) {
            // Not readable to say the least but i like it
            // It generates a random string e.g. insult -> !#@!*#
            // Yes it's overcomplicated but it looks cool :)
            string = string.replaceAll("(?i)" + Pattern.quote(word),
                    new Random().ints(word.length(), 0, censorChars.length).mapToObj(i -> censorChars[i])
                            .collect(Collectors.joining()));
        }

        return string;
    }
}