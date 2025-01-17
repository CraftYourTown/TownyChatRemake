package net.laboulangerie.townychat.commands;

import com.palmergames.bukkit.towny.TownyMessaging;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.laboulangerie.townychat.TownyChat;
import net.laboulangerie.townychat.channels.ChannelTypes;
import net.laboulangerie.townychat.player.ChatPlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ShortcutCommand implements CommandExecutor {
    private final ChannelTypes channelType;

    public ShortcutCommand(ChannelTypes channelType) {
        this.channelType = channelType;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias,
                             @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            String errorMessage = TownyChat.PLUGIN.getConfig().getString("lang.err_sender_not_player");
            sender.sendMessage(MiniMessage.miniMessage().deserialize(errorMessage));
            return true;
        }

        ChatPlayer chatPlayer = TownyChat.PLUGIN.getChatPlayerManager().getChatPlayer(player);

        if (args.length == 0 && chatPlayer.getChannel(channelType) != null) {

            chatPlayer.setCurrentChannel(channelType);
            String switchMessage = TownyChat.PLUGIN.getConfig().getString("lang.channel_switched");
            TextComponent switchMessageComponent = (TextComponent) MiniMessage.miniMessage().deserialize(switchMessage,
                    Placeholder.unparsed("channel", channelType.name()));
            TownyMessaging.sendMsg(sender, switchMessageComponent.content());
            return true;
        }

        String message = String.join(" ", args);

        if (chatPlayer.getChannels().containsKey(this.channelType)) {
            ChannelTypes previousChannelType = chatPlayer.getCurrentChannel().getType();
            chatPlayer.setCurrentChannel(this.channelType);
            player.chat(message);
            chatPlayer.setCurrentChannel(previousChannelType);
            return true;
        }

        String errorMessage = TownyChat.PLUGIN.getConfig().getString("lang.err_channel_not_found");
        TextComponent errorMessageComponent = (TextComponent) MiniMessage.miniMessage().deserialize(errorMessage,
                Placeholder.unparsed("channel", channelType.name().toLowerCase()));

        TownyMessaging.sendErrorMsg(sender, errorMessageComponent.content());

        return true;
    }
}
