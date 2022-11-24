package net.laboulangerie.townychat.listeners;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.laboulangerie.townychat.TownyChat;
import net.laboulangerie.townychat.channels.ChannelTypes;
import net.laboulangerie.townychat.events.AsyncChatHookEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.Executors;

public class DiscordHook implements Listener {

    private final PlainTextComponentSerializer plainText = PlainTextComponentSerializer.plainText();
    private final URI uri = URI.create(TownyChat.PLUGIN.getConfig().getString("webhook-url"));
    private final HttpClient httpClient = HttpClient.newBuilder()
            .executor(Executors.newSingleThreadExecutor())
            .build();
    private final String chat =
            """
            {
                "content": "<content>"
            }
            """;
    private final String session =
            """
            {
              "embeds": [{
                "author": {
                  "name": "<who> <what> the server",
                  "icon_url": "https://crafatar.com/avatars/<uuid>?overlay"
                }
              }]
            }
            """;

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMessage(AsyncChatHookEvent event) {
        if (event.getChannel().getType() == ChannelTypes.GLOBAL) {
            send(
                    chat,
                    "<content>", "**" + event.getPlayer().getName() + "** » " + plainText.serialize(event.getMessage()).replace('@', ' ')
            );
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        send(
                session,
                "<who>", event.getPlayer().getName(),
                "<what>", "joined",
                "<uuid>", event.getPlayer().getUniqueId().toString()
        );
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        send(
                session,
                "<who>", event.getPlayer().getName(),
                "<what>", "left",
                "<uuid>", event.getPlayer().getUniqueId().toString()
        );
    }

    private void send(String json, String... replacements) {
        for (int i = 0; i < replacements.length; i += 2) {
            json = json.replace(replacements[i], replacements[i + 1]);
        }
        httpClient.sendAsync(
                HttpRequest.newBuilder()
                        .header("Content-Type", "application/json")
                        .uri(uri)
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build(),
                HttpResponse.BodyHandlers.discarding()
        );
    }
}
