package net.laboulangerie.townychat.listeners;

import com.earth2me.essentials.Essentials;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.laboulangerie.townychat.TownyChat;
import net.laboulangerie.townychat.channels.Channel;
import net.laboulangerie.townychat.channels.ChannelTypes;
import net.laboulangerie.townychat.core.TownyChatRenderer;
import net.laboulangerie.townychat.events.AsyncChatHookEvent;
import net.laboulangerie.townychat.player.ChatPlayer;
import net.laboulangerie.townychat.player.ChatPlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;
import java.util.stream.Collectors;

public class TownyChatListener implements Listener {
    private final ChatPlayerManager chatPlayerManager;
    private final TownyChatRenderer townyChatRenderer;
    private final TownyAPI townyAPI;
    private final Essentials essentials;

    public TownyChatListener() {
        this.chatPlayerManager = TownyChat.PLUGIN.getChatPlayerManager();
        this.townyChatRenderer = TownyChat.PLUGIN.getTownyChatRenderer();
        this.townyAPI = TownyChat.PLUGIN.getTownyAPI();
        this.essentials = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerChat(AsyncChatEvent event) {
        event.viewers().clear();

        Player player = event.getPlayer();
        ChatPlayer chatPlayer = chatPlayerManager.getChatPlayer(player);

        Channel currentChannel = chatPlayer.getCurrentChannel();

        if (!(chatPlayer.getActiveChannels().contains(currentChannel))) {
            String errMessage = TownyChat.PLUGIN.getConfig().getString("lang.err_channel_disabled");

            TextComponent switchMessageComponent = (TextComponent) MiniMessage.miniMessage().deserialize(errMessage,
                    Placeholder.unparsed("channel", currentChannel.getType().name().toLowerCase()));

            TownyMessaging.sendErrorMsg(player, switchMessageComponent.content());
            event.setCancelled(true);
            return;
        }

        Resident resident = townyAPI.getResident(player.getUniqueId());

        if (chatPlayer.getCurrentChannel().getType() != ChannelTypes.GLOBAL) {
            for (ChatPlayer spy : chatPlayerManager.getSpies()) {
                Player spyPlayer = Bukkit.getPlayer(spy.getUniqueId());

                if (shouldSpy(spyPlayer, player)) {
                    spyPlayer.sendMessage(townyChatRenderer.spyRender(player, event.message()));
                }
            }
        }

        Set<Resident> residents = new HashSet<>();

        switch (currentChannel.getType()) {
            case TOWN -> {
                Town town = resident.getTownOrNull();
                if (town == null)
                    return;
                residents.addAll(town.getResidents());
            }
            case NATION -> {
                Nation nation = resident.getNationOrNull();
                if (nation == null)
                    return;
                residents.addAll(nation.getResidents());
            }
            case LOCAL -> {
                int radius = TownyChat.PLUGIN.getConfig().getInt("channels.local.radius");
                residents.addAll(getNearbyResidents(player, radius));
            }
            case GLOBAL -> residents.addAll(TownyUniverse.getInstance().getResidents());
        }

        Set<Player> recipients = residents.stream().map(Resident::getPlayer)
                // Filter null players
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // Removes players that disabled this channel
        recipients.removeIf(p ->
                !(chatPlayerManager.getChatPlayer(p).getActiveChannels().contains(currentChannel))
                || essentials.getUser(p).isIgnoredPlayer(essentials.getUser(player))
        );

        Component message = townyChatRenderer.render(player, player.displayName(), event.originalMessage());
        recipients.forEach(it -> it.sendMessage(message));
        Bukkit.getConsoleSender().sendMessage(message);

        AsyncChatHookEvent hookEvent = new AsyncChatHookEvent(event, currentChannel, !Bukkit.isPrimaryThread());
        Bukkit.getPluginManager().callEvent(hookEvent);
    }

    private Boolean shouldSpy(Player spy, Player spied) {
        Resident spyRes = townyAPI.getResident(spy.getUniqueId());
        Resident spiedRes = townyAPI.getResident(spied.getUniqueId());

        return spyRes.getTownOrNull() != spiedRes.getTownOrNull()
                || spyRes.getNationOrNull() != spiedRes.getNationOrNull();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        chatPlayerManager.loadChatPlayer(player);
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        chatPlayerManager.unloadChatPlayer(player);
    }

    private List<Resident> getNearbyResidents(Player player, int radius) {
        List<Resident> nearbyResidents = new ArrayList<Resident>();
        int playerBlockX = player.getLocation().getBlockX();
        int playerBlockZ = player.getLocation().getBlockZ();
        int radiusSquared = radius * radius;

        for (Player nearbyPlayer : player.getWorld().getPlayers()) {
            Location nearbyPlayerLocation = nearbyPlayer.getLocation();
            int dx = Math.abs(nearbyPlayerLocation.getBlockX() - playerBlockX);
            int dz = Math.abs(nearbyPlayerLocation.getBlockZ() - playerBlockZ);

            if (dx * dx + dz * dz <= radiusSquared) {
                nearbyResidents.add(townyAPI.getResident(nearbyPlayer));
            }
        }

        return nearbyResidents;
    }
}