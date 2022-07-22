package net.laboulangerie.townychat.channels;

import com.palmergames.bukkit.towny.object.Government;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import net.laboulangerie.townychat.TownyChat;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ChannelManager {

    private final Map<Government, Channel> channelsMap;

    public ChannelManager() {
        this.channelsMap = new HashMap<>();
        loadChannels();
    }

    public Channel getChannel(Government government) {
        return this.channelsMap.get(government);
    }

    public Map<Government, Channel> getChannels() {
        return this.channelsMap;
    }

    public void addChannel(Government government, Channel channel) {
        this.channelsMap.put(government, channel);
    }

    public void removeChannel(Government government) {
        this.channelsMap.remove(government);
    }

    public void loadChannels() {
        Collection<Town> towns = TownyChat.PLUGIN.getTownyUniverse().getTowns();

        for (Town town : towns) {
            Channel townChannel = new Channel(ChannelTypes.TOWN, town);
            this.channelsMap.put(town, townChannel);
        }

        Collection<Nation> nations = TownyChat.PLUGIN.getTownyUniverse().getNations();

        for (Nation nation : nations) {
            Channel nationChannel = new Channel(ChannelTypes.NATION, nation);
            this.channelsMap.put(nation, nationChannel);
        }
    }

    public void unloadChannels() {
        this.channelsMap.clear();
    }
}
