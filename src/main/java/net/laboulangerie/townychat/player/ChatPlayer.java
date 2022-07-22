package net.laboulangerie.townychat.player;

import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import net.laboulangerie.townychat.TownyChat;
import net.laboulangerie.townychat.channels.Channel;
import net.laboulangerie.townychat.channels.ChannelManager;
import net.laboulangerie.townychat.channels.ChannelTypes;
import org.bukkit.OfflinePlayer;

import java.util.*;

public class ChatPlayer {
    private final UUID uniqueId;

    private final ChannelManager channelManager;

    private Channel currentChannel;
    private final Map<ChannelTypes, Channel> channels;
    private final Set<Channel> activeChannels;

    private boolean isSpying;

    public ChatPlayer(OfflinePlayer player) {
        this.uniqueId = player.getUniqueId();

        this.channelManager = TownyChat.PLUGIN.getChannelManager();

        this.channels = new HashMap<>();
        this.activeChannels = new HashSet<>();

        this.isSpying = false;

        loadPlayerChannels();
    }

    public UUID getUniqueId() {
        return this.uniqueId;
    }

    public Channel getCurrentChannel() {
        return this.currentChannel;
    }

    public void setCurrentChannel(ChannelTypes channelType) {
        this.currentChannel = this.channels.get(channelType);
    }

    public Channel getChannel(ChannelTypes channelType) {
        return this.channels.get(channelType);
    }

    public Map<ChannelTypes, Channel> getChannels() {
        return this.channels;
    }

    public void addChannel(ChannelTypes channelType, Channel channel) {
        this.activeChannels.add(channel);
        this.channels.put(channelType, channel);
    }

    public boolean isSpying() {
        return this.isSpying;
    }

    public void setSpying(boolean isSpying) {
        this.isSpying = isSpying;
    }

    public void removeChannel(ChannelTypes channelType) {
        Channel channel = getChannel(channelType);
        this.activeChannels.remove(channel);
        this.channels.remove(channelType);
    }

    public Set<Channel> getActiveChannels() {
        return this.activeChannels;
    }

    public boolean toggleChannel(ChannelTypes channelType) {
        Channel channel = channels.get(channelType);
        if (this.activeChannels.contains(channel)) {
            activeChannels.remove(channel);
            return false;
        } else {
            activeChannels.add(channel);
            return true;
        }
    }

    private void loadPlayerChannels() {
        Resident resident = TownyUniverse.getInstance().getResident(this.getUniqueId());
        if (resident != null) {
            Town town = resident.getTownOrNull();
            Nation nation = resident.getNationOrNull();

            if (town != null && this.channelManager.getChannels().containsKey(town)) {
                Channel townChannel = this.channelManager.getChannel(town);

                this.activeChannels.add(townChannel);
                this.channels.put(ChannelTypes.TOWN, townChannel);
            }

            if (nation != null && this.channelManager.getChannels().containsKey(nation)) {
                Channel nationChannel = this.channelManager.getChannel(nation);

                this.activeChannels.add(nationChannel);
                this.channels.put(ChannelTypes.NATION, nationChannel);
            }
        }

        Channel globalChannel = TownyChat.PLUGIN.getGlobalChannel();
        this.activeChannels.add(globalChannel);
        this.channels.put(ChannelTypes.GLOBAL, globalChannel);
        this.currentChannel = globalChannel;

        Channel localChannel = TownyChat.PLUGIN.getLocalChannel();
        this.activeChannels.add(localChannel);
        this.channels.put(ChannelTypes.LOCAL, localChannel);
    }
}
